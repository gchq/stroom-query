package stroom.query.hibernate;

public class DocRefEntityDTO {
    private String uuid;

    private String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static abstract class ABuilder<T extends DocRefEntityDTO, E extends DocRefEntity, CHILD_CLASS extends ABuilder<T, E, ?>> {

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

        public CHILD_CLASS entity(final E value) {
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
