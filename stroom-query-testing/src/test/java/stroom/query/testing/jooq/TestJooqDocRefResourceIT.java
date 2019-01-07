package stroom.query.testing.jooq;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.testing.*;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DatabaseContainerExtensionSupport.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(StroomAuthenticationExtensionSupport.class)
class TestJooqDocRefResourceIT extends DocRefResourceIT<TestDocRefJooqEntity, JooqConfig> {
    private static StroomAuthenticationExtension authRule = new StroomAuthenticationExtension();

    private static final DatabaseContainerExtension dbRule = new DatabaseContainerExtension();

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

    @Test
    public void whenSelectQueryExecuted_thenResulstsReturned()
            throws Exception {
        var dbContainer = dbRule.getDbContainer();
        String jdbcUrl = dbContainer.getJdbcUrl();
        String username = dbContainer.getUsername();
        String password = dbContainer.getPassword();
        Connection conn = DriverManager
                .getConnection(jdbcUrl, username, password);
        ResultSet resultSet =
                conn.createStatement().executeQuery("SELECT 1");
        resultSet.next();
        int result = resultSet.getInt(1);

        assertThat(result).isEqualTo(1);
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
