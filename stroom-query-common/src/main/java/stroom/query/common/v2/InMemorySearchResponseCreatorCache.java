package stroom.query.common.v2;

import com.google.common.cache.LoadingCache;

class InMemorySearchResponseCreatorCache implements SearchResponseCreatorCache {

    private final LoadingCache<Key, SearchResponseCreator> cache;

    InMemorySearchResponseCreatorCache(final LoadingCache<Key, SearchResponseCreator> cache) {
        this.cache = cache;
    }

    @Override
    public SearchResponseCreator get(final SearchResponseCreatorCache.Key key) {
        return cache.getUnchecked(key);
    }

    @Override
    public void remove(final SearchResponseCreatorCache.Key key) {
        cache.invalidate(key);
        cache.cleanUp();
    }

    @Override
    public void evictExpiredElements() {
        cache.cleanUp();
    }
}
