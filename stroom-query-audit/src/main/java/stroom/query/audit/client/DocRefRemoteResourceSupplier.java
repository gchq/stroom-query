package stroom.query.audit.client;

import stroom.query.audit.rest.DocRefResource;
import stroom.query.audit.rest.DocRefResourceSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DocRefRemoteResourceSupplier implements DocRefResourceSupplier {

    private final RemoteClientCache<DocRefResource> remoteClientCache;

    public static DocRefRemoteResourceSupplier forUrls(final Map<String, String> urlsByType) {
        return new DocRefRemoteResourceSupplier(urlsByType, (t, u) -> new DocRefResourceHttpClient(u));
    }

    public static DocRefRemoteResourceSupplier forUrlsWrapped(final Map<String, String> urlsByType,
                                                              final Function<DocRefResource, DocRefResource> wrapper) {
        return new DocRefRemoteResourceSupplier(urlsByType, (t, u) -> wrapper.apply(new DocRefResourceHttpClient(u)));
    }

    private DocRefRemoteResourceSupplier(final Map<String, String> urlsByType,
                                         final BiFunction<String, String, DocRefResource> supplier) {
        this.remoteClientCache = new RemoteClientCache<>(urlsByType::get, supplier);
    }

    @Override
    public Optional<DocRefResource> apply(final String s) {
        return remoteClientCache.apply(s);
    }
}
