module stroom.query.api {
    requires stroom.docref;
    requires java.xml.bind;
    requires swagger.annotations;
    requires com.fasterxml.jackson.annotation;

    exports stroom.query.api.v2;
    exports stroom.datasource.api.v2;
}