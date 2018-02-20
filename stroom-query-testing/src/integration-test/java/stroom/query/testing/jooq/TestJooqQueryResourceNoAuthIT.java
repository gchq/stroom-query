package stroom.query.testing.jooq;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.ClassRule;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.Field;
import stroom.query.api.v2.OffsetRange;
import stroom.query.api.v2.Query;
import stroom.query.api.v2.ResultRequest;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.TableSettings;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.security.NoAuthValueFactoryProvider;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.QueryResourceNoAuthIT;
import stroom.query.testing.data.CreateTestDataClient;
import stroom.query.testing.generic.app.TestQueryServiceImpl;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;
import stroom.query.testing.jooq.app.TestQueryableJooqEntity;

import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestJooqQueryResourceNoAuthIT extends QueryResourceNoAuthIT<TestDocRefJooqEntity, JooqConfig> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<JooqConfig> appRule =
            new DropwizardAppWithClientsRule<>(JooqApp.class, resourceFilePath("hibernate_noauth/config.yml"));

    private final CreateTestDataClient testDataClient;

    private String testDataSeed;
    private DocRef testDataDocRef;

    public TestJooqQueryResourceNoAuthIT() {
        super(TestDocRefJooqEntity.class,
                TestDocRefJooqEntity.TYPE,
                appRule);
        testDataClient = appRule.getClient(CreateTestDataClient::new);
    }

    @Before
    public void beforeTest() {
        this.testDataSeed = UUID.randomUUID().toString();
        final Response response = testDataClient.createTestData(NoAuthValueFactoryProvider.ADMIN_USER, this.testDataSeed);
        assertEquals(HttpStatus.OK_200, response.getStatus());
        this.testDataDocRef = response.readEntity(DocRef.class);
    }

    @Override
    protected SearchRequest getValidSearchRequest(final DocRef docRef,
                                                  final ExpressionOperator expressionOperator,
                                                  final OffsetRange offsetRange) {
        final String queryKey = UUID.randomUUID().toString();
        return new SearchRequest.Builder()
                .query(new Query.Builder()
                        .dataSource(docRef)
                        .expression(expressionOperator)
                        .build())
                .key(queryKey)
                .dateTimeLocale("en-gb")
                .incremental(true)
                .addResultRequests(new ResultRequest.Builder()
                        .fetch(ResultRequest.Fetch.ALL)
                        .resultStyle(ResultRequest.ResultStyle.FLAT)
                        .componentId("componentId")
                        .requestedRange(offsetRange)
                        .addMappings(new TableSettings.Builder()
                                .queryId(queryKey)
                                .extractValues(false)
                                .showDetail(false)
                                .addFields(new Field.Builder()
                                        .name(TestQueryableJooqEntity.COLOUR)
                                        .expression("${" + TestQueryableJooqEntity.COLOUR + "}")
                                        .build())
                                .addMaxResults(10)
                                .build())
                        .build())
                .build();
    }

    @Override
    protected void assertValidDataSource(final DataSource dataSource) {
        final Set<String> resultFieldNames = dataSource.getFields().stream()
                .map(DataSourceField::getName)
                .collect(Collectors.toSet());

        assertTrue(resultFieldNames.contains(DocRefEntity.CREATE_TIME));
        assertTrue(resultFieldNames.contains(DocRefEntity.CREATE_USER));
        assertTrue(resultFieldNames.contains(DocRefEntity.UPDATE_TIME));
        assertTrue(resultFieldNames.contains(DocRefEntity.UPDATE_USER));
        assertTrue(resultFieldNames.contains(TestQueryableJooqEntity.ID));
        assertTrue(resultFieldNames.contains(TestQueryableJooqEntity.COLOUR));
    }

    @Override
    protected TestDocRefJooqEntity getValidEntity(final DocRef docRef) {
        return new TestDocRefJooqEntity.Builder()
                .docRef(docRef)
                .planetName("PlanetName")
                .build();
    }
}