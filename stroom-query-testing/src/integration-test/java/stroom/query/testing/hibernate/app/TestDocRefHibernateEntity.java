package stroom.query.testing.hibernate.app;

import stroom.query.hibernate.DocRefHibernateEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TestDocRefHibernateEntity that = (TestDocRefHibernateEntity) o;
        return Objects.equals(clanName, that.clanName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), clanName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDocRefHibernateEntity{");
        sb.append("super='").append(super.toString()).append('\'');
        sb.append("clanName='").append(clanName).append('\'');
        sb.append('}');
        return sb.toString();
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
