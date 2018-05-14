package stroom.query.testing.jooq;

import org.junit.Before;
import org.junit.ClassRule;
import stroom.query.testing.DocRefResourceNoAuthIT;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;

import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class TestJooqDocRefResourceNoAuthIT extends DocRefResourceNoAuthIT<TestDocRefJooqEntity, JooqConfig> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<JooqConfig> appRule =
            new DropwizardAppWithClientsRule<>(JooqApp.class, resourceFilePath("jooq_noauth/config.yml"));

    public TestJooqDocRefResourceNoAuthIT() {
        super(TestDocRefJooqEntity.class,
                appRule);
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
