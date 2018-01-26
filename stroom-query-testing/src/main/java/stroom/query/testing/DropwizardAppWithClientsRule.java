package stroom.query.testing;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.junit.DropwizardAppRule;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/**
 * This rule allows tests to create client classes that requires the local app URL.
 * @param <C> The dropwizard configuration class.
 */
public class DropwizardAppWithClientsRule<C extends Configuration> extends DropwizardAppRule<C> {
    public DropwizardAppWithClientsRule(final Class<? extends Application<C>> applicationClass) {
        super(applicationClass, (String) null);
    }

    public DropwizardAppWithClientsRule(final Class<? extends Application<C>> applicationClass,
                                        final @Nullable String configPath,
                                        final ConfigOverride... configOverrides) {
        super(applicationClass, configPath, Optional.empty(), configOverrides);
    }

    public DropwizardAppWithClientsRule(final Class<? extends Application<C>> applicationClass,
                                        final String configPath,
                                        final Optional<String> customPropertyPrefix,
                                        final ConfigOverride... configOverrides) {
        super(applicationClass, configPath, customPropertyPrefix, ServerCommand::new, configOverrides);
    }

    public DropwizardAppWithClientsRule(final Class<? extends Application<C>> applicationClass,
                                        final String configPath,
                                        final Optional<String> customPropertyPrefix,
                                        final Function<Application<C>, Command> commandInstantiator,
                                        final ConfigOverride... configOverrides) {
        super(new DropwizardTestSupport<>(applicationClass, configPath, customPropertyPrefix, commandInstantiator,
                configOverrides));
    }

    public <CLIENT> CLIENT getClient(final Function<String, CLIENT> supplier) {
        return supplier.apply(String.format("http://localhost:%d", getLocalPort()));
    }
}
