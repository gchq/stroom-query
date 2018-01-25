package stroom.query.testing.hibernate;

import org.junit.Before;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestHibernateDocRefResourceIT extends DocRefResourceIT<TestDocRefHibernateEntity, HibernateConfig, HibernateApp> {
    public TestHibernateDocRefResourceIT() {
        super(HibernateApp.class, TestDocRefHibernateEntity.class, TestDocRefHibernateEntity.TYPE, "hibernate/config.yml");
    }

    @Override
    protected TestDocRefHibernateEntity createPopulatedEntity() {
        return new TestDocRefHibernateEntity.Builder()
                .indexName(UUID.randomUUID().toString())
                .build();
    }

    @Override
    protected Map<String, String> exportValues(final TestDocRefHibernateEntity docRefEntity) {
        final Map<String, String> values = new HashMap<>();
        values.put(TestDocRefHibernateEntity.INDEX_NAME, docRefEntity.getIndexName());
        return values;
    }

    @Before
    public void beforeTest() {
        // TestDocRefServiceImpl.eraseAllData();
    }
}
