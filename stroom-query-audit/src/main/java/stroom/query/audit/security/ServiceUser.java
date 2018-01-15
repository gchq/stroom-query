package stroom.query.audit.security;

import com.google.common.base.Preconditions;

import javax.validation.constraints.NotNull;
import java.security.Principal;

public final class ServiceUser implements Principal {

    @NotNull
    private String name;
    @NotNull
    private final String jwt;

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public final String getJwt() {
        return this.jwt;
    }

    public ServiceUser(@NotNull String name, @NotNull String jwt) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(jwt);
        this.name = name;
        this.jwt = jwt;
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

