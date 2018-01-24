package stroom.query.testing.generic;

import org.junit.Before;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.generic.app.App;
import stroom.query.testing.generic.app.Config;
import stroom.query.testing.generic.app.TestDocRefEntity;
import stroom.query.testing.generic.app.TestDocRefServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestDocRefResourceIT extends DocRefResourceIT<TestDocRefEntity, Config, App> {
    public TestDocRefResourceIT() {
        super(App.class, TestDocRefEntity.class, TestDocRefEntity.TYPE, "generic/config.yml");
    }

    @Override
    protected TestDocRefEntity createPopulatedEntity() {
        return new TestDocRefEntity.Builder()
                .indexName(UUID.randomUUID().toString())
                .build();
    }

    @Override
    protected Map<String, String> exportValues(final TestDocRefEntity docRefEntity) {
        final Map<String, String> values = new HashMap<>();
        values.put(TestDocRefEntity.INDEX_NAME, docRefEntity.getIndexName());
        return values;
    }

    @Before
    public void beforeTest() {
        TestDocRefServiceImpl.eraseAllData();
    }
}
