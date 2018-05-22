package stroom.query.testing;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.junit.DropwizardAppRule;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

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

    private final List<Closeable> clients = new ArrayList<>();

    @Override
    protected void after() {
        super.after();

        for (Closeable client : clients) {
            try {
                client.close();
            } catch (final IOException e) {
                fail("Could not close a client: " + e.getLocalizedMessage());
            }
        }
    }

    public <CLIENT extends Closeable> CLIENT getClient(final Function<String, CLIENT> supplier) {
        final CLIENT client = supplier.apply(String.format("http://localhost:%d", getLocalPort()));
        clients.add(client);
        return client;
    }
}
