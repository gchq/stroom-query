package stroom.query.audit.client;

import stroom.query.audit.rest.QueryResource;
import stroom.query.audit.rest.QueryResourceSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class QueryRemoteResourceSupplier implements QueryResourceSupplier {

    private final RemoteClientCache<QueryResource> remoteClientCache;

    public static QueryRemoteResourceSupplier forUrls(final Map<String, String> urlsByType) {
        return new QueryRemoteResourceSupplier(urlsByType, (t, u) -> new QueryResourceHttpClient(u));
    }

    public static QueryRemoteResourceSupplier forUrlsWrapped(final Map<String, String> urlsByType,
                                                             final Function<QueryResource, QueryResource> wrapper) {
        return new QueryRemoteResourceSupplier(urlsByType, (t, u) -> wrapper.apply(new QueryResourceHttpClient(u)));
    }

    private QueryRemoteResourceSupplier(final Map<String, String> urlsByType,
                                        final BiFunction<String, String, QueryResource> supplier) {
        this.remoteClientCache = new RemoteClientCache<>(urlsByType::get, supplier);
    }

    @Override
    public Optional<QueryResource> apply(final String s) {
        return remoteClientCache.apply(s);
    }
}
