package stroom.query.testing;

import io.dropwizard.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.client.DocRefServiceHttpClient;
import stroom.query.audit.client.NotFoundException;
import stroom.query.audit.client.UnauthenticatedException;
import stroom.query.audit.client.UnauthorisedException;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.service.QueryApiException;
import stroom.query.authorisation.DocumentPermission;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static stroom.query.testing.FifoLogbackExtension.containsAllOf;

@ExtendWith(FifoLogbackExtensionSupport.class)
@ExtendWith(DropwizardAppExtensionWithClientsSupport.class)
@ExtendWith(StroomAuthenticationExtensionSupport.class)
public abstract class DocRefRemoteServiceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private final String docRefType;
    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    private final DropwizardAppExtensionWithClients<CONFIG_CLASS> appRule;
    private final StroomAuthenticationExtension authRule;
    protected FifoLogbackExtension auditLogRule = new FifoLogbackExtension();
    private DocRefServiceHttpClient<DOC_REF_ENTITY> docRefClient;

    protected DocRefRemoteServiceIT(final String docRefType,
                                    final Class<DOC_REF_ENTITY> docRefEntityClass,
                                    final DropwizardAppExtensionWithClients<CONFIG_CLASS> appRule,
                                    final StroomAuthenticationExtension authRule) {
        this.docRefType = docRefType;
        this.docRefEntityClass = docRefEntityClass;
        this.appRule = appRule;
        this.authRule = authRule;
    }

    @BeforeEach
    void beforeEach() {
        docRefClient = appRule.getClient(u -> new DocRefServiceHttpClient<>(docRefType, docRefEntityClass, u));
    }

    @Test
    void testCreate() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();

        assertThatThrownBy(() ->
                docRefClient.createDocument(
                        authRule.unauthenticatedUser(unauthenticatedUsername),
                        uuid,
                        name))
                .isInstanceOf(UnauthenticatedException.class);

        final DOC_REF_ENTITY createdEntity = docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        assertThat(createdEntity.getUuid()).isEqualTo(uuid);
        assertThat(createdEntity.getName()).isEqualTo(name);

        final DOC_REF_ENTITY foundEntity = docRefClient.get(authRule.adminUser(), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getName()).isEqualTo(name);

        // Create (forbidden), Create (ok), get
        auditLogRule.check()
                .thereAreAtLeast(2)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testUpdate() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .permission(DocumentPermission.UPDATE)
                .done();

        // Create a document
        docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final DOC_REF_ENTITY updateResponseBody = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(updateResponseBody).isEqualTo(authorisedEntityUpdate);

        // Try updating it as an unauthorised user
        final DOC_REF_ENTITY unauthorisedEntityUpdate = createPopulatedEntity(uuid, name);
        assertThatThrownBy(() -> docRefClient.update(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid,
                unauthorisedEntityUpdate)).isInstanceOf(UnauthorisedException.class);

        // Try updating it as an unauthenticated user
        final DOC_REF_ENTITY unauthenticatedEntityUpdate = createPopulatedEntity(uuid, name);
        assertThatThrownBy(() -> docRefClient.update(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                unauthenticatedEntityUpdate)).isInstanceOf(UnauthorisedException.class);

        // Check it is still in the state from the authorised update
        final DOC_REF_ENTITY checkEntity = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(checkEntity).isEqualTo(authorisedEntityUpdate);

        // Create, update (ok), update (forbidden), get (check)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testGetInfo() throws QueryApiException {
        final Long testStartTime = System.currentTimeMillis();

        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .permission(DocumentPermission.UPDATE)
                .done();

        // Create a document
        docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);

        // Get info as authorised user
        final DocRefInfo info = docRefClient.getInfo(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(info.getCreateTime() >= testStartTime).isTrue();
        assertThat(info.getUpdateTime() > info.getCreateTime()).isTrue();
        assertThat(authRule.adminUser().getName()).isEqualTo(info.getCreateUser());
        assertThat(authRule.authenticatedUser(authorisedUsername).getName()).isEqualTo(info.getUpdateUser());

        // Try to get info as unauthorised user
        assertThatThrownBy(() ->
                docRefClient.getInfo(authRule.authenticatedUser(unauthorisedUsername), uuid))
                .isInstanceOf(UnauthorisedException.class);

        // Try to get info as unauthenticated user
        assertThatThrownBy(() ->
                docRefClient.getInfo(authRule.unauthenticatedUser(unauthenticatedUsername), uuid))
                .isInstanceOf(UnauthenticatedException.class);

        // Create, update (ok), get (ok), get (forbidden)
        // GET INFO defaults to calling GET in the Service interface, so that is how it looks on the server side...
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testGet() throws QueryApiException {
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();

        authRule.permitAdminUser()
                .andAuthenticatedUser(authorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();

        docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        docRefClient.get(authRule.adminUser(), uuid);

        docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        assertThatThrownBy(() ->
                docRefClient.get(authRule.unauthenticatedUser(unauthenticatedUsername), uuid))
                .isInstanceOf(UnauthenticatedException.class);

        assertThatThrownBy(() ->
                docRefClient.get(authRule.authenticatedUser(unauthorisedUsername), uuid))
                .isInstanceOf(UnauthorisedException.class);

        // Create, get (admin), get (authorized), get (unauthorised)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testRename() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name1 = UUID.randomUUID().toString();
        final String name2 = UUID.randomUUID().toString();
        final String name3 = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();
        // No specific permissions required for rename (is this right?)

        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();

        docRefClient.createDocument(authRule.adminUser(), uuid, name1)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Attempt rename as an authorised user
        final DOC_REF_ENTITY renamedEntity = docRefClient.renameDocument(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                name2)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(renamedEntity.getName()).isEqualTo(name2);

        // Check it has the new name
        final DOC_REF_ENTITY updated = docRefClient.get(authRule.adminUser(), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(updated.getName()).isEqualTo(name2);

        // Attempt rename with name3 as unauthenticated user
        assertThatThrownBy(() ->
                docRefClient.renameDocument(
                        authRule.unauthenticatedUser(unauthenticatedUsername),
                        uuid,
                        name3))
                .isInstanceOf(UnauthenticatedException.class);

        // Check it still has name2
        final DOC_REF_ENTITY updatesPostFailedRenames = docRefClient.get(authRule.adminUser(), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(updatesPostFailedRenames).isNotNull();
        assertThat(updatesPostFailedRenames.getName()).isEqualTo(name2);

        // Create, rename, rename, get (check still got name2)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid)) // create
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.RENAME_DOC_REF, uuid)) // authorised rename
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid)) // check name
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid)); // check name after unauthorised attempt
    }

    @Test
    void testCopy() throws QueryApiException {
        final String uuid1 = UUID.randomUUID().toString();
        final String uuid2 = UUID.randomUUID().toString();
        final String uuid3 = UUID.randomUUID().toString();
        final String uuid4 = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .permission(DocumentPermission.READ)
                .docRef(uuid1, docRefType)
                .docRef(uuid2, docRefType)
                .docRef(uuid3, docRefType)
                .docRef(uuid4, docRefType)
                .done();

        docRefClient.createDocument(authRule.adminUser(), uuid1, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Attempt copy as authorised user
        final DOC_REF_ENTITY copiedEntity = docRefClient.copyDocument(authRule.authenticatedUser(authorisedUsername), uuid1, uuid2)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(copiedEntity.getUuid()).isEqualTo(uuid2);

        // Get the copy
        final DOC_REF_ENTITY updatedEntity = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid2)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(updatedEntity.getName()).isEqualTo(name);
        assertThat(updatedEntity.getUuid()).isEqualTo(uuid2);

        // Attempt copy as unauthorised user
        assertThatThrownBy(() ->
                docRefClient.copyDocument(
                        authRule.authenticatedUser(unauthorisedUsername),
                        uuid1,
                        uuid3))
                .isInstanceOf(UnauthorisedException.class);

        assertThatThrownBy(() ->
                docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid3))
                .isInstanceOf(NotFoundException.class);

        // Attempt copy as unauthenticated user
        assertThatThrownBy(() ->
                docRefClient.copyDocument(
                        authRule.unauthenticatedUser(unauthenticatedUsername),
                        uuid1,
                        uuid4))
                .isInstanceOf(UnauthenticatedException.class);

        assertThatThrownBy(() ->
                docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid4))
                .isInstanceOf(NotFoundException.class);

        // Create, copy, get, copy (forbidden), get, get
        auditLogRule.check()
                .thereAreAtLeast(6)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid1))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.COPY_DOC_REF, uuid1, uuid2))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid2))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.COPY_DOC_REF, uuid1, uuid3))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid3))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid4));
    }

    @Test
    void testDelete() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .andAuthenticatedUser(unauthorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.DELETE)
                .done();

        docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Make user a user that is not authorised for deletion cannot delete it
        assertThatThrownBy(() ->
                docRefClient.deleteDocument(
                        authRule.authenticatedUser(unauthorisedUsername),
                        uuid))
                .isInstanceOf(UnauthorisedException.class);

        // Make sure a user that is not authenticated cannot delete it either
        assertThatThrownBy(() ->
                docRefClient.deleteDocument(
                        authRule.unauthenticatedUser(unauthenticatedUsername),
                        uuid))
                .isInstanceOf(UnauthenticatedException.class);

        // Ensure the document is still there
        docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Check the fully authorised user can delete it
        docRefClient.deleteDocument(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Now check it has been deleted
        assertThatThrownBy(() ->
                docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid))
                .isInstanceOf(NotFoundException.class);

        // Create, delete (forbidden), get (200), delete, get (404)
        auditLogRule.check()
                .thereAreAtLeast(5)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.DELETE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.DELETE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testExport() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.UPDATE)
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.EXPORT)
                .done();

        // Create a document
        docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        docRefClient.update(authRule.adminUser(), uuid, entityUpdate)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Try exporting it as an authorised user
        final ExportDTO exportDTO = docRefClient.exportDocument(authRule.authenticatedUser(authorisedUsername), uuid);

        final Map<String, String> expectedExportValues = exportValues(entityUpdate);
        expectedExportValues.put(DocRefEntity.NAME, entityUpdate.getName()); // add common fields
        assertThat(exportDTO.getValues()).isEqualTo(expectedExportValues);

        // Try exporting it as an unauthorised user
        assertThatThrownBy(() ->
                docRefClient.exportDocument(authRule.authenticatedUser(unauthorisedUsername), uuid))
                .isInstanceOf(UnauthorisedException.class);

        // Try exporting it as an unauthenticated user
        assertThatThrownBy(() ->
                docRefClient.exportDocument(authRule.unauthenticatedUser(unauthenticatedUsername), uuid))
                .isInstanceOf(UnauthenticatedException.class);

        // Create, update, export, export (forbidden)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid));
    }

    @Test
    void testImport() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAuthenticatedUser(authorisedUsername)
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.IMPORT)
                .permission(DocumentPermission.READ)
                .done();

        // Create an entity to import
        final DOC_REF_ENTITY docRefEntity = createPopulatedEntity(uuid, name);
        final Map<String, String> importValues = exportValues(docRefEntity);

        // Try importing it as an authorised user
        final DOC_REF_ENTITY importedDocRefEntity = docRefClient.importDocument(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                name,
                true,
                importValues)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Fetch the doc ref from the system to check it's been imported ok
        final DOC_REF_ENTITY getCheckEntity = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertThat(getCheckEntity).isEqualTo(docRefEntity);

        // import, get
        auditLogRule.check()
                .thereAreAtLeast(2)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.IMPORT_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    private DOC_REF_ENTITY createPopulatedEntity(final String uuid, final String name) {
        return new DocRefEntity.Builder<>(createPopulatedEntity())
                .uuid(uuid)
                .name(name)
                .build();
    }

    protected abstract DOC_REF_ENTITY createPopulatedEntity();

    protected abstract Map<String, String> exportValues(DOC_REF_ENTITY docRefEntity);
}
