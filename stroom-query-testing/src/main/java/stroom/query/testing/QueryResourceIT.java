package stroom.query.testing;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.OffsetRange;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.client.QueryResourceHttpClient;
import stroom.query.audit.service.DocRefEntity;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public abstract class QueryResourceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration,
        APP_CLASS extends Application<CONFIG_CLASS>>
        extends AbstractIT<DOC_REF_ENTITY, CONFIG_CLASS, APP_CLASS> {
    protected DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;
    protected QueryResourceHttpClient queryClient;

    protected QueryResourceIT(final Class<APP_CLASS> appClass,
                               final Class<DOC_REF_ENTITY> docRefEntityClass,
                               final String docRefType,
                              final DropwizardAppRule<CONFIG_CLASS> appRule,
                              final WireMockClassRule wireMockRule) {
        super(appClass, docRefEntityClass, docRefType, appRule, wireMockRule);
    }

    protected abstract SearchRequest getValidSearchRequest(final DocRef docRef,
                                                           final ExpressionOperator expressionOperator,
                                                           final OffsetRange offsetRange);

    @Before
    public final void beforeQueryTest() {
        queryClient = new QueryResourceHttpClient(getAppHost());
        docRefClient = new DocRefResourceHttpClient<>(getAppHost());
    }

    protected abstract void assertValidDataSource(final DataSource dataSource);

    protected abstract DOC_REF_ENTITY getValidEntity(final DocRef docRef);

    @Test
    public void testGetDataSource() throws Exception {
        final DocRef docRef = createDocument();

        final Response response = queryClient.getDataSource(adminUser(), docRef);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        final DataSource result = getFromBody(response, DataSource.class);

        assertValidDataSource(result);

        // Create doc ref, update, get data source
        checkAuditLogs(3);
    }

    @Test
    public void testGetDataSourcePermissions() {
        final DocRef docRef = createDocument();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        giveDocumentPermission(authenticatedUser(authorisedUsername), docRef.getUuid(), DocumentPermission.READ);

        final Response authorisedResponse = queryClient.getDataSource(authenticatedUser(authorisedUsername), docRef);
        assertEquals(HttpStatus.OK_200, authorisedResponse.getStatus());

        final Response unauthorisedResponse = queryClient.getDataSource(authenticatedUser(unauthorisedUsername), docRef);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedResponse.getStatus());

        final Response unauthenticatedResponse = queryClient.getDataSource(unauthenticatedUser(unauthenticatedUsername), docRef);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedResponse.getStatus());

        // Create index, update, authorised get, unauthorised get
        checkAuditLogs(4);
    }

    @Test
    public void testSearchPermissions() {
        final DocRef docRef = createDocument();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        giveDocumentPermission(authenticatedUser(authorisedUsername), docRef.getUuid(), DocumentPermission.READ);

        final SearchRequest searchRequest = getValidSearchRequest(docRef,
                new ExpressionOperator.Builder(ExpressionOperator.Op.OR).build(),
                new OffsetRange.Builder()
                        .length(0L)
                        .offset(0L)
                        .build());

        final Response authorisedResponse = queryClient.search(authenticatedUser(authorisedUsername), searchRequest);
        assertEquals(HttpStatus.OK_200, authorisedResponse.getStatus());

        final Response unauthorisedResponse = queryClient.getDataSource(authenticatedUser(unauthorisedUsername), docRef);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedResponse.getStatus());

        final Response unauthenticatedResponse = queryClient.getDataSource(unauthenticatedUser(unauthenticatedUsername), docRef);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedResponse.getStatus());

    }

    /**
     * Utility function to randomly generate a new annotations index doc ref.
     * It assumes that the creation of documents works, the detail of that is tested in another suite of tests.
     * Once the document is created, the passed in doc ref entity is then used to flesh out the implementation
     * specific details.
     * @param docRefEntity The implementation specific entity, used to update the doc ref so it can be used.
     * @return The DocRef of the newly created annotations index.
     */
    protected DocRef createDocument(final DOC_REF_ENTITY docRefEntity) {
        // Generate UUID's for the doc ref and it's parent folder
        final String parentFolderUuid = UUID.randomUUID().toString();
        final DocRef docRef = new DocRef.Builder()
                .uuid(UUID.randomUUID().toString())
                .type(getDocRefType())
                .name(UUID.randomUUID().toString())
                .build();

        // Ensure admin user can create the document in the folder
        giveFolderCreatePermission(adminUser(), parentFolderUuid);

        // Create a doc ref to hang the search from
        final Response createResponse = docRefClient.createDocument(
                adminUser(),
                docRef.getUuid(),
                docRef.getName(),
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Give admin all the roles required to manipulate the document and it's underlying data
        giveDocumentPermission(adminUser(), docRef.getUuid(), DocumentPermission.READ);
        giveDocumentPermission(adminUser(), docRef.getUuid(), DocumentPermission.UPDATE);

        final DOC_REF_ENTITY docRefEntityToUse = (docRefEntity != null) ? docRefEntity : getValidEntity(docRef);
        final Response updateIndexResponse =
                docRefClient.update(adminUser(),
                        docRef.getUuid(),
                        docRefEntityToUse);
        assertEquals(HttpStatus.OK_200, updateIndexResponse.getStatus());

        return docRef;
    }

    /**
     * Create document, use the 'valid' doc ref entity for this test class.
     * @return The Doc Ref of the created document.
     */
    protected DocRef createDocument() {
        return createDocument(null);
    }
}
