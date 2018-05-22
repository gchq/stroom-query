package stroom.query.audit.client;

import com.google.inject.AbstractModule;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.DocRefResourceSupplier;
import stroom.query.audit.rest.QueryResourceSupplier;
import stroom.query.audit.service.DocRefServiceSupplier;
import stroom.query.audit.service.QueryServiceSupplier;

import java.util.HashMap;
import java.util.Map;

public class RemoteClientModule extends AbstractModule {
    private final Map<String, String> urlsByType;
    private final Map<String, Class<? extends DocRefEntity>> docRefServiceClasses;

    public RemoteClientModule(final Map<String, String> urlsByType) {
        this.urlsByType = urlsByType;
        this.docRefServiceClasses = new HashMap<>();
    }

    public <DOC_REF_TYPE extends DocRefEntity>
    RemoteClientModule addType(final String type,
                               final Class<DOC_REF_TYPE> docRefClass) {
        docRefServiceClasses.put(type, docRefClass);
        return this;
    }

    @Override
    protected void configure() {
        bind(QueryServiceSupplier.class)
                .toInstance(QueryRemoteServiceSupplier.forUrls(this.urlsByType));
        bind(QueryResourceSupplier.class)
                .toInstance(QueryRemoteResourceSupplier.forUrls(this.urlsByType));
        bind(DocRefResourceSupplier.class)
                .toInstance(DocRefRemoteResourceSupplier.forUrls(this.urlsByType));
        bind(DocRefServiceSupplier.class)
                .toInstance(DocRefRemoteServiceSupplier.forUrls(this.urlsByType, docRefServiceClasses));
    }
}
