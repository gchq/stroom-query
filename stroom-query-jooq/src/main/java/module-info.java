module stroom.query.jooq {
    requires dropwizard.db;
    requires com.google.guice;
    requires dropwizard.core;
    requires stroom.query.audit;
    requires stroom.query.authorisation;
    requires stroom.query.api;
    requires slf4j.api;
    requires stroom.query.common;
    requires javax.inject;
    requires java.sql;
    requires jooq;
    requires stroom.expression;
    requires flyway.core;
    requires dropwizard.jooq;
    requires dropwizard.flyway;
}