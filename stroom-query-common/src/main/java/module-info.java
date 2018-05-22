module stroom.query.common {
    exports stroom.mapreduce.v2;
    exports stroom.query.common.v2;
    requires java.xml.bind;
    requires slf4j.api;
    requires guava;
    requires stroom.expression;
    requires stroom.docref;
    requires stroom.query.api;
}