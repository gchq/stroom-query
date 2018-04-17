package stroom.query.audit.client;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.DocRefResource;
import stroom.query.audit.rest.QueryResource;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class RemoteClientModule extends AbstractModule {
    private final Map<String, String> urlsByType;
    private final Map<String, Supplier<? extends DocRefService<?>>> docRefServices;

    public RemoteClientModule(final Map<String, String> urlsByType) {
        this.urlsByType = urlsByType;
        this.docRefServices = new HashMap<>();
    }

    public <DOC_REF_TYPE extends DocRefEntity>
    RemoteClientModule addType(final String type,
                               final Class<DOC_REF_TYPE> docRefClass) {
        docRefServices.put(type, () -> new DocRefServiceHttpClient<>(type, docRefClass, urlsByType.get(type)));
        return this;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<RemoteClientCache<QueryService>>(){})
            .toInstance(new RemoteClientCache<>(this.urlsByType::get, QueryServiceHttpClient::new));
        bind(new TypeLiteral<RemoteClientCache<QueryResource>>(){})
                .toInstance(new RemoteClientCache<>(this.urlsByType::get, (t, u) -> new QueryResourceHttpClient(u)));
        bind(new TypeLiteral<RemoteClientCache<DocRefResource>>(){})
                .toInstance(new RemoteClientCache<>(this.urlsByType::get, (t, u) -> new DocRefResourceHttpClient(u)));
        bind(new TypeLiteral<RemoteClientCache<DocRefService>>(){})
                .toInstance(new RemoteClientCache<>(this.urlsByType::get, (t, u) ->
                    Optional.ofNullable(docRefServices.get(t))
                            .map(Supplier::get)
                            .orElseThrow(() -> new RuntimeException("No explicitly typed Doc Ref service provided for " + t))));
    }
}
