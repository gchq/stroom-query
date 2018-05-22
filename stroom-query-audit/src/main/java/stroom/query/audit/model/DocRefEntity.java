package stroom.query.audit.model;

import stroom.query.api.v2.DocRef;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

public class DocRefEntity implements Serializable {

    /**
     * Used for injection based on templated services.
     *
     * @param <T> The specific implementation of QueryableEntity used in the application
     */
    public static class ClassProvider<T extends DocRefEntity> implements Supplier<Class<T>> {

        private final Class<T> clazz;

        public ClassProvider(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Class<T> get() {
            return this.clazz;
        }
    }

    public static final String UUID = "uuid";
    public static final String NAME = "name";
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String CREATE_USER = "createUser";
    public static final String UPDATE_USER = "updateUser";

    // This is the UUID of the DocRef within Stroom
    private String uuid;

    private String type;

    // This is the name of the DocRef within Stroom
    private String name;

    private Long createTime;
    private Long updateTime;
    private String createUser;
    private String updateUser;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

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
        DocRefEntity that = (DocRefEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryableEntity{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static abstract class BaseBuilder<T extends DocRefEntity, CHILD_CLASS extends BaseBuilder<T, ?>> {

        protected final T instance;

        protected BaseBuilder(final T instance) {
            this.instance = instance;
        }

        public CHILD_CLASS docRef(final DocRef docRef) {
            this.instance.setUuid(docRef.getUuid());
            this.instance.setType(docRef.getType());
            this.instance.setName(docRef.getName());
            return self();
        }

        public CHILD_CLASS uuid(final String value) {
            this.instance.setUuid(value);
            return self();
        }

        public CHILD_CLASS type(final String value) {
            this.instance.setType(value);
            return self();
        }

        public CHILD_CLASS name(final String value) {
            this.instance.setName(value);
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

        public CHILD_CLASS original(final T original) {
            this.instance.setUuid(original.getUuid());
            this.instance.setType(original.getType());
            this.instance.setName(original.getName());
            this.instance.setCreateUser(original.getCreateUser());
            this.instance.setCreateTime(original.getCreateTime());
            this.instance.setUpdateUser(original.getUpdateUser());
            this.instance.setUpdateTime(original.getUpdateTime());
            return self();
        }

        public T build() {
            return instance;
        }

        protected abstract CHILD_CLASS self();
    }

    public static class Builder<T extends DocRefEntity> extends BaseBuilder<T, Builder<T>> {

        public Builder(final T instance) {
            super(instance);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
