package stroom.query.testing.generic;

import org.junit.Before;
import org.junit.ClassRule;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.generic.app.App;
import stroom.query.testing.generic.app.Config;
import stroom.query.testing.generic.app.TestDocRefEntity;
import stroom.query.testing.generic.app.TestDocRefServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class TestDocRefResourceIT extends DocRefResourceIT<TestDocRefEntity, Config> {

    @ClassRule
    public static StroomAuthenticationRule authRule = new StroomAuthenticationRule();

    @ClassRule
    public static final DropwizardAppWithClientsRule<Config> appRule =
            new DropwizardAppWithClientsRule<>(App.class,
                    resourceFilePath("generic/config.yml"),
                    authRule.authService(),
                    authRule.authToken());

    public TestDocRefResourceIT() {
        super(TestDocRefEntity.TYPE,
                TestDocRefEntity.class,
                appRule,
                authRule);
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
