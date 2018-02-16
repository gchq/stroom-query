package stroom.query.testing.hibernate.app;

import stroom.query.hibernate.DocRefHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name="test_hibernate_doc_ref")
public class TestDocRefHibernateEntity extends DocRefHibernateEntity {
    public static final String TYPE = "TestDocRefHibernateEntity";

    public static final String CLAN_NAME = "clanName";

    private String clanName;

    @Column(name=CLAN_NAME)
    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public static final class Builder
            extends BaseBuilder<TestDocRefHibernateEntity, Builder> {

        public Builder(final TestDocRefHibernateEntity instance) {
            super(instance);
        }

        public Builder() {
            super(new TestDocRefHibernateEntity());
        }

        public Builder clanName(final String value) {
            this.instance.clanName = value;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
