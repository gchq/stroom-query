package stroom.query.testing.jooq;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.AbstractField;
import stroom.docref.DocRef;
import stroom.query.api.v2.*;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.authorisation.DocumentPermission;
import stroom.query.testing.*;
import stroom.query.testing.data.CreateTestDataClient;
import stroom.query.testing.jooq.app.*;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(DatabaseContainerExtensionSupport.class)
@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(StroomAuthenticationExtensionSupport.class)
class TestJooqQueryResourceIT extends QueryResourceIT<TestDocRefJooqEntity, JooqConfig> {

    private static final DatabaseContainerExtension dbRule = new DatabaseContainerExtension();

    private static StroomAuthenticationExtension authRule = new StroomAuthenticationExtension();

    private static final DropwizardAppExtensionWithClients<JooqConfig> appRule =
            new DropwizardAppExtensionWithClients<>(JooqApp.class,
                    resourceFilePath("jooq/config.yml"),
                    authRule.authToken(),
                    authRule.authService());

    private final CreateTestDataClient testDataClient;

    private String testDataSeed;
    private DocRef testDataDocRef;

    TestJooqQueryResourceIT() {
        super(TestDocRefJooqEntity.TYPE,
                appRule,
                authRule);
        testDataClient = appRule.getClient(CreateTestDataClient::new);
    }

    @BeforeEach
    void beforeTest() {
        testDataSeed = UUID.randomUUID().toString();

        testDataDocRef = new DocRef.Builder()
                .uuid(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .type(TestDocRefJooqEntity.TYPE)
                .build();
        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(testDataDocRef)
                .permission(DocumentPermission.READ)
                .done();

        final Response createDocumentResponse = docRefClient.createDocument(authRule.adminUser(), testDataDocRef.getUuid(), testDataDocRef.getName());
        assertThat(createDocumentResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createDocumentResponse.close();

        final Response createTestDataResponse = testDataClient.createTestData(authRule.adminUser(), testDataDocRef.getUuid(), testDataSeed);
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
                                //.addMaxResults(10)
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
                .planetName("TestPlanet")
                .build();
    }

    @Test
    void testQuerySearch() {

        // Precalculate the page offsets
        int numberPages = 10;
        int pageSize = CreateTestDataJooqImpl.RECORDS_TO_CREATE / numberPages;
        final Collection<OffsetRange> pageOffsets = IntStream.range(0, numberPages)
                .mapToObj(value -> new OffsetRange.Builder()
                        .length((long) pageSize)
                        .offset((long) (value * pageSize))
                        .build())
                .collect(Collectors.toList());

        final ExpressionOperator expressionOperator = new ExpressionOperator.Builder(ExpressionOperator.Op.OR)
                .addTerm(TestQueryableJooqEntity.COLOUR, ExpressionTerm.Condition.CONTAINS, this.testDataSeed)
                .build();
        final Set<String> resultsSet = new HashSet<>();

        pageOffsets.forEach(offsetRange -> {
            try {
                final SearchRequest request = getValidSearchRequest(testDataDocRef, expressionOperator, offsetRange);

                final Response response = queryClient.search(authRule.adminUser(), request);
                assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);

                final SearchResponse searchResponse = response.readEntity(SearchResponse.class);

                for (final Result result : searchResponse.getResults()) {
                    assertThat(result instanceof FlatResult).isTrue();

                    final FlatResult flatResult = (FlatResult) result;
                    flatResult.getValues().stream()
                            .map(objects -> objects.get(3)) // skip over :ParentKey, :Key, :Depth
                            .map(Object::toString)
                            .forEach(resultsSet::add);
                }
            } catch (final RuntimeException e) {
                fail(e.getLocalizedMessage());
            }
        });

        assertThat(resultsSet).hasSize(CreateTestDataJooqImpl.RECORDS_TO_CREATE);
    }
}
