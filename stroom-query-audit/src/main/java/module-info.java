module stroom.query.audit {
    requires event.logging.api;
    requires stroom.docref;
    requires stroom.query.api;
    requires stroom.query.authorisation;
    requires stroom.query.common;

    requires com.google.guice;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires dropwizard.auth;
    requires dropwizard.core;
    requires dropwizard.logging;
    requires javax.inject;
    requires javax.servlet.api;
    requires javax.ws.rs.api;
    requires jersey.client;
    requires jersey.server;
    requires jetty.http;
    requires kafka.clients;
    requires logback.core;
    requires logback.classic;
    requires metrics.annotation;
    requires slf4j.api;

    exports stroom.query.audit;
    exports stroom.query.audit.client;
    exports stroom.query.audit.logback;
    exports stroom.query.audit.model;
    exports stroom.query.audit.rest;
    exports stroom.query.audit.service;
}