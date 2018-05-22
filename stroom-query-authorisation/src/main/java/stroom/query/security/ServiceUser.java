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

import java.security.Principal;
import java.util.Objects;

public final class ServiceUser implements Principal {

    private String name;
    private final String jwt;

    public String getName() {
        return this.name;
    }
    public final String getJwt() {
        return this.jwt;
    }

    public ServiceUser(final String name,
                       final String jwt) {
        this.name = Objects.requireNonNull(name);
        this.jwt = Objects.requireNonNull(jwt);
    }

    public static class Builder {
        private String name;

        private String jwt;

        public Builder() {

        }

        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        public Builder jwt(final String value) {
            this.jwt = value;
            return this;
        }

        public ServiceUser build() {
            return new ServiceUser(this.name, this.jwt);
        }
    }
}

