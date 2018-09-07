module stroom.query.authorisation {
    requires stroom.docref;

    requires com.fasterxml.jackson.annotation;
    requires dropwizard.auth;
    requires hk2.api;

    exports stroom.query.authorisation;
    exports stroom.query.security;
}