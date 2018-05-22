package stroom.query.audit.client;

import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.DocRefServiceSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DocRefRemoteServiceSupplier implements DocRefServiceSupplier {
    private final RemoteClientCache<DocRefService> remoteClientCache;

    public static DocRefRemoteServiceSupplier forUrls(final Map<String, String> urlsByType,
                                                      final Map<String, Class<? extends DocRefEntity>> docRefServices) {
        return new DocRefRemoteServiceSupplier(urlsByType, docRefServices, d -> d);
    }

    public static DocRefRemoteServiceSupplier forUrlsWrapped(final Map<String, String> urlsByType,
                                                             final Map<String, Class<? extends DocRefEntity>> docRefServices,
                                                             final Function<DocRefService, DocRefService> wrapper) {
        return new DocRefRemoteServiceSupplier(urlsByType, docRefServices, wrapper);
    }

    private DocRefRemoteServiceSupplier(final Map<String, String> urlsByType,
                                        final Map<String, Class<? extends DocRefEntity>> docRefServiceClasses,
                                        final Function<DocRefService, DocRefService> wrapper) {
        this.remoteClientCache = new RemoteClientCache<>(urlsByType::get, (t, u) ->
                Optional.ofNullable(docRefServiceClasses.get(t))
                        .map(c -> new DocRefServiceHttpClient<>(t, c, u))
                        .map(wrapper)
                        .orElseThrow(() -> new RuntimeException("No explicitly typed Doc Ref service provided for " + t)));
    }

    @Override
    public Optional<DocRefService> apply(final String s) {
        return remoteClientCache.apply(s);
    }
}
