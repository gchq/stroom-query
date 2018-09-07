module stroom.query.api {
    requires stroom.docref;

    requires com.fasterxml.jackson.annotation;
    requires java.xml.bind;
    requires swagger.annotations;

    exports stroom.datasource.api.v2;
    exports stroom.query.api.v2;
}