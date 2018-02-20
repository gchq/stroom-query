package stroom.query.testing.jooq.app;

import com.bendb.dropwizard.jooq.JooqBundle;
import com.bendb.dropwizard.jooq.JooqFactory;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.query.audit.service.DocRefService;
import stroom.query.jooq.AuditedJooqQueryBundle;

public class JooqApp extends Application<JooqConfig> {
    // Wrap the flyway bundle so that we can call migrate in the bundles 'run'.
    // This allows the flyway migration to happen before the hibernate validation
    private final ConfiguredBundle<JooqConfig> flywayBundle = new ConfiguredBundle<JooqConfig>() {

        private final FlywayBundle<JooqConfig> wrappedBundle = new FlywayBundle<JooqConfig>() {
            public DataSourceFactory getDataSourceFactory(JooqConfig config) {
                return config.getDataSourceFactory();
            }

            public FlywayFactory getFlywayFactory(final JooqConfig config) {
                return config.getFlywayFactory();
            }
        };

        @Override
        public void run(final JooqConfig configuration, final Environment environment) throws Exception {
            wrappedBundle.run(environment);

            final ManagedDataSource dataSource = configuration.getDataSourceFactory()
                    .build(environment.metrics(), "flywayDataSource");
            configuration.getFlywayFactory()
                    .build(dataSource)
                    .migrate();
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {
            wrappedBundle.initialize(bootstrap);
        }

    };

    private final JooqBundle<JooqConfig> jooqBundle = new JooqBundle<JooqConfig>() {
        public DataSourceFactory getDataSourceFactory(JooqConfig configuration) {
            return configuration.getDataSourceFactory();
        }

        public JooqFactory getJooqFactory(JooqConfig configuration) {
            return configuration.getJooqFactory();
        }
    };

    private final AuditedJooqQueryBundle<JooqConfig,
            TestQueryableJooqEntity,
            TestDocRefJooqEntity,
            TestDocRefServiceJooqImpl> auditedQueryBundle =
            new AuditedJooqQueryBundle<>(
                    TestQueryableJooqEntity.class,
                    jooqBundle,
                    TestDocRefJooqEntity.class,
                    TestDocRefServiceJooqImpl.class);

    @Override
    public void run(final JooqConfig configuration,
                    final Environment environment) {
        environment.healthChecks().register("Something", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.healthy("Keeps Dropwizard Happy");
            }
        });

        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TestDocRefServiceJooqImpl.class).to(new TypeLiteral<DocRefService<TestDocRefJooqEntity>>() {});
            }
        });
        environment.jersey().register(CreateTestDataJooqImpl.class);
    }

    @Override
    public void initialize(final Bootstrap<JooqConfig> bootstrap) {
        super.initialize(bootstrap);

        // This allows us to use templating in the YAML configuration.
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(this.flywayBundle);
        bootstrap.addBundle(this.auditedQueryBundle);
    }
}
