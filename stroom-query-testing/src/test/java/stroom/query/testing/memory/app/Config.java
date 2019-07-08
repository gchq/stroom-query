package stroom.query.testing.memory.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import stroom.query.authorisation.AuthorisationServiceConfig;
import stroom.query.authorisation.HasAuthorisationConfig;
import stroom.query.security.HasTokenConfig;
import stroom.query.security.TokenConfig;

public class Config extends Configuration implements HasAuthorisationConfig, HasTokenConfig {
    @JsonProperty("token")
    private TokenConfig tokenConfig;

    @JsonProperty("authorisationService")
    private AuthorisationServiceConfig authorisationServiceConfig;

    @Override
    public TokenConfig getTokenConfig() {
        return tokenConfig;
    }

    @Override
    public AuthorisationServiceConfig getAuthorisationServiceConfig() {
        return authorisationServiceConfig;
    }
}
