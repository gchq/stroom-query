package stroom.query.jooq;

import io.dropwizard.flyway.FlywayFactory;

public interface HasFlywayFactory {
    FlywayFactory getFlywayFactory();
}
