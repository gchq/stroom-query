package stroom.query.testing.generic;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.generic.app.App;
import stroom.query.testing.generic.app.Config;
import stroom.query.testing.generic.app.TestDocRefEntity;
import stroom.query.testing.generic.app.TestDocRefServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
class TestDocRefResourceIT extends DocRefResourceIT<TestDocRefEntity, Config> {
    private static StroomAuthenticationRule authRule = new StroomAuthenticationRule();

    private static final DropwizardAppExtensionWithClients<Config> appRule =
            new DropwizardAppExtensionWithClients<>(App.class,
                    resourceFilePath("generic/config.yml"),
                    authRule.authService(),
                    authRule.authToken());

    TestDocRefResourceIT() {
        super(TestDocRefEntity.TYPE,
                TestDocRefEntity.class,
                appRule,
                authRule);
    }

    @BeforeAll
    static void beforeAll() {
        authRule.start();
        authRule.before();
    }

    @AfterAll
    static void afterAll() {
        appRule.after();
        authRule.after();
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

    @BeforeEach
    void beforeTest() {
        TestDocRefServiceImpl.eraseAllData();
    }
}
