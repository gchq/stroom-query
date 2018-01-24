package stroom.query.testing.generic.app;

import stroom.query.audit.service.DocRefEntity;

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
