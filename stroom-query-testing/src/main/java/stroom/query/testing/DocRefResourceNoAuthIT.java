package stroom.query.testing;

import io.dropwizard.Configuration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.security.NoAuthValueFactoryProvider;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static stroom.query.testing.FifoLogbackRule.containsAllOf;

public abstract class DocRefResourceNoAuthIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefResourceIT.class);
    
    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    protected DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;

    protected DocRefResourceNoAuthIT(final Class<DOC_REF_ENTITY> docRefEntityClass,
                               final DropwizardAppWithClientsRule<CONFIG_CLASS> appRule) {
        this.docRefEntityClass = docRefEntityClass;
        this.docRefClient = appRule.getClient(DocRefResourceHttpClient::new);
    }

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    @Test
    public void testCreate() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create the document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        final DOC_REF_ENTITY createdEntity = createResponse.readEntity(docRefEntityClass);
        assertNotNull(createdEntity);
        assertEquals(uuid, createdEntity.getUuid());
        assertEquals(name, createdEntity.getName());

        // Get the entity
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY foundEntity = getResponse.readEntity(docRefEntityClass);
        assertNotNull(foundEntity);
        assertEquals(name, foundEntity.getName());

        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testUpdate() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create a document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

        // Check it is still in the state from the authorised update
        final Response getCheckResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());
        final DOC_REF_ENTITY checkEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertEquals(authorisedEntityUpdate, checkEntity);

        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testGetInfo() {
        final Long testStartTime = System.currentTimeMillis();

        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create a document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                authorisedEntityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());

        // Get info as authorised user
        final Response authorisedGetInfoResponse = docRefClient.getInfo(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, authorisedGetInfoResponse.getStatus());
        final DocRefInfo info = authorisedGetInfoResponse.readEntity(DocRefInfo.class);
        assertTrue(info.getCreateTime() >= testStartTime);
        assertTrue(info.getUpdateTime() > info.getCreateTime());
        assertEquals(info.getCreateUser(), NoAuthValueFactoryProvider.ADMIN_USER.getName());
        assertEquals(info.getUpdateUser(), NoAuthValueFactoryProvider.ADMIN_USER.getName());

        // Create, update (ok), get info (ok), get info (forbidden)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF_INFO, uuid));
    }

    @Test
    public void testGet() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final Response createReponse = docRefClient.createDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name,
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createReponse.getStatus());
        createReponse.close();

        final Response getResponseAdmin = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, getResponseAdmin.getStatus());
        getResponseAdmin.close();

        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testRename() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name1 = UUID.randomUUID().toString();
        final String name2 = UUID.randomUUID().toString();

        final Response createResponse = docRefClient.createDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name1,
                parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Attempt rename as an authorised user
        final Response renameResponse = docRefClient.renameDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name2);
        assertEquals(HttpStatus.OK_200, renameResponse.getStatus());

        final DOC_REF_ENTITY renamedEntity = renameResponse.readEntity(docRefEntityClass);
        assertNotNull(renamedEntity);
        assertEquals(name2, renamedEntity.getName());

        // Check it has the new name
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY updated = getResponse.readEntity(docRefEntityClass);
        assertNotNull(updated);
        assertEquals(name2, updated.getName());

        // Create, rename, rename, get (check still got name2)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid)) // create
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.RENAME_DOC_REF, uuid)) // authorised rename
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid)); // check name
    }

    @Test
    public void testCopy() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid1 = UUID.randomUUID().toString();
        final String uuid2 = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid1, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Attempt copy as authorised user
        final Response copyResponse = docRefClient.copyDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid1, uuid2, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, copyResponse.getStatus());

        final DOC_REF_ENTITY copiedEntity = copyResponse.readEntity(docRefEntityClass);
        assertNotNull(copiedEntity);
        assertEquals(uuid2, copiedEntity.getUuid());

        // Get the copy
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid2);
        assertEquals(HttpStatus.OK_200, getResponse.getStatus());

        final DOC_REF_ENTITY updatedEntity = getResponse.readEntity(docRefEntityClass);
        assertNotNull(updatedEntity);
        assertEquals(name, updatedEntity.getName());
        assertEquals(uuid2, updatedEntity.getUuid());

        // Create, copy, get, copy (forbidden), get, get
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid1))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.COPY_DOC_REF, uuid1, uuid2))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid2));
    }

    @Test
    public void testDelete() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Check the fully authorised user can delete it
        final Response authorisedDeleteResponse = docRefClient.deleteDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, authorisedDeleteResponse.getStatus());
        authorisedDeleteResponse.close();

        // Now check it has been deleted
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.NOT_FOUND_404, getResponse.getStatus());
        getResponse.close();

        // Create, delete (forbidden), get (200), delete, get (404)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.DELETE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testExport() {
        final String parentFolderUuid = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create a document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name, parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());
        createResponse.close();

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(NoAuthValueFactoryProvider.ADMIN_USER, uuid, entityUpdate);
        assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
        updateResponse.close();

        // Try exporting it as an authorised user
        final Response authorisedExportResponse = docRefClient.exportDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, authorisedExportResponse.getStatus());

        final ExportDTO exportDTO = authorisedExportResponse.readEntity(ExportDTO.class);
        assertNotNull(exportDTO);

        final Map<String, String> expectedExportValues = exportValues(entityUpdate);
        expectedExportValues.put(DocRefEntity.NAME, entityUpdate.getName()); // add common fields
        assertEquals(expectedExportValues, exportDTO.getValues());

        // Create, update, export, export (forbidden)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid));
    }

    @Test
    public void testImport() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create an entity to import
        final DOC_REF_ENTITY docRefEntity = createPopulatedEntity(uuid, name);
        final Map<String, String> importValues = exportValues(docRefEntity);

        // Try importing it as an authorised user
        final Response authorisedImportResponse = docRefClient.importDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name,
                true,
                importValues);
        assertEquals(HttpStatus.OK_200, authorisedImportResponse.getStatus());

        final DOC_REF_ENTITY importedDocRefEntity = authorisedImportResponse.readEntity(docRefEntityClass);
        assertEquals(docRefEntity, importedDocRefEntity);

        // Fetch the doc ref from the system to check it's been imported ok
        final Response getCheckResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertEquals(HttpStatus.OK_200, getCheckResponse.getStatus());

        final DOC_REF_ENTITY getCheckEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertEquals(docRefEntity, getCheckEntity);

        // import, get
        auditLogRule.check()
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
