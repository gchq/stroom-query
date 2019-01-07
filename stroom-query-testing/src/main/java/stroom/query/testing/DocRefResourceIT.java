package stroom.query.testing;

import io.dropwizard.Configuration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.authorisation.DocumentPermission;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static stroom.query.testing.FifoLogbackExtension.containsAllOf;

@ExtendWith(FifoLogbackExtensionSupport.class)
public abstract class DocRefResourceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private final String docRefType;
    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    private final DropwizardAppExtensionWithClients<CONFIG_CLASS> appRule;
    private final StroomAuthenticationExtension authRule;
    protected FifoLogbackExtension auditLogRule = new FifoLogbackExtension();
    private DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;

    protected DocRefResourceIT(final String docRefType,
                               final Class<DOC_REF_ENTITY> docRefEntityClass,
                               final DropwizardAppExtensionWithClients<CONFIG_CLASS> appRule,
                               final StroomAuthenticationExtension authRule) {
        this.docRefType = docRefType;
        this.docRefEntityClass = docRefEntityClass;
        this.authRule = authRule;
        this.appRule = appRule;
    }

    @BeforeEach
    public void beforeAll() {
        docRefClient = appRule.getClient(DocRefResourceHttpClient::new);
    }

    @Test
    void testCreate() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();

        final Response unauthenticatedCreateResponse = docRefClient.createDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name);
        assertThat(unauthenticatedCreateResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedCreateResponse.close();

        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY createdEntity = createResponse.readEntity(docRefEntityClass);
        assertThat(createdEntity).isNotNull();
        assertThat(createdEntity.getUuid()).isEqualTo(uuid);
        assertThat(createdEntity.getName()).isEqualTo(name);
        createResponse.close();

        final Response getResponse = docRefClient.get(authRule.adminUser(), uuid);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY foundEntity = getResponse.readEntity(docRefEntityClass);
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getName()).isEqualTo(name);
        getResponse.close();

        // Create (forbidden), Create (ok), get
        auditLogRule.check()
                .thereAreAtLeast(2)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testUpdate() {
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
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertThat(updateResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        final DOC_REF_ENTITY updateResponseBody = updateResponse.readEntity(docRefEntityClass);
        assertThat(updateResponseBody).isEqualTo(authorisedEntityUpdate);
        updateResponse.close();

        // Try updating it as an unauthorised user
        final DOC_REF_ENTITY unauthorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthorisedUpdateResponse = docRefClient.update(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid,
                unauthorisedEntityUpdate);
        assertThat(unauthorisedUpdateResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        unauthorisedUpdateResponse.close();

        // Try updating it as an unauthenticated user
        final DOC_REF_ENTITY unauthenticatedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthenticatedUpdateResponse = docRefClient.update(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                unauthenticatedEntityUpdate);
        assertThat(unauthenticatedUpdateResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedUpdateResponse.close();

        // Check it is still in the state from the authorised update
        final Response getCheckResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(getCheckResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        final DOC_REF_ENTITY checkEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertThat(checkEntity).isEqualTo(authorisedEntityUpdate);
        getCheckResponse.close();

        // Create, update (ok), update (forbidden), get (check)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testGetInfo() {
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
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertThat(updateResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        updateResponse.close();

        // Get info as authorised user
        final Response authorisedGetInfoResponse = docRefClient.getInfo(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(authorisedGetInfoResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        final DocRefInfo info = authorisedGetInfoResponse.readEntity(DocRefInfo.class);
        assertThat(info.getCreateTime() >= testStartTime).isTrue();
        assertThat(info.getUpdateTime() > info.getCreateTime()).isTrue();
        assertThat(authRule.adminUser().getName()).isEqualTo(info.getCreateUser());
        assertThat(authRule.authenticatedUser(authorisedUsername).getName()).isEqualTo(info.getUpdateUser());
        authorisedGetInfoResponse.close();

        // Try to get info as unauthorised user
        final Response unauthorisedGetInfoResponse = docRefClient.getInfo(authRule.authenticatedUser(unauthorisedUsername), uuid);
        assertThat(unauthorisedGetInfoResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        unauthorisedGetInfoResponse.close();

        // Try to get info as unauthenticated user
        final Response unauthenticatedGetInfoResponse = docRefClient.getInfo(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
        assertThat(unauthenticatedGetInfoResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedGetInfoResponse.close();

        // Create, update (ok), get info (ok), get info (forbidden)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF_INFO, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF_INFO, uuid));
    }

    @Test
    void testGet() {
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

        final Response createResponse = docRefClient.createDocument(
                authRule.adminUser(),
                uuid,
                name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        final Response getResponseAdmin = docRefClient.get(authRule.adminUser(), uuid);
        assertThat(getResponseAdmin.getStatus()).isEqualTo(HttpStatus.OK_200);
        getResponseAdmin.close();

        final Response getResponseAuthorisedUser = docRefClient.get(
                authRule.authenticatedUser(authorisedUsername),
                uuid);
        assertThat(getResponseAuthorisedUser.getStatus()).isEqualTo(HttpStatus.OK_200);
        getResponseAuthorisedUser.close();

        final Response getResponseUnauthenticatedUser = docRefClient.get(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertThat(getResponseUnauthenticatedUser.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        getResponseUnauthenticatedUser.close();

        final Response getResponseUnauthorisedUser = docRefClient.get(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid);
        assertThat(getResponseUnauthorisedUser.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        getResponseUnauthorisedUser.close();

        // Create, get (admin), get (authorized), get (unauthorised)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testRename() {
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

        final Response createResponse = docRefClient.createDocument(
                authRule.adminUser(),
                uuid,
                name1);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Attempt rename as an authorised user
        final Response renameResponse = docRefClient.renameDocument(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                name2);
        assertThat(renameResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY renamedEntity = renameResponse.readEntity(docRefEntityClass);
        assertThat(renamedEntity).isNotNull();
        assertThat(renamedEntity.getName()).isEqualTo(name2);

        // Check it has the new name
        final Response getResponse = docRefClient.get(authRule.adminUser(), uuid);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY updated = getResponse.readEntity(docRefEntityClass);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo(name2);

        // Attempt rename with name3 as unauthenticated user
        final Response unauthenticatedRenameResponse = docRefClient.renameDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name3);
        assertThat(unauthenticatedRenameResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedRenameResponse.close();

        // Check it still has name2
        final Response getPostFailedRenamesResponse = docRefClient.get(authRule.adminUser(), uuid);
        final DOC_REF_ENTITY updatesPostFailedRenames = getPostFailedRenamesResponse.readEntity(docRefEntityClass);
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
    void testCopy() {
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

        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid1, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Attempt copy as authorised user
        final Response copyResponse = docRefClient.copyDocument(authRule.authenticatedUser(authorisedUsername), uuid1, uuid2);
        assertThat(copyResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY copiedEntity = copyResponse.readEntity(docRefEntityClass);
        assertThat(copiedEntity).isNotNull();
        assertThat(copiedEntity.getUuid()).isEqualTo(uuid2);

        // Get the copy
        final Response getResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid2);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY updatedEntity = getResponse.readEntity(docRefEntityClass);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getName()).isEqualTo(name);
        assertThat(updatedEntity.getUuid()).isEqualTo(uuid2);

        // Attempt copy as unauthorised user
        final Response unauthorisedCopyResponse = docRefClient.copyDocument(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid1,
                uuid3);
        assertThat(unauthorisedCopyResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        unauthorisedCopyResponse.close();

        final Response getUnauthorisedCopyResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid3);
        assertThat(getUnauthorisedCopyResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        getUnauthorisedCopyResponse.close();

        // Attempt copy as unauthenticated user
        final Response unauthenticatedCopyResponse = docRefClient.copyDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid1,
                uuid4);
        assertThat(unauthenticatedCopyResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedCopyResponse.close();

        final Response getUnauthenticatedCopyResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid4);
        assertThat(getUnauthenticatedCopyResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        getUnauthenticatedCopyResponse.close();

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
    void testDelete() {
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

        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Make user a user that is not authorised for deletion cannot delete it
        final Response unauthorisedDeleteResponse = docRefClient.deleteDocument(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid);
        assertThat(unauthorisedDeleteResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        unauthorisedDeleteResponse.close();

        // Make sure a user that is not authenticated cannot delete it either
        final Response unauthenticatedDeleteResponse = docRefClient.deleteDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertThat(unauthenticatedDeleteResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedDeleteResponse.close();

        // Ensure the document is still there
        final Response getStillThereResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(getStillThereResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        getStillThereResponse.close();

        // Check the fully authorised user can delete it
        final Response authorisedDeleteResponse = docRefClient.deleteDocument(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(authorisedDeleteResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        authorisedDeleteResponse.close();

        // Now check it has been deleted
        final Response getResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        getResponse.close();

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
    void testExport() {
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
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(authRule.adminUser(), uuid, entityUpdate);
        assertThat(updateResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        updateResponse.close();

        // Try exporting it as an authorised user
        final Response authorisedExportResponse = docRefClient.exportDocument(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(authorisedExportResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final ExportDTO exportDTO = authorisedExportResponse.readEntity(ExportDTO.class);
        assertThat(exportDTO).isNotNull();

        final Map<String, String> expectedExportValues = exportValues(entityUpdate);
        expectedExportValues.put(DocRefEntity.NAME, entityUpdate.getName()); // add common fields
        assertThat(exportDTO.getValues()).isEqualTo(expectedExportValues);

        // Try exporting it as an unauthorised user
        final Response unauthorisedExportResponse = docRefClient.exportDocument(authRule.authenticatedUser(unauthorisedUsername), uuid);
        assertThat(unauthorisedExportResponse.getStatus()).isEqualTo(HttpStatus.FORBIDDEN_403);
        unauthorisedExportResponse.close();

        // Try exporting it as an unauthenticated user
        final Response unauthenticatedExportResponse = docRefClient.exportDocument(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
        assertThat(unauthenticatedExportResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED_401);
        unauthenticatedExportResponse.close();

        // Create, update, export, export (forbidden)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid));
    }

    @Test
    void testImport() {
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
        final Response authorisedImportResponse = docRefClient.importDocument(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                name,
                true,
                importValues);
        assertThat(authorisedImportResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY importedDocRefEntity = authorisedImportResponse.readEntity(docRefEntityClass);
        assertThat(importedDocRefEntity).isEqualTo(docRefEntity);

        // Fetch the doc ref from the system to check it's been imported ok
        final Response getCheckResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertThat(getCheckResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY getCheckEntity = getCheckResponse.readEntity(docRefEntityClass);
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
