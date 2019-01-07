package stroom.query.testing.memory.app;

import stroom.query.audit.model.DocRefEntity;

import java.util.Objects;

public class TestDocRefEntity extends DocRefEntity {
    public static final String TYPE = "TestDocRefEntity";

    public static final String INDEX_NAME = "IndexName";

    private String indexName;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TestDocRefEntity that = (TestDocRefEntity) o;
        return Objects.equals(indexName, that.indexName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), indexName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDocRefEntity{");
        sb.append("super='").append(super.toString()).append('\'');
        sb.append("indexName='").append(indexName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder
            extends DocRefEntity.BaseBuilder<TestDocRefEntity, Builder> {

        public Builder(final TestDocRefEntity instance) {
            super(instance);
        }

        public Builder() {
            super(new TestDocRefEntity());
        }

        public Builder indexName(final String value) {
            this.instance.indexName = value;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
