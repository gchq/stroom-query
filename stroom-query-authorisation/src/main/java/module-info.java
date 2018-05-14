module stroom.query.authorisation {
    exports stroom.query.authorisation;
    exports stroom.query.security;
    requires stroom.query.api;
    requires jetty.http;
    requires javax.ws.rs.api;
    requires jersey.client;
    requires slf4j.api;
    requires javax.inject;
    requires dropwizard.auth;
    requires hk2.api;
    requires jersey.server;
    requires dropwizard.auth.jwt;
    requires jose4j;
}