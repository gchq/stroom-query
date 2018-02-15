package stroom.query.audit;

import event.logging.EventLoggingService;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.AuthorisationServiceConfig;
import stroom.query.audit.authorisation.AuthorisationServiceImpl;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.authorisation.NoAuthAuthorisationServiceImpl;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.security.NoAuthValueFactoryProvider;
import stroom.query.audit.security.RobustJwtAuthFilter;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.security.TokenConfig;
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
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <QUERY_SERVICE> Implementation class for the Query Service
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        DOC_REF_POJO extends DocRefEntity,
        QUERY_SERVICE extends QueryService,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>> implements ConfiguredBundle<CONFIG> {

    protected final Class<DOC_REF_POJO> docRefEntityClass;
    private final Class<QUERY_SERVICE> queryServiceClass;
    protected final Class<DOC_REF_SERVICE> docRefServiceClass;

    public AuditedQueryBundle(final Class<DOC_REF_POJO> docRefEntityClass,
                              final Class<QUERY_SERVICE> queryServiceClass,
                              final Class<DOC_REF_SERVICE> docRefServiceClass) {
        this.docRefEntityClass = docRefEntityClass;
        this.queryServiceClass = queryServiceClass;
        this.docRefServiceClass = docRefServiceClass;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
        environment.jersey().register(AuditedQueryResourceImpl.class);
        environment.jersey().register(AuditedDocRefResourceImpl.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(QueryEventLoggingService.class).to(EventLoggingService.class);
                bind(queryServiceClass).to(QueryService.class);
                bind(new DocRefEntity.ClassProvider<>(docRefEntityClass)).to(DocRefEntity.ClassProvider.class);
                bind(docRefServiceClass).to(DocRefService.class);
                if (configuration.getTokenConfig().getSkipAuth()) {
                    bind(NoAuthAuthorisationServiceImpl.class).to(AuthorisationService.class);
                } else {
                    bind(AuthorisationServiceImpl.class).to(AuthorisationService.class);
                    bind(configuration.getAuthorisationServiceConfig()).to(AuthorisationServiceConfig.class);
                    bind(configuration.getTokenConfig()).to(TokenConfig.class);
                }
            }
        });

        // Configure auth
        if (configuration.getTokenConfig().getSkipAuth()) {
            environment.jersey().register(new NoAuthValueFactoryProvider.Binder());
        } else {
            environment.jersey().register(
                    new AuthDynamicFeature(
                            new RobustJwtAuthFilter(configuration.getTokenConfig())
                    ));
            environment.jersey().register(new AuthValueFactoryProvider.Binder<>(ServiceUser.class));
            environment.jersey().register(RolesAllowedDynamicFeature.class);
        }
    }
}
