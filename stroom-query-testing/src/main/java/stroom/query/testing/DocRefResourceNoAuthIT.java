package stroom.query.testing;

import io.dropwizard.Configuration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.docref.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.security.NoAuthValueFactoryProvider;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static stroom.query.testing.FifoLogbackExtension.containsAllOf;

@ExtendWith(FifoLogbackExtensionSupport.class)
public abstract class DocRefResourceNoAuthIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    private final DropwizardAppExtensionWithClients<CONFIG_CLASS> appRule;
    protected FifoLogbackExtension auditLogRule = new FifoLogbackExtension();
    private DocRefResourceHttpClient<DOC_REF_ENTITY> docRefClient;

    protected DocRefResourceNoAuthIT(final Class<DOC_REF_ENTITY> docRefEntityClass,
                                     final DropwizardAppExtensionWithClients<CONFIG_CLASS> appRule) {
        this.docRefEntityClass = docRefEntityClass;
        this.appRule = appRule;
    }

    @BeforeEach
    void beforeEach() {
        docRefClient = appRule.getClient(DocRefResourceHttpClient::new);
    }

    @Test
    void testCreate() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create the document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY createdEntity = createResponse.readEntity(docRefEntityClass);
        assertThat(createdEntity).isNotNull();
        assertThat(createdEntity.getUuid()).isEqualTo(uuid);
        assertThat(createdEntity.getName()).isEqualTo(name);
        createResponse.close();

        // Get the entity
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY foundEntity = getResponse.readEntity(docRefEntityClass);
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getName()).isEqualTo(name);
        getResponse.close();

        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testUpdate() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create a document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                authorisedEntityUpdate);
        assertThat(updateResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        updateResponse.close();

        // Check it is still in the state from the authorised update
        final Response getCheckResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(getCheckResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        final DOC_REF_ENTITY checkEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertThat(checkEntity).isEqualTo(authorisedEntityUpdate);
        getCheckResponse.close();

        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testGetInfo() {
        final Long testStartTime = System.currentTimeMillis();

        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create a document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                authorisedEntityUpdate);
        assertThat(updateResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        updateResponse.close();

        // Get info as authorised user
        final Response authorisedGetInfoResponse = docRefClient.getInfo(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(authorisedGetInfoResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        final DocRefInfo info = authorisedGetInfoResponse.readEntity(DocRefInfo.class);
        assertThat(info.getCreateTime() >= testStartTime).isTrue();
        assertThat(info.getUpdateTime() > info.getCreateTime()).isTrue();
        assertThat(NoAuthValueFactoryProvider.ADMIN_USER.getName()).isEqualTo(info.getCreateUser());
        assertThat(NoAuthValueFactoryProvider.ADMIN_USER.getName()).isEqualTo(info.getUpdateUser());
        authorisedGetInfoResponse.close();

        // Create, update (ok), get info (ok), get info (forbidden)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF_INFO, uuid));
    }

    @Test
    void testGet() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final Response createReponse = docRefClient.createDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name);
        assertThat(createReponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createReponse.close();

        final Response getResponseAdmin = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(getResponseAdmin.getStatus()).isEqualTo(HttpStatus.OK_200);
        getResponseAdmin.close();

        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testRename() {
        final String uuid = UUID.randomUUID().toString();
        final String name1 = UUID.randomUUID().toString();
        final String name2 = UUID.randomUUID().toString();

        final Response createResponse = docRefClient.createDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name1);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Attempt rename as an authorised user
        final Response renameResponse = docRefClient.renameDocument(
                NoAuthValueFactoryProvider.ADMIN_USER,
                uuid,
                name2);
        assertThat(renameResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY renamedEntity = renameResponse.readEntity(docRefEntityClass);
        assertThat(renamedEntity).isNotNull();
        assertThat(renamedEntity.getName()).isEqualTo(name2);
        renameResponse.close();

        // Check it has the new name
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY updated = getResponse.readEntity(docRefEntityClass);
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo(name2);
        getResponse.close();

        // Create, rename, rename, get (check still got name2)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid)) // create
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.RENAME_DOC_REF, uuid)) // authorised rename
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid)); // check name
    }

    @Test
    void testCopy() {
        final String uuid1 = UUID.randomUUID().toString();
        final String uuid2 = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid1, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Attempt copy as authorised user
        final Response copyResponse = docRefClient.copyDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid1, uuid2);
        assertThat(copyResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY copiedEntity = copyResponse.readEntity(docRefEntityClass);
        assertThat(copiedEntity).isNotNull();
        assertThat(copiedEntity.getUuid()).isEqualTo(uuid2);
        copyResponse.close();

        // Get the copy
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid2);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY updatedEntity = getResponse.readEntity(docRefEntityClass);
        assertThat(updatedEntity).isNotNull();
        assertThat(updatedEntity.getName()).isEqualTo(name);
        assertThat(updatedEntity.getUuid()).isEqualTo(uuid2);
        getResponse.close();

        // Create, copy, get, copy (forbidden), get, get
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid1))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.COPY_DOC_REF, uuid1, uuid2))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid2));
    }

    @Test
    void testDelete() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Check the fully authorised user can delete it
        final Response authorisedDeleteResponse = docRefClient.deleteDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(authorisedDeleteResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        authorisedDeleteResponse.close();

        // Now check it has been deleted
        final Response getResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(getResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        getResponse.close();

        // Create, delete (forbidden), get (200), delete, get (404)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.DELETE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    void testExport() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();

        // Create a document
        final Response createResponse = docRefClient.createDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid, name);
        assertThat(createResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        createResponse.close();

        // Update it with some real details
        final DOC_REF_ENTITY entityUpdate = createPopulatedEntity(uuid, name);
        final Response updateResponse = docRefClient.update(NoAuthValueFactoryProvider.ADMIN_USER, uuid, entityUpdate);
        assertThat(updateResponse.getStatus()).isEqualTo(HttpStatus.OK_200);
        updateResponse.close();

        // Try exporting it as an authorised user
        final Response authorisedExportResponse = docRefClient.exportDocument(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(authorisedExportResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final ExportDTO exportDTO = authorisedExportResponse.readEntity(ExportDTO.class);
        assertThat(exportDTO).isNotNull();
        authorisedExportResponse.close();

        final Map<String, String> expectedExportValues = exportValues(entityUpdate);
        expectedExportValues.put(DocRefEntity.NAME, entityUpdate.getName()); // add common fields
        assertThat(exportDTO.getValues()).isEqualTo(expectedExportValues);

        // Create, update, export, export (forbidden)
        auditLogRule.check()
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid));
    }

    @Test
    void testImport() {
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
        assertThat(authorisedImportResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY importedDocRefEntity = authorisedImportResponse.readEntity(docRefEntityClass);
        assertThat(importedDocRefEntity).isEqualTo(docRefEntity);
        authorisedImportResponse.close();

        // Fetch the doc ref from the system to check it's been imported ok
        final Response getCheckResponse = docRefClient.get(NoAuthValueFactoryProvider.ADMIN_USER, uuid);
        assertThat(getCheckResponse.getStatus()).isEqualTo(HttpStatus.OK_200);

        final DOC_REF_ENTITY getCheckEntity = getCheckResponse.readEntity(docRefEntityClass);
        assertThat(getCheckEntity).isEqualTo(docRefEntity);
        getCheckResponse.close();

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
