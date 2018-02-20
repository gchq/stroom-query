package stroom.query.jooq;

import org.jooq.Field;
import stroom.query.audit.model.QueryableEntity;

import static org.jooq.impl.DSL.field;

public class QueryableJooqEntity extends QueryableEntity {
    public static final Field<String> DATA_SOURCE_UUID_FIELD = field(DATA_SOURCE_UUID, String.class);
}
