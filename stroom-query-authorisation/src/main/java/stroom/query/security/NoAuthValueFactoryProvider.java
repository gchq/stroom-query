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

package stroom.query.security;

import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthValueFactoryProvider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.Principal;

/**
 * A cut down copy of the JWT based Auth value provider, it simply logs in as 'admin'
 * for every resource call. Used when auth it to be 'turned off', mainly for testing without using
 * an available auth service.
 */
public class NoAuthValueFactoryProvider extends AbstractValueFactoryProvider {

    public static final ServiceUser ADMIN_USER = new ServiceUser.Builder()
            .name("admin")
            .jwt("d4111b8f-d4e9-4545-806e-9f0508414ee0")
            .build();

    /**
     * {@link Principal} value factory provider injection constructor.
     *
     * @param mpep                   multivalued parameter extractor provider
     * @param injector               injector instance
     */
    @Inject
    public NoAuthValueFactoryProvider(final MultivaluedParameterExtractorProvider mpep,
                                      final ServiceLocator injector) {
        super(mpep, injector, Parameter.Source.UNKNOWN);
    }

    @Override
    protected Factory<ServiceUser> createValueFactory(Parameter parameter) {
        return new Factory<ServiceUser>() {
            @Override
            public ServiceUser provide() {
                return ADMIN_USER;
            }

            @Override
            public void dispose(ServiceUser o) {

            }
        };
    }

    @Singleton
    static class NoAuthInjectionResolver extends ParamInjectionResolver<Auth> {

        /**
         * Create new {@link Auth} annotation injection resolver.
         */
        NoAuthInjectionResolver() {
            super(AuthValueFactoryProvider.class);
        }
    }

    /**
     * Injection binder for {@link NoAuthValueFactoryProvider} and {@link NoAuthValueFactoryProvider.NoAuthInjectionResolver}.
     */
    public static class Binder extends AbstractBinder {

        public Binder() {
        }

        @Override
        protected void configure() {
            bind(NoAuthValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(NoAuthInjectionResolver.class).to(new TypeLiteral<InjectionResolver<Auth>>() {}).in(Singleton.class);
        }
    }
}
