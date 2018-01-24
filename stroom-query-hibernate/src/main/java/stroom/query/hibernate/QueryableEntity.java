package stroom.query.hibernate;

import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.audit.service.DocRefEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

@MappedSuperclass
public class QueryableEntity implements Serializable {
    public static final String DATA_SOURCE_UUID = "dataSourceUuid";

    private String dataSourceUuid;

    private Long createTime;
    private Long updateTime;
    private String createUser;
    private String updateUser;

    @Id
    @Column(name= DATA_SOURCE_UUID)
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

    @Column(name=DocRefEntity.CREATE_TIME)
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

    @Column(name=DocRefEntity.UPDATE_TIME)
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
                    .type(DataSourceField.DataSourceFieldType.FIELD)
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

    @Column(name=DocRefEntity.CREATE_USER)
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
                    .type(DataSourceField.DataSourceFieldType.FIELD)
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

    @Column(name=DocRefEntity.UPDATE_USER)
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

    public static abstract class Builder<T extends QueryableEntity, CHILD_CLASS extends Builder<T, ?>> {

        protected final T instance;

        protected Builder(final T instance) {
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
}
