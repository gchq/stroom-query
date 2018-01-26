package stroom.query.testing.generic.app;

import com.codahale.metrics.health.HealthCheck;
import event.logging.EventLoggingService;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import stroom.query.audit.AuditedQueryBundle;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.security.RobustJwtAuthFilter;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.security.TokenConfig;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

import javax.inject.Inject;

public class App extends Application<Config> {
    public static final class AuditedTestDocRefResource extends AuditedDocRefResourceImpl<TestDocRefEntity> {

        @Inject
        public AuditedTestDocRefResource(final DocRefService<TestDocRefEntity> service,
                                         final EventLoggingService eventLoggingService,
                                         final AuthorisationService authorisationService) {
            super(service, eventLoggingService, authorisationService);
        }
    }

    public static final class AuditedTestQueryResource extends AuditedQueryResourceImpl<TestDocRefEntity> {

        @Inject
        public AuditedTestQueryResource(final EventLoggingService eventLoggingService,
                                        final QueryService service,
                                        final AuthorisationService authorisationService,
                                        final DocRefService<TestDocRefEntity> docRefService) {
            super(eventLoggingService, service, authorisationService, docRefService);
        }
    }

    private final AuditedQueryBundle<Config,
            TestDocRefEntity,
            TestQueryServiceImpl,
            AuditedTestQueryResource,
            TestDocRefServiceImpl,
            AuditedTestDocRefResource> auditedQueryBundle =
            new AuditedQueryBundle<>(
                    TestDocRefEntity.class,
                    TestQueryServiceImpl.class,
                    AuditedTestQueryResource.class,
                    TestDocRefServiceImpl.class,
                    AuditedTestDocRefResource.class);

    @Override
    public void run(final Config configuration,
                    final Environment environment) throws Exception {

        environment.healthChecks().register("Something", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("Keeps Dropwizard Happy");
            }
        });
        configureAuthentication(configuration.getTokenConfig(), environment);
    }

    @Override
    public void initialize(final Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);

        // This allows us to use templating in the YAML configuration.
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(this.auditedQueryBundle);

    }

    private static void configureAuthentication(final TokenConfig tokenConfig,
                                                final Environment environment) {
        environment.jersey().register(
                new AuthDynamicFeature(
                        new RobustJwtAuthFilter(tokenConfig)
                ));
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(ServiceUser.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }
}
