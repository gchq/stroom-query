package stroom.query.audit.client;

import stroom.query.audit.service.QueryService;
import stroom.query.audit.service.QueryServiceSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class QueryRemoteServiceSupplier implements QueryServiceSupplier {

    private final RemoteClientCache<QueryService> remoteClientCache;

    public static QueryRemoteServiceSupplier forUrls(final Map<String, String> urlsByType) {
        return new QueryRemoteServiceSupplier(urlsByType, QueryServiceHttpClient::new);
    }

    public static QueryRemoteServiceSupplier forUrlsWrapped(final Map<String, String> urlsByType,
                                                            final Function<QueryService, QueryService> wrapper) {
        return new QueryRemoteServiceSupplier(urlsByType, (t, u) -> wrapper.apply(new QueryServiceHttpClient(t, u)));
    }

    private QueryRemoteServiceSupplier(final Map<String, String> urlsByType,
                                       final BiFunction<String, String, QueryService> supplier) {
        this.remoteClientCache = new RemoteClientCache<>(urlsByType::get, supplier);
    }

    @Override
    public Optional<QueryService> apply(final String s) {
        return remoteClientCache.apply(s);
    }
}
