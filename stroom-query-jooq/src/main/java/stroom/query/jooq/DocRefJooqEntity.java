package stroom.query.jooq;

import org.jooq.Field;
import org.jooq.types.ULong;
import stroom.query.audit.model.DocRefEntity;

import static org.jooq.impl.DSL.field;

public class DocRefJooqEntity extends DocRefEntity {
    public static final Field<String> UUID_FIELD = field(UUID, String.class);
    public static final Field<String> NAME_FIELD = field(NAME, String.class);
    public static final Field<ULong> CREATE_TIME_FIELD = field(CREATE_TIME, ULong.class);
    public static final Field<String> CREATE_USER_FIELD = field(CREATE_USER, String.class);
    public static final Field<ULong> UPDATE_TIME_FIELD = field(UPDATE_TIME, ULong.class);
    public static final Field<String> UPDATE_USER_FIELD = field(UPDATE_USER, String.class);
}
