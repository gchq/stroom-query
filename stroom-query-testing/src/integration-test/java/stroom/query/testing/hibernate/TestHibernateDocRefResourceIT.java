package stroom.query.testing.hibernate;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
class TestHibernateDocRefResourceIT extends DocRefResourceIT<TestDocRefHibernateEntity, HibernateConfig> {
    private static StroomAuthenticationRule authRule = new StroomAuthenticationRule();

    private static final DropwizardAppExtensionWithClients<HibernateConfig> appRule =
            new DropwizardAppExtensionWithClients<>(HibernateApp.class,
                    resourceFilePath("hibernate/config.yml"),
                    authRule.authToken(),
                    authRule.authService());

    TestHibernateDocRefResourceIT() {
        super(TestDocRefHibernateEntity.TYPE,
                TestDocRefHibernateEntity.class,
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
    protected TestDocRefHibernateEntity createPopulatedEntity() {
        return new TestDocRefHibernateEntity.Builder()
                .clanName(UUID.randomUUID().toString())
                .build();
    }

    @Override
    protected Map<String, String> exportValues(final TestDocRefHibernateEntity docRefEntity) {
        final Map<String, String> values = new HashMap<>();
        values.put(TestDocRefHibernateEntity.CLAN_NAME, docRefEntity.getClanName());
        return values;
    }

    @BeforeEach
    void beforeTest() {
        // TestDocRefServiceImpl.eraseAllData();
    }
}
