package stroom.query.audit.model;

import stroom.datasource.api.v2.DataSourceField;
import stroom.datasource.api.v2.DataSourceField.DataSourceFieldType;
import stroom.query.api.v2.ExpressionTerm;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class QueryableEntity implements Serializable {

    /**
     * Climbs the super-class chain to find all instances of an annotation on any method.
     *
     * @param clazz             The class to search through, will be called recursively
     * @return A stream of Annotations
     */
    public static Stream<IsDataSourceField> getFields(final Class clazz) {
        if (!Object.class.equals(clazz)) {
            final Stream<IsDataSourceField> onThis = Stream.of(clazz.getMethods())
                    .map(m -> m.getAnnotation(IsDataSourceField.class))
                    .filter(Objects::nonNull);

            final Stream<IsDataSourceField> inherited = getFields(clazz.getSuperclass());

            return Stream.concat(onThis, inherited);
        } else {
            return Stream.empty();
        }
    }

    /**
     * Used for injection based on templated services.
     *
     * @param <T> The specific implementation of QueryableEntity used in the application
     */
    public static class ClassProvider<T extends QueryableEntity> implements Supplier<Class<T>> {

        private final Class<T> clazz;

        public ClassProvider(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Class<T> get() {
            return this.clazz;
        }
    }

    public static final String DATA_SOURCE_UUID = "dataSourceUuid";

    private String dataSourceUuid;

    private Long createTime;
    private Long updateTime;
    private String createUser;
    private String updateUser;

    public String getDataSourceUuid() {
        return dataSourceUuid;
    }

    public void setDataSourceUuid(String dataSourceUuid) {
        this.dataSourceUuid = dataSourceUuid;
    }

    public static class CreateTimeField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.DATE_FIELD)
                    .name(DocRefEntity.CREATE_TIME)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.BETWEEN,
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.GREATER_THAN,
                            ExpressionTerm.Condition.GREATER_THAN_OR_EQUAL_TO,
                            ExpressionTerm.Condition.LESS_THAN,
                            ExpressionTerm.Condition.LESS_THAN_OR_EQUAL_TO
                    ).build();
        }
    }

    @IsDataSourceField(fieldSupplier = CreateTimeField.class)
    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public static class UpdateTimeField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField.Builder()
                    .type(DataSourceField.DataSourceFieldType.DATE_FIELD)
                    .name(DocRefEntity.UPDATE_TIME)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.BETWEEN,
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.GREATER_THAN,
                            ExpressionTerm.Condition.GREATER_THAN_OR_EQUAL_TO,
                            ExpressionTerm.Condition.LESS_THAN,
                            ExpressionTerm.Condition.LESS_THAN_OR_EQUAL_TO
                    ).build();
        }
    }

    @IsDataSourceField(fieldSupplier = UpdateTimeField.class)
    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public static class CreateUserField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField.Builder()
                    .type(DataSourceFieldType.TEXT_FIELD)
                    .name(DocRefEntity.CREATE_USER)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.CONTAINS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

    @IsDataSourceField(fieldSupplier = CreateUserField.class)
    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public static class UpdateUserField implements Supplier<DataSourceField> {

        @Override
        public DataSourceField get() {
            return new DataSourceField.Builder()
                    .type(DataSourceFieldType.TEXT_FIELD)
                    .name(DocRefEntity.UPDATE_USER)
                    .queryable(true)
                    .addConditions(
                            ExpressionTerm.Condition.EQUALS,
                            ExpressionTerm.Condition.CONTAINS,
                            ExpressionTerm.Condition.IN,
                            ExpressionTerm.Condition.IN_DICTIONARY
                    ).build();
        }
    }

    @IsDataSourceField(fieldSupplier = UpdateUserField.class)
    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryableEntity that = (QueryableEntity) o;
        return Objects.equals(dataSourceUuid, that.dataSourceUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSourceUuid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryableEntity{");
        sb.append("dataSourceUuid='").append(dataSourceUuid).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static abstract class BaseBuilder<T extends QueryableEntity, CHILD_CLASS extends BaseBuilder<T, ?>> {

        protected final T instance;

        protected BaseBuilder(final T instance) {
            this.instance = instance;
        }

        public CHILD_CLASS dataSourceUuid(final String value) {
            this.instance.setDataSourceUuid(value);
            return self();
        }
        public CHILD_CLASS createUser(final String value) {
            this.instance.setCreateUser(value);
            return self();
        }

        public CHILD_CLASS createTime(final Long value) {
            this.instance.setCreateTime(value);
            return self();
        }

        public CHILD_CLASS updateUser(final String value) {
            this.instance.setUpdateUser(value);
            return self();
        }

        public CHILD_CLASS updateTime(final Long value) {
            this.instance.setUpdateTime(value);
            return self();
        }

        public T build() {
            return instance;
        }

        protected abstract CHILD_CLASS self();
    }

    public static class Builder<T extends QueryableEntity> extends BaseBuilder<T, Builder<T>> {

        public Builder(final T instance) {
            super(instance);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
