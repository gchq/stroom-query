/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.common.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.Result;
import stroom.query.api.v2.ResultRequest;
import stroom.query.api.v2.ResultRequest.Fetch;
import stroom.query.api.v2.ResultRequest.ResultStyle;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.api.v2.TableResult;
import stroom.query.common.v2.format.FieldFormatter;
import stroom.query.common.v2.format.FormatterFactory;
import stroom.query.util.LambdaLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//TODO copy SearchResultCreatorManager from stroom into here for a common solution to
//caching the response creators. Will require an abstraction of a cache and a cache manager

public class SearchResponseCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResponseCreator.class);

    private static final Duration FALL_BACK_DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final Store store;
    private final Duration defaultTimeout;

    private final Map<String, ResultCreator> cachedResultCreators = new HashMap<>();

    // Cache the last results for each component.
    private final Map<String, Result> resultCache = new HashMap<>();

    /**
     * @param store The underlying store to use for creating the search responses.
     */
    public SearchResponseCreator(final Store store) {

        this.store = Objects.requireNonNull(store);
        this.defaultTimeout = FALL_BACK_DEFAULT_TIMEOUT;
    }

    /**
     * @param store          The underlying store to use for creating the search responses.
     * @param defaultTimeout The service's default timeout period to use for waiting for the store to complete. This
     *                       will be used when the search request hasn't specified a timeout period.
     */
    public SearchResponseCreator(final Store store, final Duration defaultTimeout) {

        this.store = Objects.requireNonNull(store);
        this.defaultTimeout = Objects.requireNonNull(defaultTimeout);
    }

    /**
     * Build a {@link SearchResponse} from the passed {@link SearchRequest}.
     * @param searchRequest The {@link SearchRequest} containing the query terms and the result requests
     * @return A {@link SearchResponse} object that may be one of:
     * <ul>
     *     <li>A 'complete' {@link SearchResponse} containing all the data requested</li>
     *     <li>An incomplete {@link SearchResponse} containing none, some or all of the data requested, i.e the
     *     currently know results at the point the request is made.</li>
     *     <li>An empty response with an error message indicating the request timed out waiting for a 'complete'
     *     result set. This only applies to non-incremental queries.</li>
     *     <li>An empty response with a different error message. This will happen when some unexpected error has
     *     occurred while assembling the {@link SearchResponse}</li>
     * </ul>
     */
    public SearchResponse create(final SearchRequest searchRequest) {
        final boolean didSearchComplete;

        if (!store.isComplete()) {
            LOGGER.debug("Store not complete so will wait for completion or timeout");
            try {
                final Duration effectiveTimeout = getEffectiveTimeout(searchRequest);

                LOGGER.debug("effectiveTimeout: {}", effectiveTimeout);

                final CountDownLatch storeCompletionLatch = new CountDownLatch(1);

                if (Duration.ZERO.equals(effectiveTimeout)) {
                    // timeout is 0 so no point registering a completion listener
                    didSearchComplete = store.isComplete();
                } else {
                    //When the store completes/terminates the latch will be counted down to release the block
                    store.registerCompletionListener(storeCompletionLatch::countDown);

                    //block and wait for the store to notify us of its completion/termination, or
                    //if the wait is too long we will timeout
                    didSearchComplete = storeCompletionLatch.await(effectiveTimeout.toMillis(), TimeUnit.MILLISECONDS);
                }

                if (!didSearchComplete && !searchRequest.incremental()) {
                    //search didn't complete non-incremental search in time so return a timed out error response
                    return createErrorResponse(
                            store,
                            Collections.singletonList(
                                    LambdaLogger.buildMessage("The search timed out after {}", effectiveTimeout.toString())));
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.debug("Thread {} interrupted", Thread.currentThread().getName(), e);
                return createErrorResponse(
                        store, Collections.singletonList("Thread was interrupted before the search could complete"));
            }
        }

        //we will only get here if the search is complete or it is an incremental search in which case we don't care
        //about completion state. Therefore assemble whatever results we currently have
        try {
            // Get completion state before we get results.
            final boolean complete = store.isComplete();

            List<Result> results = getResults(searchRequest);

            if (results.size() == 0) {
                results = null;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Returning new SearchResponse with results: {}, complete: {}, isComplete: {}",
                        (results == null ? "null" : results.size()), complete, store.isComplete());
            }
            return new SearchResponse(store.getHighlights(), results, store.getErrors(), complete);

        } catch (Exception e) {
            LOGGER.error("Error getting search results for query {}", searchRequest.getKey().toString(), e);

            return createErrorResponse(
                    store, Collections.singletonList(
                            LambdaLogger.buildMessage(
                                    "Error getting search results: [{}], see service's logs for details",
                                    e.getMessage())));
        }
    }

    /**
     * @param errorMessages List of errors to add to the {@link SearchResponse}
     * @return An empty {@link SearchResponse} with the passed error messages
     */
    public static SearchResponse createErrorResponse(final List<String> errorMessages) {
        Objects.requireNonNull(errorMessages);
        List<String> errors = new ArrayList<>();
        errors.addAll(errorMessages);
        return new SearchResponse(
                null,
                null,
                errors,
                false);
    }

    private static SearchResponse createErrorResponse(final Store store, List<String> errorMessages) {
        Objects.requireNonNull(store);
        Objects.requireNonNull(errorMessages);
        List<String> errors = new ArrayList<>();
        errors.addAll(errorMessages);
        if (store.getErrors() != null) {
            errors.addAll(store.getErrors());
        }
        return createErrorResponse(errors);
    }


    private Duration getEffectiveTimeout(final SearchRequest searchRequest) {
        Duration requestedTimeout = searchRequest.getTimeout() == null
                ? null
                : Duration.ofMillis(searchRequest.getTimeout());
        if (requestedTimeout != null) {
            return requestedTimeout;
        } else {
            if (searchRequest.incremental()) {
                //no timeout supplied so they want a response immediately
                return Duration.ZERO;
            } else {
                //this is synchronous so just use the service's default
                return defaultTimeout;
            }
        }
    }

    private List<Result> getResults(final SearchRequest searchRequest) {

        // Provide results if this search is incremental or the search is complete.
        List<Result> results = new ArrayList<>(searchRequest.getResultRequests().size());
        // Copy the requested portion of the result cache into the result.
        for (final ResultRequest resultRequest : searchRequest.getResultRequests()) {
            final String componentId = resultRequest.getComponentId();

            // Only deliver data to components that actually want it.
            final Fetch fetch = resultRequest.getFetch();
            if (!Fetch.NONE.equals(fetch)) {
                Result result = null;

                final Data data = store.getData(componentId);
                if (data != null) {
                    try {
                        final ResultCreator resultCreator = getResultCreator(componentId,
                                resultRequest, searchRequest.getDateTimeLocale());
                        if (resultCreator != null) {
                            result = resultCreator.create(data, resultRequest);
                        }
                    } catch (final Exception e) {
                        result = new TableResult(componentId, null, null, null, e.getMessage());
                    }
                }

                if (result != null) {
                    if (fetch == null || Fetch.ALL.equals(fetch)) {
                        // If the fetch option has not been set or is set to ALL we deliver the full result.
                        results.add(result);
                        LOGGER.info("Delivering " + result + " for " + componentId);

                    } else if (Fetch.CHANGES.equals(fetch)) {
                        // Cache the new result and get the previous one.
                        final Result lastResult = resultCache.put(componentId, result);

                        // See if we have delivered an identical result before so we
                        // don't send more data to the client than we need to.
                        if (!result.equals(lastResult)) {
                            //
                            // CODE TO HELP DEBUGGING.
                            //

                            // try {
                            // if (lastComponentResult instanceof
                            // ChartResult) {
                            // final ChartResult lr = (ChartResult)
                            // lastComponentResult;
                            // final ChartResult cr = (ChartResult)
                            // componentResult;
                            // final File dir = new
                            // File(FileUtil.getTempDir());
                            // StreamUtil.stringToFile(lr.getJSON(), new
                            // File(dir, "last.json"));
                            // StreamUtil.stringToFile(cr.getJSON(), new
                            // File(dir, "current.json"));
                            // }
                            // } catch (final Exception e) {
                            // LOGGER.error(e.getMessage(), e);
                            // }

                            // Either we haven't returned a result before or this result
                            // is different from the one delivered previously so deliver it to the client.
                            results.add(result);
                            LOGGER.info("Delivering {} for {}", result, componentId);
                        }
                    }
                }
            }
        }
        return results;
    }

    private ResultCreator getResultCreator(final String componentId,
                                           final ResultRequest resultRequest,
                                           final String dateTimeLocale) {

        if (cachedResultCreators.containsKey(componentId)) {
            return cachedResultCreators.get(componentId);
        }

        ResultCreator resultCreator = null;
        try {
            if (ResultStyle.TABLE.equals(resultRequest.getResultStyle())) {
                final FieldFormatter fieldFormatter = new FieldFormatter(new FormatterFactory(dateTimeLocale));
                resultCreator = new TableResultCreator(fieldFormatter, store.getDefaultMaxResultsSizes());
            } else {
                resultCreator = new FlatResultCreator(
                        resultRequest,
                        null,
                        null,
                        store.getDefaultMaxResultsSizes(),
                        store.getStoreSize());
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            cachedResultCreators.put(componentId, resultCreator);
        }

        return resultCreator;
    }

    public void destroy() {
        store.destroy();
    }
}