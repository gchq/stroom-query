package stroom.query.common.v2;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

@SuppressWarnings("unused")
public abstract class AbstractInMemorySearchResponseCreatorCacheFactory implements SearchResponseCreatorCacheFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInMemorySearchResponseCreatorCacheFactory.class);

    @Override
    public SearchResponseCreatorCache create(final StoreFactory storeFactory) {

        final CacheLoader<SearchResponseCreatorCache.Key, SearchResponseCreator> cacheLoader = buildLoaderFunc(storeFactory);

        final CacheBuilder<SearchResponseCreatorCache.Key, SearchResponseCreator> cacheBuilder = CacheBuilder.newBuilder()
                .removalListener(AbstractInMemorySearchResponseCreatorCacheFactory::onRemove);

        addAdditionalBuildOptions(cacheBuilder);

        final LoadingCache<SearchResponseCreatorCache.Key, SearchResponseCreator> cache = cacheBuilder.build(cacheLoader);

        registerCache(cacheBuilder, cache);

        return new InMemorySearchResponseCreatorCache(cache);
    }

    private static void onRemove(final RemovalNotification<SearchResponseCreatorCache.Key, SearchResponseCreator> notification) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removal notification for key {}, value {}, cause {}",
                    notification.getKey(),
                    notification.getValue(),
                    notification.getCause());
        }

        if (notification != null && notification.getValue() != null) {
            notification.getValue().destroy();
        }
    }

    private CacheLoader<SearchResponseCreatorCache.Key, SearchResponseCreator> buildLoaderFunc(final StoreFactory storeFactory) {
        final Function<SearchResponseCreatorCache.Key, SearchResponseCreator> loaderFunc = (SearchResponseCreatorCache.Key key) -> {
            LOGGER.debug("Loading new store for key {}", key);
            final Store store = storeFactory.create(key.getSearchRequest());
            return new SearchResponseCreator(store, key.getHasTerminate());
        };
        return CacheLoader.from(loaderFunc::apply);
    }

    protected abstract void addAdditionalBuildOptions(
            final CacheBuilder<SearchResponseCreatorCache.Key, SearchResponseCreator> cacheBuilder);
    /**
     * Allows for the cache and its builder to be registered with a cache manager
     */
    protected abstract void registerCache(
            final CacheBuilder<SearchResponseCreatorCache.Key, SearchResponseCreator> cacheBuilder,
            final Cache<SearchResponseCreatorCache.Key, SearchResponseCreator> cache);

}
