module stroom.query.hibernate {
    requires stroom.docref;
    requires stroom.expression;
    requires stroom.query.api;
    requires stroom.query.audit;
    requires stroom.query.authorisation;
    requires stroom.query.common;

    requires com.google.guice;
//    requires hibernate.core;
//    requires hibernate.jpa;
    requires java.naming;
    requires java.sql;
}