module stroom.query.audit {
    requires event.logging.api;
    requires stroom.docref;
    requires stroom.query.api;
    requires stroom.query.authorisation;
    requires stroom.query.common;

    requires javax.ws.rs.api;

    exports stroom.query.audit;
    exports stroom.query.audit.client;
    exports stroom.query.audit.logback;
    exports stroom.query.audit.model;
    exports stroom.query.audit.rest;
    exports stroom.query.audit.service;
}