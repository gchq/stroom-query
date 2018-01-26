package stroom.query.testing.generic;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.ClassRule;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.generic.app.App;
import stroom.query.testing.generic.app.Config;
import stroom.query.testing.generic.app.TestDocRefEntity;
import stroom.query.testing.generic.app.TestDocRefServiceImpl;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class TestDocRefResourceIT extends DocRefResourceIT<TestDocRefEntity, Config, App> {

    @ClassRule
    public static final DropwizardAppRule<Config> appRule =
            new DropwizardAppRule<>(App.class, resourceFilePath("generic/config.yml"));

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(
            WireMockConfiguration.options().port(10080));

    public TestDocRefResourceIT() {
        super(App.class, TestDocRefEntity.class, TestDocRefEntity.TYPE, appRule, wireMockRule);
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
