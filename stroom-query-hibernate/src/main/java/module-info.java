module stroom.query.hibernate {
    requires com.google.guice;
    requires dropwizard.core;
    requires dropwizard.db;
    requires dropwizard.flyway;
    requires dropwizard.hibernate;
    requires flyway.core;
    requires hibernate.core;
    requires slf4j.api;
    requires stroom.query.audit;
    requires stroom.query.authorisation;
    requires stroom.expression;
    requires stroom.query.api;
    requires stroom.query.common;
    requires hibernate.jpa;
    requires java.sql;
    requires java.naming;
    requires javax.inject;
}