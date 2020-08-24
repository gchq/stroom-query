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

import stroom.mapreduce.v2.UnsafePairQueue;
import stroom.query.api.v2.TableSettings;
import stroom.query.common.v2.CoprocessorSettingsMap.CoprocessorKey;
import stroom.util.shared.HasTerminate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SearchResultHandler implements ResultHandler {
    private final CoprocessorSettingsMap coprocessorSettingsMap;
    private final Map<CoprocessorKey, TablePayloadHandler> handlerMap = new HashMap<>();
    private final CompletionState completionState = new CompletionStateImpl();

    public SearchResultHandler(final CoprocessorSettingsMap coprocessorSettingsMap,
                               final Sizes defaultMaxResultsSizes,
                               final Sizes storeSize) {

        this.coprocessorSettingsMap = coprocessorSettingsMap;

        for (final Entry<CoprocessorKey, CoprocessorSettings> entry : coprocessorSettingsMap.getMap().entrySet()) {
            final CoprocessorKey coprocessorKey = entry.getKey();
            final CoprocessorSettings coprocessorSettings = entry.getValue();
            if (coprocessorSettings instanceof TableCoprocessorSettings) {
                final TableCoprocessorSettings tableCoprocessorSettings = (TableCoprocessorSettings) coprocessorSettings;
                final TableSettings tableSettings = tableCoprocessorSettings.getTableSettings();
                // Create a set of sizes that are the minimum values for the combination of user provided sizes for the table and the default maximum sizes.
                final Sizes maxResults = Sizes.min(Sizes.create(tableSettings.getMaxResults()), defaultMaxResultsSizes);

                handlerMap.put(coprocessorKey, new TablePayloadHandler(
                        tableSettings.getFields(),
                        tableSettings.showDetail(),
                        maxResults,
                        storeSize));
            }
        }
    }

    @Override
    public void handle(final Map<CoprocessorKey, Payload> payloadMap, final HasTerminate hasTerminate) {
        if (payloadMap != null) {
            for (final Entry<CoprocessorKey, Payload> entry : payloadMap.entrySet()) {
                final Payload payload = entry.getValue();
                if (payload instanceof TablePayload) {
                    final TablePayload tablePayload = (TablePayload) payload;

                    final TablePayloadHandler payloadHandler = handlerMap.get(entry.getKey());
                    final UnsafePairQueue<GroupKey, Item> newQueue = tablePayload.getQueue();
                    if (newQueue != null) {
                        payloadHandler.addQueue(newQueue, hasTerminate);
                    }
                }
            }
        }
    }

    public TablePayloadHandler getPayloadHandler(final String componentId) {
        final CoprocessorKey coprocessorKey = coprocessorSettingsMap.getCoprocessorKey(componentId);
        if (coprocessorKey == null) {
            return null;
        }

        return handlerMap.get(coprocessorKey);
    }

    @Override
    public boolean shouldTerminateSearch() {
        boolean terminate = false;
        if (handlerMap.size() == coprocessorSettingsMap.getMap().size()) {
            terminate = true;
            for (final PayloadHandler payloadHandler : handlerMap.values()) {
                if (!payloadHandler.shouldTerminateSearch()) {
                    terminate = false;
                    break;
                }
            }
        }

        return terminate;
    }

    @Override
    public CompletionState getCompletionState() {
        return completionState;
    }

//    private boolean areHandlersBusy() {
//        for (final TablePayloadHandler handler : handlerMap.values()) {
//            if (handler.busy()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void setComplete(final boolean complete) {
//        final boolean isAlreadyComplete = this.complete.get();
//        LOGGER.trace("setComplete({}), currentValue={}", complete, isAlreadyComplete);
//        if (isAlreadyComplete && !complete) {
//            throw new RuntimeException("Attempting to mark SearchResultHandler as not complete when it has " +
//                    "already been completed");
//        }
//
//        if (complete) {
//            // We have been told the search is complete but the TablePayloadHandlers may still be doing work
//            // so wait for them
//            boolean hasPendingWorkFinished = false;
//            for (final TablePayloadHandler handler : handlerMap.values()) {
//                hasPendingWorkFinished = handler.waitForPendingWork(10, TimeUnit.SECONDS);
//
//                if (!hasPendingWorkFinished) {
//                    LOGGER.trace("Work still pending after timeout");
//                    break;
//                }
//            }
//            LOGGER.trace("isPendingWorkFinished={}", hasPendingWorkFinished);
//
//            if (hasPendingWorkFinished) {
//                LOGGER.trace("setting complete to {}", true);
//                this.complete.set(true);
//                //notify the listeners
//                for (CompletionListener listener; (listener = completionListeners.poll()) != null; ) {
//                    // when notified they will check isComplete
//                    LOGGER.debug("Notifying {} {} that we are complete", listener.getClass().getName(), listener);
//                    listener.onCompletion();
//                }
//            } else {
//                LOGGER.trace("Handlers are busy so not setting complete");
//            }
//        }
//    }

    @Override
    public Data getResultStore(final String componentId) {
        final TablePayloadHandler tablePayloadHandler = getPayloadHandler(componentId);
        if (tablePayloadHandler != null) {
            return tablePayloadHandler.getData();
        }
        return null;
    }
}
