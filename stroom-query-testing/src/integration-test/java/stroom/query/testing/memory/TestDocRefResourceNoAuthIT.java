package stroom.query.testing.memory;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.DocRefResourceNoAuthIT;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.memory.app.App;
import stroom.query.testing.memory.app.Config;
import stroom.query.testing.memory.app.TestDocRefEntity;
import stroom.query.testing.memory.app.TestDocRefServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
class TestDocRefResourceNoAuthIT extends DocRefResourceNoAuthIT<TestDocRefEntity, Config> {
    private static final DropwizardAppExtensionWithClients<Config> appRule =
            new DropwizardAppExtensionWithClients<>(App.class, resourceFilePath("generic_noauth/config.yml"));

    TestDocRefResourceNoAuthIT() {
        super(TestDocRefEntity.class,
                appRule);
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
