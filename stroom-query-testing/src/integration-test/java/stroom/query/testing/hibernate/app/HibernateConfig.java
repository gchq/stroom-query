package stroom.query.testing.hibernate.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import stroom.query.authorisation.AuthorisationServiceConfig;
import stroom.query.authorisation.HasAuthorisationConfig;
import stroom.query.security.HasTokenConfig;
import stroom.query.security.TokenConfig;
import stroom.query.hibernate.HasDataSourceFactory;
import stroom.query.hibernate.HasFlywayFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HibernateConfig extends Configuration implements HasAuthorisationConfig, HasTokenConfig, HasFlywayFactory, HasDataSourceFactory {
    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty("flyway")
    private FlywayFactory flywayFactory = new FlywayFactory();

    @NotNull
    @JsonProperty("token")
    private TokenConfig tokenConfig;

    @NotNull
    @JsonProperty("authorisationService")
    private AuthorisationServiceConfig authorisationServiceConfig;

    public final DataSourceFactory getDataSourceFactory() {
        return this.dataSourceFactory;
    }

    public final FlywayFactory getFlywayFactory() {
        return this.flywayFactory;
    }

    @Override
    public TokenConfig getTokenConfig() {
        return tokenConfig;
    }

    @Override
    public AuthorisationServiceConfig getAuthorisationServiceConfig() {
        return authorisationServiceConfig;
    }
}
