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

package stroom.query.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.Result;
import stroom.query.api.v2.ResultRequest;
import stroom.query.api.v2.ResultRequest.Fetch;
import stroom.query.api.v2.ResultRequest.ResultStyle;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.api.v2.TableResult;
import stroom.query.v2.format.FieldFormatter;
import stroom.query.v2.format.FormatterFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResponseCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResponseCreator.class);

    private final Store store;

    private final Map<String, ResultCreator> cachedResultCreators = new HashMap<>();

    // Cache the last results for each component.
    private final Map<String, Result> resultCache = new HashMap<>();

    public SearchResponseCreator(final Store store) {
        this.store = store;
    }

    public SearchResponse create(final SearchRequest searchRequest) {
        List<Result> results = new ArrayList<>(searchRequest.getResultRequests().size());
        final boolean complete = store.isComplete();

        // Provide results if this search is incremental or the search is complete.
        if (searchRequest.incremental() || complete) {
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
                                LOGGER.info("Delivering " + result + " for " + componentId);
                            }
                        }
                    }
                }
            }
        }

        if (results.size() == 0) {
            results = null;
        }

        return new SearchResponse(store.getHighlights(), results, store.getErrors(), complete);
    }

    private ResultCreator getResultCreator(final String componentId,
                                           final ResultRequest resultRequest,
                                           final String dateTimeLocale) {
        if (cachedResultCreators.containsKey(componentId)) {
            return cachedResultCreators.get(componentId);
        }

        ResultCreator resultCreator = null;
        try {
            final MaxResults maxResults = new MaxResults(resultRequest.)
            if (ResultStyle.TABLE.equals(resultRequest.getResultStyle())) {
                final FieldFormatter fieldFormatter = new FieldFormatter(new FormatterFactory(dateTimeLocale));
                resultCreator = new TableResultCreator(fieldFormatter);
            } else {
                resultCreator = new FlatResultCreator(resultRequest, null, null);
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