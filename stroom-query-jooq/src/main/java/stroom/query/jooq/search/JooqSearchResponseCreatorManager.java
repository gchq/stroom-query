package stroom.query.jooq.search;

import stroom.query.common.v2.SearchResponseCreator;
import stroom.query.common.v2.SearchResponseCreatorCache;
import stroom.query.common.v2.SearchResponseCreatorManager;

import javax.inject.Inject;

public class JooqSearchResponseCreatorManager implements SearchResponseCreatorManager {

    private final SearchResponseCreatorCache cache;

    @Inject
    public JooqSearchResponseCreatorManager(final JooqInMemorySearchResponseCreatorCacheFactory cacheFactory,
                                            final JooqStoreFactory storeFactory) {
        this.cache = cacheFactory.create(storeFactory);
    }

    @Override
    public SearchResponseCreator get(final SearchResponseCreatorCache.Key key) {
        return cache.get(key);
    }

    @Override
    public void remove(final SearchResponseCreatorCache.Key key) {
        cache.remove(key);
    }

    /**
     * Do we need to cron this somehow?
     */
    @Override
    public void evictExpiredElements() {
        cache.evictExpiredElements();
    }
}
