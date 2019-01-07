package stroom.query.testing.hibernate;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DatabaseContainerExtension;
import stroom.query.testing.DatabaseContainerExtensionSupport;
import stroom.query.testing.DocRefResourceNoAuthIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DatabaseContainerExtensionSupport.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class TestHibernateDocRefResourceNoAuthIT extends DocRefResourceNoAuthIT<TestDocRefHibernateEntity, HibernateConfig> {

    private static final DatabaseContainerExtension dbRule = new DatabaseContainerExtension();
    private static final DropwizardAppExtensionWithClients<HibernateConfig> appRule =
            new DropwizardAppExtensionWithClients<>(HibernateApp.class, resourceFilePath("hibernate_noauth/config.yml"));

    TestHibernateDocRefResourceNoAuthIT() {
        super(TestDocRefHibernateEntity.class,
                appRule);
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
