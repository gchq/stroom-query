package stroom.query.testing.jooq.app;

import stroom.query.jooq.DocRefJooqEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name="test_doc_ref")
public class TestDocRefJooqEntity extends DocRefJooqEntity {
    public static final String TYPE = "TestDocRefJooqEntity";

    public static final String PLANET_NAME = "planetName";

    private String planetName;

    @Column(name=PLANET_NAME)
    public String getPlanetName() {
        return planetName;
    }

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
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
