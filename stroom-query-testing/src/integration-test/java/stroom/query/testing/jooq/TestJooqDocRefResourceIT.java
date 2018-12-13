package stroom.query.testing.jooq;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
class TestJooqDocRefResourceIT extends DocRefResourceIT<TestDocRefJooqEntity, JooqConfig> {
    private static StroomAuthenticationRule authRule =
            new StroomAuthenticationRule();

    private static final DropwizardAppExtensionWithClients<JooqConfig> appRule =
            new DropwizardAppExtensionWithClients<>(JooqApp.class,
                    resourceFilePath("jooq/config.yml"),
                    authRule.authToken(),
                    authRule.authService());

    TestJooqDocRefResourceIT() {
        super(TestDocRefJooqEntity.TYPE,
                TestDocRefJooqEntity.class,
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
        authRule.after();
        authRule.stop();
    }

    @Override
    protected TestDocRefJooqEntity createPopulatedEntity() {
        return new TestDocRefJooqEntity.Builder()
                .planetName(UUID.randomUUID().toString())
                .build();
    }

    @Override
    protected Map<String, String> exportValues(final TestDocRefJooqEntity docRefEntity) {
        final Map<String, String> values = new HashMap<>();
        values.put(TestDocRefJooqEntity.PLANET_NAME, docRefEntity.getPlanetName());
        return values;
    }

    @BeforeEach
    void beforeTest() {
        // TestDocRefServiceImpl.eraseAllData();
    }
}
