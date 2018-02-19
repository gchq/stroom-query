package stroom.query.testing.jooq.app;

import org.jooq.Field;
import stroom.query.jooq.DocRefJooqEntity;
import stroom.query.jooq.JooqEntity;

import java.util.Objects;

import static org.jooq.impl.DSL.field;

@JooqEntity(tableName="test_jooq_doc_ref")
public class TestDocRefJooqEntity extends DocRefJooqEntity {
    public static final String TYPE = "TestDocRefJooqEntity";

    public static final String PLANET_NAME = "planetName";
    public static final Field<String> PLANET_NAME_FIELD = field(PLANET_NAME, String.class);

    private String planetName;

    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TestDocRefJooqEntity that = (TestDocRefJooqEntity) o;
        return Objects.equals(planetName, that.planetName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), planetName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestDocRefJooqEntity{");
        sb.append("super='").append(super.toString()).append('\'');
        sb.append("planetName='").append(planetName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder
            extends BaseBuilder<TestDocRefJooqEntity, Builder> {

        public Builder(final TestDocRefJooqEntity instance) {
            super(instance);
        }

        public Builder() {
            super(new TestDocRefJooqEntity());
        }

        public Builder planetName(final String value) {
            this.instance.planetName = value;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
