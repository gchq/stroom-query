package stroom.query.jooq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Doc Ref Entities and Queryable Entities that are used within the Audited Jooq Bundle must be annotated with this
 * so that the table name can be known by JOOQ
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JooqEntity {
    String tableName();
}
