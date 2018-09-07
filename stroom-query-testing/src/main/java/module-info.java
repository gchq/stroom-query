module stroom.query.testing {
    requires stroom.docref;
    requires stroom.query.api;
    requires stroom.query.audit;
    requires stroom.query.authorisation;

    requires com.google.guice;
    requires dropwizard.core;
    requires dropwizard.testing;
    requires junit;
    requires org.junit.jupiter.api;
}