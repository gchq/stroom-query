package stroom.query.jooq.search;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import stroom.query.common.v2.AbstractInMemorySearchResponseCreatorCacheFactory;
import stroom.query.common.v2.SearchResponseCreator;
import stroom.query.common.v2.SearchResponseCreatorCache;

public class JooqInMemorySearchResponseCreatorCacheFactory extends AbstractInMemorySearchResponseCreatorCacheFactory {

    @Override
    protected void addAdditionalBuildOptions(final CacheBuilder<SearchResponseCreatorCache.Key, SearchResponseCreator> cacheBuilder) {

    }

    @Override
    protected void registerCache(final CacheBuilder<SearchResponseCreatorCache.Key, SearchResponseCreator> cacheBuilder,
                                 final Cache<SearchResponseCreatorCache.Key, SearchResponseCreator> cache) {

    }
}
