package stroom.query.testing.hibernate;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.ClassRule;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class TestHibernateDocRefResourceIT extends DocRefResourceIT<TestDocRefHibernateEntity, HibernateConfig, HibernateApp> {

    @ClassRule
    public static final DropwizardAppRule<HibernateConfig> appRule =
            new DropwizardAppRule<>(HibernateApp.class, resourceFilePath("hibernate/config.yml"));

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(
            WireMockConfiguration.options().port(10080));

    public TestHibernateDocRefResourceIT() {
        super(HibernateApp.class, TestDocRefHibernateEntity.class, TestDocRefHibernateEntity.TYPE, appRule, wireMockRule);
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
