package stroom.query.common.v2;

import stroom.mapreduce.v2.Pair;
import stroom.mapreduce.v2.Reader;
import stroom.mapreduce.v2.Source;

import java.util.HashMap;
import java.util.Map;

public class ResultStoreCreator implements Reader<Key, Item> {
    private final CompiledSorter sorter;
    private final Map<Key, Items<Item>> childMap;

    public ResultStoreCreator(final CompiledSorter sorter) {
        this.sorter = sorter;
        childMap = new HashMap<>();
    }

    public Data create(final long size, final long totalSize) {
        return new Data(childMap, size, totalSize);
    }

    public Map<Key, Items<Item>> getChildMap() {
        return childMap;
    }

    @Override
    public void read(final Source<Key, Item> source) {
        // We should now have a reduction in the reducedQueue.
        for (final Pair<Key, Item> pair : source) {
            final Item item = pair.getValue();

            if (item.key != null) {
                childMap.computeIfAbsent(item.key.getParent(), k -> new ItemsArrayList<>()).add(item);
            } else {
                childMap.computeIfAbsent(null, k -> new ItemsArrayList<>()).add(item);
            }
        }
    }

    public void trim(final StoreSize storeSize) {
        trim(storeSize, null, 0);
    }

    private void trim(final StoreSize storeSize, final Key parentKey, final int depth) {
        final Items<Item> parentItems = childMap.get(parentKey);
        if (parentItems != null && storeSize != null) {
            parentItems.trim(storeSize.size(depth), sorter, item -> {
                // If there is a group key then cascade removal.
                if (item.key != null) {
                    remove(item.key);
                }
            });

            // Ensure remaining items children are also trimmed by cascading
            // trim operation.

            // // Lower levels of results should be reduced by increasing
            // amounts so that we don't get an exponential number of
            // results.
            // int sz = size / 10;
            // if (sz < 1) {
            // sz = 1;
            // }
            for (final Item item : parentItems) {
                if (item.key != null) {
                    trim(storeSize, item.key, depth + 1);
                }
            }
        }
    }

    private void remove(final Key parentKey) {
        final Items<Item> items = childMap.get(parentKey);
        if (items != null) {
            childMap.remove(parentKey);

            // Cascade delete.
            for (final Item item : items) {
                if (item.key != null) {
                    remove(item.key);
                }
            }
        }
    }
}
