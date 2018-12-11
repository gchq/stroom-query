package stroom.query.hibernate;

import stroom.mapreduce.v2.UnsafePairQueue;
import stroom.query.api.v2.TableSettings;
import stroom.query.common.v2.CompiledSorter;
import stroom.query.common.v2.CompletionListener;
import stroom.query.common.v2.Coprocessor;
import stroom.query.common.v2.CoprocessorSettingsMap;
import stroom.query.common.v2.Data;
import stroom.query.common.v2.GroupKey;
import stroom.query.common.v2.Item;
import stroom.query.common.v2.Payload;
import stroom.query.common.v2.ResultStoreCreator;
import stroom.query.common.v2.Sizes;
import stroom.query.common.v2.Store;
import stroom.query.common.v2.TableCoprocessorSettings;
import stroom.query.common.v2.TablePayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to store the results from a query made on a {@link QueryServiceCriteriaImpl}
 */
public class CriteriaStore implements Store {

    private final CoprocessorSettingsMap coprocessorSettingsMap;
    private final Map<CoprocessorSettingsMap.CoprocessorKey, Coprocessor> coprocessorMap;
    private final Map<CoprocessorSettingsMap.CoprocessorKey, Payload> payloadMap;

    private final Sizes defaultMaxResultsSizes;
    private final Sizes storeSize;
    private final List<CompletionListener> completionListeners = Collections.synchronizedList(new ArrayList<>());

    //results are currently assembled synchronously in getData so the store is always complete
    private final AtomicBoolean isComplete = new AtomicBoolean(true);

    public CriteriaStore(final Sizes defaultMaxResultsSizes,
                         final Sizes storeSize,
                         final CoprocessorSettingsMap coprocessorSettingsMap,
                         final Map<CoprocessorSettingsMap.CoprocessorKey, Coprocessor> coprocessorMap,
                         final Map<CoprocessorSettingsMap.CoprocessorKey, Payload> payloadMap) {

        this.defaultMaxResultsSizes = defaultMaxResultsSizes;
        this.storeSize = storeSize;
        this.coprocessorSettingsMap = coprocessorSettingsMap;
        this.coprocessorMap = coprocessorMap;
        this.payloadMap = payloadMap;
    }

    @Override
    public void destroy() {
        //nothing to do as this store doesn't hold any query state
    }

    @Override
    public boolean isComplete() {
        return isComplete.get();
    }

    @Override
    public Data getData(String componentId) {
        final CoprocessorSettingsMap.CoprocessorKey coprocessorKey = coprocessorSettingsMap.getCoprocessorKey(componentId);
        if (coprocessorKey == null) {
            return null;
        }
        if (null == payloadMap) {
            return new Data(new HashMap<>(), 0, 0);
        }

        TableCoprocessorSettings tableCoprocessorSettings = (TableCoprocessorSettings) coprocessorSettingsMap.getMap()
                .get(coprocessorKey);
        TableSettings tableSettings = tableCoprocessorSettings.getTableSettings();

        Payload payload = payloadMap.get(coprocessorKey);
        TablePayload tablePayload = (TablePayload) payload;
        UnsafePairQueue<GroupKey, Item> queue = tablePayload.getQueue();

        CompiledSorter compiledSorter = new CompiledSorter(tableSettings.getFields());
        final ResultStoreCreator resultStoreCreator = new ResultStoreCreator(compiledSorter);
        resultStoreCreator.read(queue);

        // Trim the number of results in the store.
        resultStoreCreator.trim(storeSize);

        return resultStoreCreator.create(queue.size(), queue.size());
    }

    @Override
    public List<String> getErrors() {
        return null;
    }

    @Override
    public List<String> getHighlights() {
        return null;
    }

    @Override
    public Sizes getDefaultMaxResultsSizes() {
        return defaultMaxResultsSizes;
    }

    @Override
    public Sizes getStoreSize() {
        return storeSize;
    }

    @Override
    public void registerCompletionListener(final CompletionListener completionListener) {
        if (isComplete.get()) {
            //immediate notification
            completionListener.onCompletion();
        } else {
            //TODO this is currently of no use but when incremenntal queries are implemented it will be needed
            completionListeners.add(Objects.requireNonNull(completionListener));
        }
    }
}
