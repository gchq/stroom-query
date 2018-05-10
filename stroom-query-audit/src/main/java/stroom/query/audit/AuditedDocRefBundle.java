package stroom.query.audit;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import event.logging.EventLoggingService;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.service.DocRefService;
import stroom.query.authorisation.AuthorisationService;
import stroom.query.authorisation.AuthorisationServiceConfig;
import stroom.query.authorisation.AuthorisationServiceImpl;
import stroom.query.authorisation.HasAuthorisationConfig;
import stroom.query.authorisation.NoAuthAuthorisationServiceImpl;
import stroom.query.security.HasTokenConfig;
import stroom.query.security.NoAuthValueFactoryProvider;
import stroom.query.security.RobustJwtAuthFilter;
import stroom.query.security.ServiceUser;
import stroom.query.security.TokenConfig;

import java.util.function.Function;

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
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedDocRefBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>,
        DOC_REF_POJO extends DocRefEntity> implements ConfiguredBundle<CONFIG> {

    private Function<CONFIG, Injector> injectorSupplier;
    protected final Class<DOC_REF_SERVICE> docRefServiceClass;
    protected final Class<DOC_REF_POJO> docRefEntityClass;

    public AuditedDocRefBundle(final Function<CONFIG, Injector> injectorSupplier,
                               final Class<DOC_REF_SERVICE> docRefServiceClass,
                               final Class<DOC_REF_POJO> docRefEntityClass) {
        this.injectorSupplier = injectorSupplier;
        this.docRefServiceClass = docRefServiceClass;
        this.docRefEntityClass = docRefEntityClass;
    }

    /**
     * This function will be overridden by child classes that have further specific modules to register.
     * @param configuration The dropwizard application configuration
     * @return A guice module which combines the immediate dependencies and any from underlying bundles
     */
    public Module getGuiceModule(final CONFIG configuration) {
        return new AbstractModule() {
            @Override
            @SuppressWarnings("unchecked")
            protected void configure() {
                bind(EventLoggingService.class).to(QueryEventLoggingService.class);
                bind(DocRefEntity.ClassProvider.class).toInstance(new DocRefEntity.ClassProvider<>(docRefEntityClass));
                bind(DocRefService.class).to(docRefServiceClass);

                if (configuration.getTokenConfig().getSkipAuth()) {
                    bind(AuthorisationService.class).to(NoAuthAuthorisationServiceImpl.class);
                } else {
                    bind(AuthorisationService.class).to(AuthorisationServiceImpl.class);
                    bind(AuthorisationServiceConfig.class).toInstance(configuration.getAuthorisationServiceConfig());
                    bind(TokenConfig.class).toInstance(configuration.getTokenConfig());
                }
            }
        };
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
        final Injector injector = injectorSupplier.apply(configuration);

        environment.jersey().register(injector.getInstance(AuditedQueryResourceImpl.class));
        environment.jersey().register(injector.getInstance(AuditedDocRefResourceImpl.class));

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
