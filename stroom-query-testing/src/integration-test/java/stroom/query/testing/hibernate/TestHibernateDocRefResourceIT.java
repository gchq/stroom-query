package stroom.query.testing.hibernate;


import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.StroomAuthenticationExtension;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

class TestHibernateDocRefResourceIT extends DocRefResourceIT<TestDocRefHibernateEntity, HibernateConfig> {
    private static StroomAuthenticationExtension authRule = new StroomAuthenticationExtension();

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
}
