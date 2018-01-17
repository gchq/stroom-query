package stroom.query.audit;

import event.logging.EventLoggingService;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.AuthorisationServiceConfig;
import stroom.query.audit.authorisation.AuthorisationServiceImpl;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.security.TokenConfig;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

/**
 * This Dropwizard bundle can be used to register an implementation of Query Resource implementation
 * This bundle will wrap that implementation in an audited layer, any requests made to your Query Resource
 * will be passed to the {@link QueryEventLoggingService}
 *
 * It will also register an audited version of the external DocRef resource. External DataSources will need to provide
 * implementations of DocRef resource to allow stroom to manage the documents that live outside of it.
 *
 * @param <CONFIG> The configuration class
 * @param <QUERY_SERVICE> Implementation class for the Query Service
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <AUDITED_DOC_REF_RESOURCE> Implementation class for the Audited DocRef Resource
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public final class AuditedQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        QUERY_SERVICE extends QueryService,
        DOC_REF_POJO extends DocRefEntity,
        AUDITED_DOC_REF_RESOURCE extends AuditedDocRefResourceImpl<DOC_REF_POJO>,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>> implements ConfiguredBundle<CONFIG> {

    private final Class<QUERY_SERVICE> queryServiceClass;
    private final Class<DOC_REF_POJO> docRefClass;
    private final Class<AUDITED_DOC_REF_RESOURCE> auditedDocRefResourceClass;
    private final Class<DOC_REF_SERVICE> docRefServiceClass;

    public AuditedQueryBundle(final Class<QUERY_SERVICE> queryServiceClass,
                              final Class<DOC_REF_POJO> docRefClass,
                              final Class<AUDITED_DOC_REF_RESOURCE> auditedDocRefResourceClass,
                              final Class<DOC_REF_SERVICE> docRefServiceClass) {
        this.queryServiceClass = queryServiceClass;
        this.docRefClass = docRefClass;
        this.auditedDocRefResourceClass = auditedDocRefResourceClass;
        this.docRefServiceClass = docRefServiceClass;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
        environment.jersey().register(AuditedQueryResourceImpl.class);
        environment.jersey().register(auditedDocRefResourceClass);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(QueryEventLoggingService.class).to(EventLoggingService.class);
                bind(queryServiceClass).to(QueryService.class);
                bind(docRefServiceClass).to(new ParameterizedTypeImpl(DocRefService.class, docRefClass));
                bind(AuthorisationServiceImpl.class).to(AuthorisationService.class);
                bind(configuration.getAuthorisationServiceConfig()).to(AuthorisationServiceConfig.class);
                bind(configuration.getTokenConfig()).to(TokenConfig.class);
            }
        });
    }
}
