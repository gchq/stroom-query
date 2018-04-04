package stroom.query.testing;

import io.dropwizard.Configuration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static stroom.query.testing.FifoLogbackRule.containsAllOf;

public abstract class DocRefResourceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private final String docRefType;
    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    private DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;
    private final StroomAuthenticationRule authRule;

    protected DocRefResourceIT(final String docRefType,
                               final Class<DOC_REF_ENTITY> docRefEntityClass,
                               final DropwizardAppWithClientsRule<CONFIG_CLASS> appRule,
                               final StroomAuthenticationRule authRule) {
        this.docRefType = docRefType;
        this.docRefEntityClass = docRefEntityClass;
        this.authRule = authRule;
        this.docRefClient = appRule.getClient(DocRefResourceHttpClient::new);
    }

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    @Test
    public void testCreate() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();

//        final Response unauthorisedCreateResponse = docRefClient.createDocument(
//                authRule.authenticatedUser(unauthorisedUsername),
//                uuid,
//                name);
//        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedCreateResponse.getStatus());
//        unauthorisedCreateResponse.close();

        final Response unauthenticatedCreateResponse = docRefClient.createDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedCreateResponse.getStatus());
        unauthenticatedCreateResponse.close();

        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        final DOC_REF_ENTITY createdEntity = createResponse.readEntity(docRefEntityClass);
        assertNotNull(createdEntity);
        assertEquals(uuid, createdEntity.getUuid());
        assertEquals(name, createdEntity.getName());

        final Response getResponse = docRefClient.get(authRule.adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY foundEntity = getResponse.readEntity(docRefEntityClass);
        assertNotNull(foundEntity);
        assertEquals(name, foundEntity.getName());

        // Create (forbidden), Create (ok), get
        auditLogRule.check()
                .thereAreAtLeast(2)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testUpdate() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
        final DOC_REF_ENTITY updateResponseBody = updateResponse.readEntity(docRefEntityClass);
        assertEquals(authorisedEntityUpdate, updateResponseBody);

        // Try updating it as an unauthorised user
        final DOC_REF_ENTITY unauthorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthorisedUpdateResponse = docRefClient.update(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid,
                unauthorisedEntityUpdate);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedUpdateResponse.getStatus());
        unauthorisedUpdateResponse.close();

        // Try updating it as an unauthenticated user
        final DOC_REF_ENTITY unauthenticatedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthenticatedUpdateResponse = docRefClient.update(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                unauthenticatedEntityUpdate);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedUpdateResponse.getStatus());
        unauthenticatedUpdateResponse.close();

        // Check it is still in the state from the authorised update
        final Response getCheckResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());
        final DOC_REF_ENTITY checkEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertEquals(authorisedEntityUpdate, checkEntity);

        // Create, update (ok), update (forbidden), get (check)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testGetInfo() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
        updateResponse.close();

        // Get info as authorised user
        final Response authorisedGetInfoResponse = docRefClient.getInfo(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, authorisedGetInfoResponse.getStatus());
        final DocRefInfo info = authorisedGetInfoResponse.readEntity(DocRefInfo.class);
        assertTrue(info.getCreateTime() >= testStartTime);
        assertTrue(info.getUpdateTime() > info.getCreateTime());
        assertEquals(info.getCreateUser(), authRule.adminUser().getName());
        assertEquals(info.getUpdateUser(), authRule.authenticatedUser(authorisedUsername).getName());

        // Try to get info as unauthorised user
        final Response unauthorisedGetInfoResponse = docRefClient.getInfo(authRule.authenticatedUser(unauthorisedUsername), uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedGetInfoResponse.getStatus());
        unauthorisedGetInfoResponse.close();

        // Try to get info as unauthenticated user
        final Response unauthenticatedGetInfoResponse = docRefClient.getInfo(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedGetInfoResponse.getStatus());
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
    public void testGet() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        final Response getResponseAdmin = docRefClient.get(authRule.adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponseAdmin.getStatus());
        getResponseAdmin.close();

        final Response getResponseAuthorisedUser = docRefClient.get(
                authRule.authenticatedUser(authorisedUsername),
                uuid);
        assertEquals(HttpStatus.OK_200, getResponseAuthorisedUser.getStatus());
        getResponseAuthorisedUser.close();

        final Response getResponseUnauthenticatedUser = docRefClient.get(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, getResponseUnauthenticatedUser.getStatus());
        getResponseUnauthenticatedUser.close();

        final Response getResponseUnauthorisedUser = docRefClient.get(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, getResponseUnauthorisedUser.getStatus());
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
    public void testRename() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Attempt rename as an authorised user
        final Response renameResponse = docRefClient.renameDocument(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                name2);
        assertEquals(HttpStatus.OK_200, renameResponse.getStatus());

        final DOC_REF_ENTITY renamedEntity = renameResponse.readEntity(docRefEntityClass);
        assertNotNull(renamedEntity);
        assertEquals(name2, renamedEntity.getName());

        // Check it has the new name
        final Response getResponse = docRefClient.get(authRule.adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY updated = getResponse.readEntity(docRefEntityClass);
        assertNotNull(updated);
        assertEquals(name2, updated.getName());

        // Attempt rename with name3 as unauthenticated user
        final Response unauthenticatedRenameResponse = docRefClient.renameDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name3);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedRenameResponse.getStatus());
        unauthenticatedRenameResponse.close();

        // Check it still has name2
        final Response getPostFailedRenamesResponse = docRefClient.get(authRule.adminUser(), uuid);
        final DOC_REF_ENTITY updatesPostFailedRenames = getPostFailedRenamesResponse.readEntity(docRefEntityClass);
        assertNotNull(updatesPostFailedRenames);
        assertEquals(name2, updatesPostFailedRenames.getName());

        // Create, rename, rename, get (check still got name2)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid)) // create
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.RENAME_DOC_REF, uuid)) // authorised rename
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid)) // check name
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid)); // check name after unauthorised attempt
    }

    @Test
    public void testCopy() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Attempt copy as authorised user
        final Response copyResponse = docRefClient.copyDocument(authRule.authenticatedUser(authorisedUsername), uuid1, uuid2);
        assertEquals(HttpStatus.OK_200, copyResponse.getStatus());

        final DOC_REF_ENTITY copiedEntity = copyResponse.readEntity(docRefEntityClass);
        assertNotNull(copiedEntity);
        assertEquals(uuid2, copiedEntity.getUuid());

        // Get the copy
        final Response getResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid2);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY updatedEntity = getResponse.readEntity(docRefEntityClass);
        assertNotNull(updatedEntity);
        assertEquals(name, updatedEntity.getName());
        assertEquals(uuid2, updatedEntity.getUuid());

        // Attempt copy as unauthorised user
        final Response unauthorisedCopyResponse = docRefClient.copyDocument(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid1,
                uuid3);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedCopyResponse.getStatus());
        unauthorisedCopyResponse.close();

        final Response getUnauthorisedCopyResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid3);
        assertEquals(HttpStatus.NOT_FOUND_404, getUnauthorisedCopyResponse.getStatus());
        getUnauthorisedCopyResponse.close();

        // Attempt copy as unauthenticated user
        final Response unauthenticatedCopyResponse = docRefClient.copyDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid1,
                uuid4);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedCopyResponse.getStatus());
        unauthenticatedCopyResponse.close();

        final Response getUnauthenticatedCopyResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid4);
        assertEquals(HttpStatus.NOT_FOUND_404, getUnauthenticatedCopyResponse.getStatus());
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
    public void testDelete() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Make user a user that is not authorised for deletion cannot delete it
        final Response unauthorisedDeleteResponse = docRefClient.deleteDocument(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedDeleteResponse.getStatus());
        unauthorisedDeleteResponse.close();

        // Make sure a user that is not authenticated cannot delete it either
        final Response unauthenticatedDeleteResponse = docRefClient.deleteDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedDeleteResponse.getStatus());
        unauthenticatedDeleteResponse.close();

        // Ensure the document is still there
        final Response getStillThereResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getStillThereResponse.getStatus());
        getStillThereResponse.close();

        // Check the fully authorised user can delete it
        final Response authorisedDeleteResponse = docRefClient.deleteDocument(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, authorisedDeleteResponse.getStatus());
        authorisedDeleteResponse.close();

        // Now check it has been deleted
        final Response getResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.NOT_FOUND_404, getResponse.getStatus());
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
    public void testExport() {
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
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(authRule.adminUser(), uuid, entityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
        updateResponse.close();

        // Try exporting it as an authorised user
        final Response authorisedExportResponse = docRefClient.exportDocument(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, authorisedExportResponse.getStatus());

        final ExportDTO exportDTO = authorisedExportResponse.readEntity(ExportDTO.class);
        assertNotNull(exportDTO);

        final Map<String, String> expectedExportValues = exportValues(entityUpdate);
        expectedExportValues.put(DocRefEntity.NAME, entityUpdate.getName()); // add common fields
        assertEquals(expectedExportValues, exportDTO.getValues());

        // Try exporting it as an unauthorised user
        final Response unauthorisedExportResponse = docRefClient.exportDocument(authRule.authenticatedUser(unauthorisedUsername), uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedExportResponse.getStatus());
        unauthorisedExportResponse.close();

        // Try exporting it as an unauthenticated user
        final Response unauthenticatedExportResponse = docRefClient.exportDocument(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedExportResponse.getStatus());
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
    public void testImport() {
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
        assertEquals(HttpStatus.OK_200, authorisedImportResponse.getStatus());

        final DOC_REF_ENTITY importedDocRefEntity = authorisedImportResponse.readEntity(docRefEntityClass);
        assertEquals(docRefEntity, importedDocRefEntity);

        // Fetch the doc ref from the system to check it's been imported ok
        final Response getCheckResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());

        final DOC_REF_ENTITY getCheckEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertEquals(docRefEntity, getCheckEntity);

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
