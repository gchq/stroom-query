package stroom.query.testing.jooq.app;

import stroom.query.jooq.DocRefServiceJooqImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class TestDocRefServiceJooqImpl extends DocRefServiceJooqImpl<TestDocRefJooqEntity, TestDocRefJooqEntity.Builder> {

    @Inject
    public TestDocRefServiceJooqImpl() {
        super(TestDocRefJooqEntity.class);
    }

    @Override
    protected TestDocRefJooqEntity.Builder createDocumentBuilder() {
        return new TestDocRefJooqEntity.Builder();
    }

    @Override
    protected TestDocRefJooqEntity.Builder copyEntity(final TestDocRefJooqEntity original) {
        return new TestDocRefJooqEntity.Builder()
                .planetName(original.getPlanetName());
    }

    @Override
    protected TestDocRefJooqEntity.Builder createImport(final Map<String, String> dataMap) {
        return new TestDocRefJooqEntity.Builder()
                .planetName(dataMap.get(TestDocRefJooqEntity.PLANET_NAME));
    }

    @Override
    protected Map<String, Object> exportValues(final TestDocRefJooqEntity entity) {
        return new HashMap<String, Object>()
        {
            {
                put(TestDocRefJooqEntity.PLANET_NAME, entity.getPlanetName());
            }
        };
    }

    @Override
    public String getType() {
        return TestDocRefJooqEntity.TYPE;
    }
}
