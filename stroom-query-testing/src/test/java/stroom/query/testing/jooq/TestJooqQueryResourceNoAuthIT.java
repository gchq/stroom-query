package stroom.query.testing.jooq;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.AbstractField;
import stroom.docref.DocRef;
import stroom.query.api.v2.*;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.security.NoAuthValueFactoryProvider;
import stroom.query.testing.DatabaseContainerExtension;
import stroom.query.testing.DatabaseContainerExtensionSupport;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.QueryResourceNoAuthIT;
import stroom.query.testing.data.CreateTestDataClient;
import stroom.query.testing.jooq.app.JooqApp;
import stroom.query.testing.jooq.app.JooqConfig;
import stroom.query.testing.jooq.app.TestDocRefJooqEntity;
import stroom.query.testing.jooq.app.TestQueryableJooqEntity;

import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DatabaseContainerExtensionSupport.class)
@ExtendWith(DropwizardExtensionsSupport.class)
class TestJooqQueryResourceNoAuthIT extends QueryResourceNoAuthIT<TestDocRefJooqEntity, JooqConfig> {

    private static final DatabaseContainerExtension dbRule = new DatabaseContainerExtension();

    private static final DropwizardAppExtensionWithClients<JooqConfig> appRule =
            new DropwizardAppExtensionWithClients<>(JooqApp.class, resourceFilePath("jooq_noauth/config.yml"));

    private final CreateTestDataClient testDataClient;


    TestJooqQueryResourceNoAuthIT() {
        super(TestDocRefJooqEntity.TYPE,
                appRule);
        testDataClient = appRule.getClient(CreateTestDataClient::new);
    }

    @BeforeEach
    void beforeTest() {
        String testDataSeed = UUID.randomUUID().toString();

        DocRef testDataDocRef = new DocRef.Builder()
                .uuid(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .type(TestDocRefJooqEntity.TYPE)
                .build();

        final Response createDocumentResponse = docRefClient.createDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                testDataDocRef.getUuid(),
                testDataDocRef.getName());
        assertThat(createDocumentResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createDocumentResponse.close();

        final Response createTestDataResponse = testDataClient.createTestData(
                NoAuthValueFactoryProvider.ADMIN_USER,
                testDataDocRef.getUuid(),
                testDataSeed);
        assertThat(createTestDataResponse.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        createTestDataResponse.close();

        // Clear the audit log for the create document
        auditLogRule.check().containsOrdered(d -> d.contains(AuditedDocRefResourceImpl.CREATE_DOC_REF));
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
                .map(AbstractField::getName)
                .collect(Collectors.toSet());

        assertThat(resultFieldNames.contains(DocRefEntity.CREATE_TIME)).isTrue();
        assertThat(resultFieldNames.contains(DocRefEntity.CREATE_USER)).isTrue();
        assertThat(resultFieldNames.contains(DocRefEntity.UPDATE_TIME)).isTrue();
        assertThat(resultFieldNames.contains(DocRefEntity.UPDATE_USER)).isTrue();
        assertThat(resultFieldNames.contains(TestQueryableJooqEntity.ID)).isTrue();
        assertThat(resultFieldNames.contains(TestQueryableJooqEntity.COLOUR)).isTrue();
    }

    @Override
    protected TestDocRefJooqEntity getValidEntity(final DocRef docRef) {
        return new TestDocRefJooqEntity.Builder()
                .docRef(docRef)
                .planetName("PlanetName")
                .build();
    }
}
