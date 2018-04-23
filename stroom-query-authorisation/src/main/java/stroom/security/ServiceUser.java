package stroom.security;

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

