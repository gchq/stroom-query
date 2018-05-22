package stroom.query.testing;

import com.google.inject.AbstractModule;
import org.mockito.Mockito;
import stroom.query.audit.client.*;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.DocRefResource;
import stroom.query.audit.rest.DocRefResourceSupplier;
import stroom.query.audit.rest.QueryResource;
import stroom.query.audit.rest.QueryResourceSupplier;
import stroom.query.audit.service.DocRefServiceSupplier;
import stroom.query.audit.service.QueryService;
import stroom.query.audit.service.QueryServiceSupplier;

import java.util.HashMap;
import java.util.Map;

public class RemoteClientTestingModule extends AbstractModule {
    private final Map<String, String> urlsByType;
    private final Map<String, Class<? extends DocRefEntity>> docRefServiceClasses;

    public RemoteClientTestingModule(final Map<String, String> urlsByType) {
        this.urlsByType = urlsByType;
        this.docRefServiceClasses = new HashMap<>();
    }

    public <DOC_REF_TYPE extends DocRefEntity>
    RemoteClientTestingModule addType(final String type,
                                      final Class<DOC_REF_TYPE> docRefClass) {
        docRefServiceClasses.put(type, docRefClass);
        return this;
    }

    @Override
    protected void configure() {
        bind(QueryServiceSupplier.class)
                .toInstance(QueryRemoteServiceSupplier.forUrlsWrapped(this.urlsByType, Mockito::spy));
        bind(QueryResourceSupplier.class)
                .toInstance(QueryRemoteResourceSupplier.forUrlsWrapped(this.urlsByType, Mockito::spy));
        bind(DocRefResourceSupplier.class)
                .toInstance(DocRefRemoteResourceSupplier.forUrlsWrapped(this.urlsByType, Mockito::spy));
        bind(DocRefServiceSupplier.class)
                .toInstance(DocRefRemoteServiceSupplier.forUrlsWrapped(this.urlsByType, docRefServiceClasses, Mockito::spy));
    }

    private QueryService createQueryService(final String type,
                                            final String url) {
        return Mockito.spy(new QueryServiceHttpClient(type, url));
    }

    private QueryResource createQueryResource(final String type,
                                              final String url) {
        return Mockito.spy(new QueryResourceHttpClient(url));
    }

    private DocRefResource createDocRefResource(final String type,
                                                final String url) {
        return Mockito.spy(new DocRefResourceHttpClient(url));
    }
}
