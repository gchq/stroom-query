package stroom.query.testing.hibernate.app;

import stroom.query.hibernate.DocRefHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name="test_doc_ref")
public class TestDocRefHibernateEntity extends DocRefHibernateEntity {
    public static final String TYPE = "TestDocRefHibernateEntity";

    public static final String INDEX_NAME = "indexName";

    private String indexName;

    @Column(name=INDEX_NAME)
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public static final class Builder
            extends BaseBuilder<TestDocRefHibernateEntity, Builder> {

        public Builder(final TestDocRefHibernateEntity instance) {
            super(instance);
        }

        public Builder() {
            super(new TestDocRefHibernateEntity());
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
