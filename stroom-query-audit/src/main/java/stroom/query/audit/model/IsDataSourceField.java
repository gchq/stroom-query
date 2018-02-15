package stroom.query.audit.model;

import stroom.datasource.api.v2.DataSourceField;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * Used alongside JPA Column annotations to indicate that a field should be exposed via the Stroom Query API
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsDataSourceField {
    Class<? extends Supplier<DataSourceField>> fieldSupplier();


}
