package stroom.query.testing.jooq.app;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.UpdateSetMoreStep;
import stroom.query.jooq.DocRefServiceJooqImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static stroom.query.testing.jooq.app.TestDocRefJooqEntity.PLANET_NAME_FIELD;

public class TestDocRefServiceJooqImpl extends DocRefServiceJooqImpl<TestDocRefJooqEntity, TestDocRefJooqEntity.Builder> {

    @Inject
    public TestDocRefServiceJooqImpl(final Configuration jooqConfiguration) {
        super("test_jooq_doc_ref", jooqConfiguration);
    }

    @Override
    protected TestDocRefJooqEntity.Builder createDocumentBuilder(final Record record) {
        return new TestDocRefJooqEntity.Builder()
                .planetName(record.getValue(PLANET_NAME_FIELD));
    }

    @Override
    protected Map<Field<?>, Object> getMappedFields(final TestDocRefJooqEntity entity) {
        return new HashMap<Field<?>, Object>()
        {
            {
                put(PLANET_NAME_FIELD, entity.getPlanetName());
            }
        };
    }

    @Override
    public String getType() {
        return TestDocRefJooqEntity.TYPE;
    }
}
