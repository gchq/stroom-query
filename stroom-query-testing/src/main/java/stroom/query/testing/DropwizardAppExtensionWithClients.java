/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.testing;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;

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
 *
 * @param <C> The dropwizard configuration class.
 */
public class DropwizardAppExtensionWithClients<C extends Configuration> extends DropwizardAppExtension<C> {
    private final List<Closeable> clients = new ArrayList<>();

//    public DropwizardAppExtensionWithClients(final Class<? extends Application<C>> applicationClass) {
//        super(applicationClass, (String) null);
//    }

    public DropwizardAppExtensionWithClients(final Class<? extends Application<C>> applicationClass,
                                             final @Nullable String configPath,
                                             final ConfigOverride... configOverrides) {
        super(applicationClass, configPath, Optional.empty(), configOverrides);
    }

//    public DropwizardAppExtensionWithClients(final Class<? extends Application<C>> applicationClass,
//                                             final String configPath,
//                                             final Optional<String> customPropertyPrefix,
//                                             final ConfigOverride... configOverrides) {
//        super(applicationClass, configPath, customPropertyPrefix, ServerCommand::new, configOverrides);
//    }
//
//    public DropwizardAppExtensionWithClients(final Class<? extends Application<C>> applicationClass,
//                                             final String configPath,
//                                             final Optional<String> customPropertyPrefix,
//                                             final Function<Application<C>, Command> commandInstantiator,
//                                             final ConfigOverride... configOverrides) {
//        super(new DropwizardTestSupport<>(applicationClass, configPath, customPropertyPrefix, commandInstantiator,
//                configOverrides));
//    }

    public DropwizardAppExtensionWithClients(Class<? extends Application<C>> applicationClass, C configuration) {
        super(applicationClass, configuration);
    }

    @Override
    public void before() {
        super.before();
    }

    @Override
    public void after() {
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
