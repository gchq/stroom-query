package stroom.query.testing.jooq.app;

import org.jooq.Configuration;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.jooq.DocRefServiceJooqImpl;

import javax.inject.Inject;

import static stroom.query.testing.jooq.app.TestDocRefJooqEntity.PLANET_NAME_FIELD;

public class TestDocRefServiceJooqImpl extends DocRefServiceJooqImpl<TestDocRefJooqEntity> {

    @Inject
    public TestDocRefServiceJooqImpl(final Configuration jooqConfiguration) {
        super(TestDocRefJooqEntity.TYPE,
                dataMap -> new TestDocRefJooqEntity.Builder()
                        .planetName(dataMap.getValue(PLANET_NAME_FIELD).orElse(null)),
                (entity, consumer) -> consumer.setValue(PLANET_NAME_FIELD, entity.getPlanetName()),
                TestDocRefJooqEntity.class,
                jooqConfiguration);
    }
}
