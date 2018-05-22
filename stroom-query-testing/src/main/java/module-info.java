module stroom.query.testing {
    requires com.google.guice;
    requires stroom.docref;
    requires stroom.query.audit;
    requires junit;
    requires dropwizard.core;
    requires dropwizard.testing;
    requires jsr305;
    requires jetty.http;
    requires stroom.query.authorisation;
    requires stroom.query.api;
    requires javax.ws.rs.api;
    requires com.fasterxml.jackson.core;
    requires wiremock;
    requires jose4j;
    requires slf4j.api;
    requires com.fasterxml.jackson.databind;
    requires org.junit.jupiter.api;
}