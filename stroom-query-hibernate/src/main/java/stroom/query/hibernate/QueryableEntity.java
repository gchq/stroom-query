package stroom.query.hibernate;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public class QueryableEntity implements Serializable {
    public static final String DATA_SOURCE_UUID = "dataSourceUuid";

    private String dataSourceUuid;

    @Id
    @Column(name= DATA_SOURCE_UUID)
    public String getDataSourceUuid() {
        return dataSourceUuid;
    }

    public void setDataSourceUuid(String dataSourceUuid) {
        this.dataSourceUuid = dataSourceUuid;
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

        public Builder dataSourceUuid(final String value) {
            this.instance.setDataSourceUuid(value);
            return self();
        }

        public T build() {
            return instance;
        }

        protected abstract CHILD_CLASS self();
    }
}
