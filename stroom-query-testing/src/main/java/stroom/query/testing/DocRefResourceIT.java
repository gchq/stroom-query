package stroom.query.testing;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.service.DocRefEntity;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public abstract class DocRefResourceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration,
        APP_CLASS extends Application<CONFIG_CLASS>>
        extends AbstractIT<DOC_REF_ENTITY, CONFIG_CLASS, APP_CLASS> {

    protected DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;

    protected DocRefResourceIT(final Class<APP_CLASS> appClass,
                               final Class<DOC_REF_ENTITY> docRefEntityClass,
                               final String docRefType) {
        super(appClass, docRefEntityClass, docRefType);
    }

    @Before
    public void beforeTest() {
        docRefClient = new DocRefResourceHttpClient<>(getAppHost());
    }

    @Test
    public void testCreate() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(adminUser(), uuid, DocumentPermission.READ);

        final Response unauthorisedCreateResponse = docRefClient.createDocument(
                authenticatedUser(unauthorisedUsername),
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedCreateResponse.getStatus());

        final Response unauthenticatedCreateResponse = docRefClient.createDocument(
                unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedCreateResponse.getStatus());


        final Response createResponse = docRefClient.createDocument(adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        final DOC_REF_ENTITY createdEntity = getEntityFromBody(createResponse);
        assertNotNull(createdEntity);
        assertEquals(uuid, createdEntity.getUuid());
        assertEquals(name, createdEntity.getName());

        final Response getResponse = docRefClient.get(adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY foundEntity = getEntityFromBody(getResponse);
        assertNotNull(foundEntity);
        assertEquals(name, foundEntity.getName());

        // Create (forbidden), Create (ok), get
        checkAuditLogs(3);
    }

    @Test
    public void testUpdate() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.UPDATE);

        // Create a document
        final Response createResponse = docRefClient.createDocument(adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

        // Try updating it as an unauthorised user
        final DOC_REF_ENTITY unauthorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthorisedUpdateResponse =  docRefClient.update(
                authenticatedUser(unauthorisedUsername),
                uuid,
                unauthorisedEntityUpdate);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedUpdateResponse.getStatus());

        // Try updating it as an unauthenticated user
        final DOC_REF_ENTITY unauthenticatedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response unauthenticatedUpdateResponse =  docRefClient.update(
                unauthenticatedUser(unauthenticatedUsername),
                uuid,
                unauthenticatedEntityUpdate);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedUpdateResponse.getStatus());

        // Check it is still in the state from the authorised update
        final Response getCheckResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());
        final DOC_REF_ENTITY checkEntity = getEntityFromBody(getCheckResponse);
        assertEquals(authorisedEntityUpdate, checkEntity);

        // Create, update (ok), update (forbidden), get (check)
        checkAuditLogs(4);
    }

    @Test
    public void testGet() {
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(adminUser(), uuid, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);

        final Response createReponse = docRefClient.createDocument(
                adminUser(),
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createReponse.getStatus());

        final Response getResponseAdmin = docRefClient.get(adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponseAdmin.getStatus());

        final Response getResponseAuthorisedUser = docRefClient.get(
                authenticatedUser(authorisedUsername),
                uuid);
        assertEquals(HttpStatus.OK_200, getResponseAuthorisedUser.getStatus());

        final Response getResponseUnauthenticatedUser = docRefClient.get(
                unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, getResponseUnauthenticatedUser.getStatus());

        final Response getResponseUnauthorisedUser = docRefClient.get(
                authenticatedUser(unauthorisedUsername),
                uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, getResponseUnauthorisedUser.getStatus());

        // Create, get (admin), get (authorized), get (unauthorised)
        checkAuditLogs(4);
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

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(adminUser(), uuid, DocumentPermission.READ);

        final Response createResponse = docRefClient.createDocument(
                adminUser(),
                uuid,
                name1,
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Attempt rename as an authorised user
        final Response renameResponse = docRefClient.renameDocument(
                authenticatedUser(authorisedUsername),
                uuid,
                name2);
        assertEquals(HttpStatus.OK_200, renameResponse.getStatus());

        final DOC_REF_ENTITY renamedEntity = getEntityFromBody(renameResponse);
        assertNotNull(renamedEntity);
        assertEquals(name2, renamedEntity.getName());

        // Check it has the new name
        final Response getResponse = docRefClient.get(adminUser(), uuid);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY updated = getEntityFromBody(getResponse);
        assertNotNull(updated);
        assertEquals(name2, updated.getName());

        // Attempt rename with name3 as unauthenticated user
        final Response unauthenticatedRenameResponse = docRefClient.renameDocument(
                unauthenticatedUser(unauthenticatedUsername),
                uuid,
                name3);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedRenameResponse.getStatus());

        // Check it still has name2
        final Response getPostFailedRenamesResponse = docRefClient.get(adminUser(), uuid);
        final DOC_REF_ENTITY updatesPostFailedRenames = getEntityFromBody(getPostFailedRenamesResponse);
        assertNotNull(updatesPostFailedRenames);
        assertEquals(name2, updatesPostFailedRenames.getName());

        // Create, rename, get, get (check still got name2)
        checkAuditLogs(4);
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

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveFolderCreatePermission(authenticatedUser(authorisedUsername), parentFolderUuid);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid1, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid2, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid3, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid4, DocumentPermission.READ);

        final Response createResponse = docRefClient.createDocument(adminUser(), uuid1, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Attempt copy as authorised user
        final Response copyResponse = docRefClient.copyDocument(authenticatedUser(authorisedUsername), uuid1, uuid2, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, copyResponse.getStatus());

        final DOC_REF_ENTITY copiedEntity = getEntityFromBody(copyResponse);
        assertNotNull(copiedEntity);
        assertEquals(uuid2, copiedEntity.getUuid());

        final Response getResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid2);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY updatedEntity = getEntityFromBody(getResponse);
        assertNotNull(updatedEntity);
        assertEquals(name, updatedEntity.getName());
        assertEquals(uuid2, updatedEntity.getUuid());

        // Attempt copy as unauthorised user
        final Response unauthorisedCopyResponse = docRefClient.copyDocument(
                authenticatedUser(unauthorisedUsername),
                uuid1,
                uuid3,
                parentFolderUuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedCopyResponse.getStatus());

        final Response getUnauthorisedCopyResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid3);
        assertEquals(HttpStatus.NOT_FOUND_404, getUnauthorisedCopyResponse.getStatus());

        // Attempt copy as unauthenticated user
        final Response unauthenticatedCopyResponse = docRefClient.copyDocument(
                unauthenticatedUser(unauthenticatedUsername),
                uuid1,
                uuid4,
                parentFolderUuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedCopyResponse.getStatus());

        final Response getUnauthenticatedCopyResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid4);
        assertEquals(HttpStatus.NOT_FOUND_404, getUnauthenticatedCopyResponse.getStatus());

        // Create, copy, get, copy (forbidden), get, get
        checkAuditLogs(6);
    }

    @Test
    public void testDelete() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(unauthorisedUsername), uuid, DocumentPermission.READ);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.DELETE);

        final Response createResponse = docRefClient.createDocument(adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Make user a user that is not authorised for deletion cannot delete it
        final Response unauthorisedDeleteResponse = docRefClient.deleteDocument(
                authenticatedUser(unauthorisedUsername),
                uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedDeleteResponse.getStatus());

        // Make sure a user that is not authenticated cannot delete it either
        final Response unauthenticatedDeleteResponse = docRefClient.deleteDocument(
                unauthenticatedUser(unauthenticatedUsername),
                uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedDeleteResponse.getStatus());

        // Ensure the document is still there
        final Response getStillThereResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getStillThereResponse.getStatus());

        // Check the fully authorised user can delete it
        final Response authorisedDeleteResponse = docRefClient.deleteDocument(authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, authorisedDeleteResponse.getStatus());

        // Now check it has been deleted
        final Response getResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.NOT_FOUND_404, getResponse.getStatus());

        // Create, delete (forbidden), get (200), delete, get (404)
        checkAuditLogs(5);
    }

    @Test
    public void testExport() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();
        final String unauthorisedUsername = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(adminUser(), uuid, DocumentPermission.UPDATE);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.EXPORT);

        // Create a document
        final Response createResponse = docRefClient.createDocument(adminUser(), uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(adminUser(), uuid, entityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

        // Try exporting it as an authorised user
        final Response authorisedExportResponse = docRefClient.exportDocument(authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, authorisedExportResponse.getStatus());
        final ExportDTO exportDTO = getFromBody(authorisedExportResponse, ExportDTO.class);
        assertNotNull(exportDTO);
        final Map<String, String> expectedExportValues = exportValues(entityUpdate);
        assertEquals(expectedExportValues, exportDTO.getValues());

        // Try exporting it as an unauthorised user
        final Response unauthorisedExportResponse = docRefClient.exportDocument(authenticatedUser(unauthorisedUsername), uuid);
        assertEquals(HttpStatus.FORBIDDEN_403, unauthorisedExportResponse.getStatus());

        // Try exporting it as an unauthenticated user
        final Response unauthenticatedExportResponse = docRefClient.exportDocument(unauthenticatedUser(unauthenticatedUsername), uuid);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedExportResponse.getStatus());

        // Create, update, export, export (forbidden)
        checkAuditLogs(4);
    }

    @Test
    public void testImport() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String authorisedUsername = UUID.randomUUID().toString();

        giveFolderCreatePermission(adminUser(), parentFolderUuid);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.IMPORT);
        giveDocumentPermission(authenticatedUser(authorisedUsername), uuid, DocumentPermission.READ);

        // Create an entity to import
        final DOC_REF_ENTITY docRefEntity = createPopulatedEntity(uuid, name);
        final Map<String, String> importValues = exportValues(docRefEntity);

        // Try importing it as an authorised user
        final Response authorisedImportResponse = docRefClient.importDocument(
                authenticatedUser(authorisedUsername),
                uuid,
                name,
                true,
                importValues);
        assertEquals(HttpStatus.OK_200, authorisedImportResponse.getStatus());

        final DOC_REF_ENTITY importedDocRefEntity = getEntityFromBody(authorisedImportResponse);
        assertEquals(docRefEntity, importedDocRefEntity);

        // Fetch the doc ref from the system to check it's been imported ok
        final Response getCheckResponse = docRefClient.get(authenticatedUser(authorisedUsername), uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());

        final DOC_REF_ENTITY getCheckEntity = getEntityFromBody(getCheckResponse);
        assertEquals(docRefEntity, getCheckEntity);

        // import, get
        checkAuditLogs(2);
    }

    private DOC_REF_ENTITY createPopulatedEntity(final String uuid, final String name) {
        return new DocRefEntity.Builder<>(createPopulatedEntity())
                .uuid(uuid)
                .name(name)
                .build();
    }

    protected abstract DOC_REF_ENTITY createPopulatedEntity();

    protected abstract Map<String, String> exportValues(DOC_REF_ENTITY docRefEntity);

    private <T> T getFromBody(final Response response, Class<T> theClass) {
        try {
            return jacksonObjectMapper.readValue(response.readEntity(String.class), theClass);
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
            return null;
        }

    }

    private DOC_REF_ENTITY getEntityFromBody(final Response response) {
        return getFromBody(response, getDocRefEntityClass());
    }
}
