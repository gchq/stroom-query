package stroom.query.testing.generic.app;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stroom.query.audit.AuditedQueryBundle;
import stroom.query.audit.service.DocRefService;

import java.util.function.Consumer;

public class App extends Application<Config> {

    private final AuditedQueryBundle<Config,
            TestDocRefServiceImpl,
            TestDocRefEntity,
            TestQueryServiceImpl> auditedQueryBundle = new AuditedQueryBundle<Config,
            TestDocRefServiceImpl,
            TestDocRefEntity,
            TestQueryServiceImpl>(
            TestDocRefServiceImpl.class,
            TestDocRefEntity.class,
            TestQueryServiceImpl.class);

    @Override
    public void run(final Config configuration,
                    final Environment environment) throws Exception {
        environment.healthChecks().register("Something", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy("Keeps Dropwizard Happy");
            }
        });
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
}
