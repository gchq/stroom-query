module stroom.query.common {
    requires stroom.docref;
    requires stroom.expression;
    requires stroom.query.api;

    requires java.xml.bind;
    requires slf4j.api;

    exports stroom.mapreduce.v2;
    exports stroom.query.common.v2;
}