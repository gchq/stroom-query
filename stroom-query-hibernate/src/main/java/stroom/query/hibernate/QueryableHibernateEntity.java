package stroom.query.hibernate;

import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.model.QueryableEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class QueryableHibernateEntity extends QueryableEntity {

    @Id
    @Column(name=DATA_SOURCE_UUID)
    public String getDataSourceUuid() {
        return super.getDataSourceUuid();
    }

    @Column(name=DocRefEntity.CREATE_TIME)
    public Long getCreateTime() {
        return super.getCreateTime();
    }

    @Column(name=DocRefEntity.UPDATE_TIME)
    public Long getUpdateTime() {
        return super.getUpdateTime();
    }

    @Column(name=DocRefEntity.CREATE_USER)
    public String getCreateUser() {
        return super.getCreateUser();
    }

    @Column(name=DocRefEntity.UPDATE_USER)
    public String getUpdateUser() {
        return super.getUpdateUser();
    }


    public static class Builder<T extends QueryableHibernateEntity> extends QueryableEntity.BaseBuilder<T, Builder<T>> {

        public Builder(final T instance) {
            super(instance);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
