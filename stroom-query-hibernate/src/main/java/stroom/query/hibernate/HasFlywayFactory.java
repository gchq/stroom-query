package stroom.query.hibernate;

import io.dropwizard.flyway.FlywayFactory;

public interface HasFlywayFactory {
    FlywayFactory getFlywayFactory();
}
