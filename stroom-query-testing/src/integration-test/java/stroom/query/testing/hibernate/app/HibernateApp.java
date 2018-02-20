package stroom.query.testing.hibernate.app;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stroom.query.hibernate.AuditedCriteriaQueryBundle;

public class HibernateApp extends Application<HibernateConfig> {


    private final AuditedCriteriaQueryBundle<HibernateConfig,
            TestDocRefServiceCriteriaImpl,
            TestDocRefHibernateEntity,
            TestQueryableHibernateEntity> auditedQueryBundle =
            new AuditedCriteriaQueryBundle<>(
                    TestDocRefServiceCriteriaImpl.class,
                    TestDocRefHibernateEntity.class,
                    TestQueryableHibernateEntity.class);

    public static void main(final String[] args) throws Exception {
        new HibernateApp().run(args);
    }

    @Override
    public void run(final HibernateConfig configuration,
                    final Environment environment) {

        environment.healthChecks().register("Something", new HealthCheck() {
            @Override
            protected Result check() {
                return Result.healthy("Keeps Dropwizard Happy");
            }
        });
    }

    @Override
    public void initialize(final Bootstrap<HibernateConfig> bootstrap) {
        super.initialize(bootstrap);

        // This allows us to use templating in the YAML configuration.
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(this.auditedQueryBundle);
    }
}
