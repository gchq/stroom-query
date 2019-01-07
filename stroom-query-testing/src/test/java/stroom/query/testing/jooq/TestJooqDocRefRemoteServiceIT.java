package stroom.query.testing.jooq;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DocRefRemoteServiceIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.StroomAuthenticationExtension;
import stroom.query.testing.StroomAuthenticationExtensionSupport;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(StroomAuthenticationExtensionSupport.class)
class TestJooqDocRefRemoteServiceIT extends DocRefRemoteServiceIT<TestDocRefJooqEntity, JooqConfig> {
    private static StroomAuthenticationExtension authRule = new StroomAuthenticationExtension();

    private static final DropwizardAppExtensionWithClients<JooqConfig> appRule =
            new DropwizardAppExtensionWithClients<>(JooqApp.class,
                    resourceFilePath("jooq/config.yml"),
                    authRule.authToken(),
                    authRule.authService());

    TestJooqDocRefRemoteServiceIT() {
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
}
