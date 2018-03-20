package stroom.query.testing.hibernate;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Before;
import org.junit.ClassRule;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class TestHibernateDocRefResourceIT extends DocRefResourceIT<TestDocRefHibernateEntity, HibernateConfig> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<HibernateConfig> appRule =
            new DropwizardAppWithClientsRule<>(HibernateApp.class, resourceFilePath("hibernate/config.yml"));

    @ClassRule
    public static StroomAuthenticationRule authRule =
            new StroomAuthenticationRule(WireMockConfiguration.options().port(10080));

    public TestHibernateDocRefResourceIT() {
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

    @Before
    public void beforeTest() {
        // TestDocRefServiceImpl.eraseAllData();
    }
}
