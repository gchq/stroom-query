module stroom.query.jooq {
    requires stroom.docref;
    requires stroom.expression;
    requires stroom.query.api;
    requires stroom.query.audit;
    requires stroom.query.authorisation;
    requires stroom.query.common;

    requires dropwizard.db;
    requires dropwizard.flyway;
    requires guava;
    requires java.sql;
//    requires slf4j.api;
}