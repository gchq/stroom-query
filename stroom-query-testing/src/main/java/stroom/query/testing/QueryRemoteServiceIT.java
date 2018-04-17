package stroom.query.testing;

import io.dropwizard.Configuration;
import org.junit.Rule;
import org.junit.Test;
import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.OffsetRange;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefServiceHttpClient;
import stroom.query.audit.client.QueryServiceHttpClient;
import stroom.query.audit.client.UnauthenticatedException;
import stroom.query.audit.client.UnauthorisedException;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.service.QueryApiException;

import java.util.UUID;

import static org.junit.Assert.fail;
import static stroom.query.testing.FifoLogbackRule.containsAllOf;

public abstract class QueryRemoteServiceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private final String docRefType;
    protected DocRefServiceHttpClient<DOC_REF_ENTITY> docRefClient;
    protected QueryServiceHttpClient queryClient;
    private final StroomAuthenticationRule authRule;

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    protected QueryRemoteServiceIT(final String docRefType,
                                   final Class<DOC_REF_ENTITY> docRefEntityClass,
                                   final DropwizardAppWithClientsRule<CONFIG_CLASS> appRule,
                                   final StroomAuthenticationRule authRule) {
        this.docRefType = docRefType;
        this.authRule = authRule;
        this.queryClient = appRule.getClient(url -> new QueryServiceHttpClient(docRefType, url));
        this.docRefClient = appRule.getClient(u -> new DocRefServiceHttpClient<>(docRefType, docRefEntityClass, u));
    }

    protected abstract SearchRequest getValidSearchRequest(final DocRef docRef,
                                                           final ExpressionOperator expressionOperator,
                                                           final OffsetRange offsetRange);

    protected abstract void assertValidDataSource(final DataSource dataSource);

    protected abstract DOC_REF_ENTITY getValidEntity(final DocRef docRef);

    protected QueryServiceHttpClient getQueryClient() {
        return queryClient;
    }

    @Test
    public void testGetDataSource() throws QueryApiException {
        final DocRef docRef = createDocument();

        final DataSource result = queryClient.getDataSource(authRule.adminUser(), docRef)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertValidDataSource(result);

        // Create doc ref, update, get data source
        auditLogRule.check()
                .thereAreAtLeast(3)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedQueryResourceImpl.GET_DATA_SOURCE, docRef.getUuid()));
    }

    @Test
    public void testGetDataSourcePermissions() throws QueryApiException {
        final DocRef docRef = createDocument();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(docRef.getUuid(), docRefType)
                .permission(DocumentPermission.READ)
                .done();

        queryClient.getDataSource(authRule.authenticatedUser(authorisedUsername), docRef)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        try {
            queryClient.getDataSource(authRule.authenticatedUser(unauthorisedUsername), docRef);
            fail();
        } catch (UnauthorisedException e) {
            // good
        }

        try {
            queryClient.getDataSource(authRule.unauthenticatedUser(unauthenticatedUsername), docRef);
            fail();
        } catch (UnauthenticatedException e) {
            // good
        }

        // Create index, update, authorised get, unauthorised get
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedQueryResourceImpl.GET_DATA_SOURCE, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedQueryResourceImpl.GET_DATA_SOURCE, docRef.getUuid()));
    }

    @Test
    public void testSearchPermissions() throws QueryApiException {
        final DocRef docRef = createDocument();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(docRef.getUuid(), docRefType)
                .permission(DocumentPermission.READ)
                .done();

        final SearchRequest searchRequest = getValidSearchRequest(docRef,
                new ExpressionOperator.Builder(ExpressionOperator.Op.OR).build(),
                new OffsetRange.Builder()
                        .length(0L)
                        .offset(0L)
                        .build());

        queryClient.search(authRule.authenticatedUser(authorisedUsername), searchRequest)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        try {
            queryClient.getDataSource(authRule.authenticatedUser(unauthorisedUsername), docRef);
            fail();
        } catch (UnauthorisedException e) {
            // good
        }

        try {
            queryClient.getDataSource(authRule.unauthenticatedUser(unauthenticatedUsername), docRef);
            fail();
        } catch (UnauthenticatedException e) {
            // good
        }
    }

    /**
     * Utility function to randomly generate a new annotations index doc ref.
     * It assumes that the creation of documents works, the detail of that is tested in another suite of tests.
     * Once the document is created, the passed in doc ref entity is then used to flesh out the implementation
     * specific details.
     *
     * @param docRefEntity The implementation specific entity, used to update the doc ref so it can be used.
     * @return The DocRef of the newly created annotations index.
     */
    protected DocRef createDocument(final DOC_REF_ENTITY docRefEntity) throws QueryApiException {
        final DocRef docRef = new DocRef.Builder()
                .uuid(UUID.randomUUID().toString())
                .type(docRefType)
                .name(UUID.randomUUID().toString())
                .build();

        // Ensure admin user can create the document in the folder
        authRule.permitAdminUser().done();

        // Create a doc ref to hang the search from
        docRefClient.createDocument(authRule.adminUser(), docRef.getUuid(), docRef.getName())
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Give admin all the roles required to manipulate the document and it's underlying data
        authRule.permitAdminUser()
                .docRef(docRef)
                .permission(DocumentPermission.READ)
                .permission(DocumentPermission.UPDATE)
                .done();

        final DOC_REF_ENTITY docRefEntityToUse = (docRefEntity != null) ? docRefEntity : getValidEntity(docRef);
        docRefClient.update(authRule.adminUser(), docRef.getUuid(), docRefEntityToUse)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        return docRef;
    }

    /**
     * Create document, use the 'valid' doc ref entity for this test class.
     *
     * @return The Doc Ref of the created document.
     */
    protected DocRef createDocument() throws QueryApiException {
        return createDocument(null);
    }
}
