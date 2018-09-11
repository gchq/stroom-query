module stroom.query.authorisation {
    requires stroom.docref;

    requires com.fasterxml.jackson.annotation;
    requires dropwizard.auth;
    requires dropwizard.auth.jwt;
    requires hk2.api;
    requires javax.inject;
    requires javax.ws.rs.api;
    requires jersey.client;
    requires jetty.http;
    requires jose4j;
    requires slf4j.api;

    exports stroom.query.authorisation;
    exports stroom.query.security;
}