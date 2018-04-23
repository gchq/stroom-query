package stroom.query.testing.jooq;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Before;
import org.junit.ClassRule;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class TestJooqDocRefResourceIT extends DocRefResourceIT<TestDocRefJooqEntity, JooqConfig> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<JooqConfig> appRule =
            new DropwizardAppWithClientsRule<>(JooqApp.class, resourceFilePath("jooq/config.yml"));

    @ClassRule
    public static StroomAuthenticationRule authRule =
            new StroomAuthenticationRule(WireMockConfiguration.options().port(10080));

    public TestJooqDocRefResourceIT() {
        super(TestDocRefJooqEntity.TYPE,
                TestDocRefJooqEntity.class,
                appRule,
                authRule);
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

    @Before
    public void beforeTest() {
        // TestDocRefServiceImpl.eraseAllData();
    }
}
