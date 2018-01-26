package stroom.query.testing;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.service.DocRefEntity;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class DocRefResourceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration,
        APP_CLASS extends Application<CONFIG_CLASS>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefResourceIT.class);
    
    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    private final String docRefType;
    protected DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;
    private final StroomAuthenticationRule authRule;

    protected DocRefResourceIT(final Class<DOC_REF_ENTITY> docRefEntityClass,
                               final String docRefType,
                               final int appPort,
                               final StroomAuthenticationRule authRule) {
        this.docRefEntityClass = docRefEntityClass;
        this.docRefType = docRefType;
        this.authRule = authRule;
        this.docRefClient = new DocRefResourceHttpClient<>(String.format("http://localhost:%d", appPort));
    }

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    @Test
    public void testCreate() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.adminUser(), uuid, DocumentPermission.READ);

        final Response unauthorisedCreateResponse = docRefClient.createDocument(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedCreateResponse.getStatus());

        final Response unauthenticatedCreateResponse = docRefClient.createDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedCreateResponse.getStatus());


        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name, parentFolderUuid);
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
        auditLogRule.checkAuditLogs(3);
    }

    @Test
    public void testUpdate() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.UPDATE);

        // Create a document
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

        // Try updating it as an unauthorised user
        final DOC_REF_ENTITY unauthorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthorisedUpdateResponse =  docRefClient.update(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid,
                unauthorisedEntityUpdate);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedUpdateResponse.getStatus());

        // Try updating it as an unauthenticated user
        final DOC_REF_ENTITY unauthenticatedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthenticatedUpdateResponse =  docRefClient.update(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid,
                unauthenticatedEntityUpdate);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedUpdateResponse.getStatus());

        // Check it is still in the state from the authorised update
        final Response getCheckResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());
        final DOC_REF_ENTITY checkEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertEquals(authorisedEntityUpdate, checkEntity);

        // Create, update (ok), update (forbidden), get (check)
        auditLogRule.checkAuditLogs(4);
    }

    @Test
    public void testGetInfo() {
        final Long testStartTime = System.currentTimeMillis();

        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.UPDATE);

        // Create a document
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

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

        // Try to get info as unauthenticated user
        final Response unauthenticatedGetInfoResponse = docRefClient.getInfo(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedGetInfoResponse.getStatus());

        // Create, update (ok), get info (ok), get info (forbidden)
        auditLogRule.checkAuditLogs(4);
    }

    @Test
    public void testGet() {
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.adminUser(), uuid, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);

        final Response createReponse = docRefClient.createDocument(
                authRule.adminUser(),
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createReponse.getStatus());

        final Response getResponseAdmin = docRefClient.get(authRule.adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponseAdmin.getStatus());

        final Response getResponseAuthorisedUser = docRefClient.get(
                authRule.authenticatedUser(authorisedUsername),
                uuid);
        assertEquals(HttpStatus.OK_200, getResponseAuthorisedUser.getStatus());

        final Response getResponseUnauthenticatedUser = docRefClient.get(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, getResponseUnauthenticatedUser.getStatus());

        final Response getResponseUnauthorisedUser = docRefClient.get(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, getResponseUnauthorisedUser.getStatus());

        // Create, get (admin), get (authorized), get (unauthorised)
        auditLogRule.checkAuditLogs(4);
    }

    @Test
    public void testRename() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name1 = UUID.randomUUID().toString();
        final String name2 = UUID.randomUUID().toString();
        final String name3 = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();
        // No specific permissions required for rename (is this right?)

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.adminUser(), uuid, DocumentPermission.READ);

        final Response createResponse = docRefClient.createDocument(
                authRule.adminUser(),
                uuid,
                name1,
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

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

        // Check it still has name2
        final Response getPostFailedRenamesResponse = docRefClient.get(authRule.adminUser(), uuid);
        final DOC_REF_ENTITY updatesPostFailedRenames = getPostFailedRenamesResponse.readEntity(docRefEntityClass);
        assertNotNull(updatesPostFailedRenames);
        assertEquals(name2, updatesPostFailedRenames.getName());

        // Create, rename, get, get (check still got name2)
        auditLogRule.checkAuditLogs(4);
    }

    @Test
    public void testCopy() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid1 = UUID.randomUUID().toString();
        final String uuid2 = UUID.randomUUID().toString();
        final String uuid3 = UUID.randomUUID().toString();
        final String uuid4 = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveFolderCreatePermission(authRule.authenticatedUser(authorisedUsername), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid1, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid2, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid3, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid4, DocumentPermission.READ);

        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid1, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Attempt copy as authorised user
        final Response copyResponse = docRefClient.copyDocument(authRule.authenticatedUser(authorisedUsername), uuid1, uuid2, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, copyResponse.getStatus());

        final DOC_REF_ENTITY copiedEntity = copyResponse.readEntity(docRefEntityClass);
        assertNotNull(copiedEntity);
        assertEquals(uuid2, copiedEntity.getUuid());

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
                uuid3,
                parentFolderUuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedCopyResponse.getStatus());

        final Response getUnauthorisedCopyResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid3);
        assertEquals(HttpStatus.NOT_FOUND_404, getUnauthorisedCopyResponse.getStatus());

        // Attempt copy as unauthenticated user
        final Response unauthenticatedCopyResponse = docRefClient.copyDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid1,
                uuid4,
                parentFolderUuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedCopyResponse.getStatus());

        final Response getUnauthenticatedCopyResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid4);
        assertEquals(HttpStatus.NOT_FOUND_404, getUnauthenticatedCopyResponse.getStatus());

        // Create, copy, get, copy (forbidden), get, get
        auditLogRule.checkAuditLogs(6);
    }

    @Test
    public void testDelete() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(unauthorisedUsername), uuid, DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.DELETE);

        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Make user a user that is not authorised for deletion cannot delete it
        final Response unauthorisedDeleteResponse = docRefClient.deleteDocument(
                authRule.authenticatedUser(unauthorisedUsername),
                uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedDeleteResponse.getStatus());

        // Make sure a user that is not authenticated cannot delete it either
        final Response unauthenticatedDeleteResponse = docRefClient.deleteDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedDeleteResponse.getStatus());

        // Ensure the document is still there
        final Response getStillThereResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getStillThereResponse.getStatus());

        // Check the fully authorised user can delete it
        final Response authorisedDeleteResponse = docRefClient.deleteDocument(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, authorisedDeleteResponse.getStatus());

        // Now check it has been deleted
        final Response getResponse = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.NOT_FOUND_404, getResponse.getStatus());

        // Create, delete (forbidden), get (200), delete, get (404)
        auditLogRule.checkAuditLogs(5);
    }

    @Test
    public void testExport() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.adminUser(), uuid, DocumentPermission.UPDATE);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.EXPORT);

        // Create a document
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(authRule.adminUser(), uuid, entityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

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

        // Try exporting it as an unauthenticated user
        final Response unauthenticatedExportResponse = docRefClient.exportDocument(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedExportResponse.getStatus());

        // Create, update, export, export (forbidden)
        auditLogRule.checkAuditLogs(4);
    }

    @Test
    public void testImport() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();

        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.IMPORT);
        authRule.giveDocumentPermission(authRule.authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);

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
        auditLogRule.checkAuditLogs(2);
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
