module stroom.query.jooq {
    requires stroom.docref;
    requires stroom.expression;
    requires stroom.query.api;
    requires stroom.query.audit;
    requires stroom.query.authorisation;
    requires stroom.query.common;

    requires dropwizard.db;
    requires dropwizard.flyway;
    requires com.google.guice;
    requires java.sql;
    requires slf4j.api;
    requires dropwizard.jooq;
    requires jooq;
    requires dropwizard.core;
    requires flyway.core;
}