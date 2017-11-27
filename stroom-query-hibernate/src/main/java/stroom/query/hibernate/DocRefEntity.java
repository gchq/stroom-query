package stroom.query.hibernate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public class DocRefEntity {
    public static final String UUID = "uuid";
    public static final String NAME = "name";

    // This is the UUID of the DocRef within Stroom
    private String uuid;

    // This is the name of the DocRef within Stroom
    private String name;

    @Id
    @Column(name=UUID)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Column(name=NAME)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public static abstract class ABuilder<T extends DocRefEntity, D extends DocRefEntityDTO, CHILD_CLASS extends ABuilder<T, D, ?>> {

        protected final T instance;

        protected ABuilder(final T instance) {
            this.instance = instance;
        }

        public CHILD_CLASS uuid(final String value) {
            this.instance.setUuid(value);
            return self();
        }

        public CHILD_CLASS name(final String value) {
            this.instance.setName(value);
            return self();
        }

        public CHILD_CLASS original(final T original) {
            this.instance.setUuid(original.getUuid());
            this.instance.setName(original.getName());
            return self();
        }

        public CHILD_CLASS dto(final D value) {
            this.instance.setUuid(value.getUuid());
            this.instance.setName(value.getName());
            return self();
        }

        public T build() {
            return instance;
        }

        protected abstract CHILD_CLASS self();
    }
}
