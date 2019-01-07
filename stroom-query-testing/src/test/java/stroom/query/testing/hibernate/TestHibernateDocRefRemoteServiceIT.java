package stroom.query.testing.hibernate;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DocRefRemoteServiceIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.StroomAuthenticationExtension;
import stroom.query.testing.StroomAuthenticationExtensionSupport;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(StroomAuthenticationExtensionSupport.class)
class TestHibernateDocRefRemoteServiceIT extends DocRefRemoteServiceIT<TestDocRefHibernateEntity, HibernateConfig> {
    private static StroomAuthenticationExtension authRule = new StroomAuthenticationExtension();

    private static final DropwizardAppExtensionWithClients<HibernateConfig> appRule =
            new DropwizardAppExtensionWithClients<>(HibernateApp.class,
                    resourceFilePath("hibernate/config.yml"),
                    authRule.authToken(),
                    authRule.authService());


    TestHibernateDocRefRemoteServiceIT() {
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