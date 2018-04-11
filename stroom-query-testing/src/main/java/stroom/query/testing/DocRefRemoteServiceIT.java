package stroom.query.testing;

import io.dropwizard.Configuration;
import org.junit.Rule;
import org.junit.Test;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefServiceHttpClient;
import stroom.query.audit.client.NotFoundException;
import stroom.query.audit.client.UnauthenticatedException;
import stroom.query.audit.client.UnauthorisedException;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.service.QueryApiException;

import java.util.Map;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static stroom.query.testing.FifoLogbackRule.containsAllOf;

public abstract class DocRefRemoteServiceIT<
        DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration> {

    private final String docRefType;
    private DocRefServiceHttpClient<DOC_REF_ENTITY> docRefClient;
    private final StroomAuthenticationRule authRule;

    protected DocRefRemoteServiceIT(final String docRefType,
                                    final Class<DOC_REF_ENTITY> docRefEntityClass,
                                    final DropwizardAppWithClientsRule<CONFIG_CLASS> appRule,
                                    final StroomAuthenticationRule authRule) {
        this.docRefType = docRefType;
        this.authRule = authRule;
        this.docRefClient = appRule.getClient(u -> new DocRefServiceHttpClient<>(docRefType, docRefEntityClass, u));
    }

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    @Test
    public void testCreate() throws QueryApiException {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final String unauthenticatedUsername = UUID.randomUUID().toString();

        authRule.permitAdminUser()
                .done();
        authRule.permitAdminUser()
                .docRef(uuid, docRefType)
                .permission(DocumentPermission.READ)
                .done();

        try {
            docRefClient.createDocument(
                    authRule.unauthenticatedUser(unauthenticatedUsername),
                    uuid,
                    name);
            fail();
        } catch (final UnauthenticatedException e) {
            // Correct
        }

        final DOC_REF_ENTITY createdEntity = docRefClient.createDocument(authRule.adminUser(), uuid, name)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        assertEquals(uuid, createdEntity.getUuid());
        assertEquals(name, createdEntity.getName());

        final DOC_REF_ENTITY foundEntity = docRefClient.get(authRule.adminUser(), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertNotNull(foundEntity);
        assertEquals(name, foundEntity.getName());

        // Create (forbidden), Create (ok), get
        auditLogRule.check()
                .thereAreAtLeast(2)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testUpdate() throws QueryApiException {
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
        assertEquals(authorisedEntityUpdate, updateResponseBody);

        // Try updating it as an unauthorised user
        final DOC_REF_ENTITY unauthorisedEntityUpdate = createPopulatedEntity(uuid, name);
        try {
            docRefClient.update(
                    authRule.authenticatedUser(unauthorisedUsername),
                    uuid,
                    unauthorisedEntityUpdate);
            fail();
        } catch (final UnauthorisedException e) {
            // good
        }

        // Try updating it as an unauthenticated user
        final DOC_REF_ENTITY unauthenticatedEntityUpdate = createPopulatedEntity(uuid, name);
        try {
            docRefClient.update(
                    authRule.unauthenticatedUser(unauthenticatedUsername),
                    uuid,
                    unauthenticatedEntityUpdate);
            fail();
        } catch (final UnauthenticatedException e) {
            // good
        }

        // Check it is still in the state from the authorised update
        final DOC_REF_ENTITY checkEntity = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
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
    public void testGetInfo() throws QueryApiException {
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
                .orElseThrow(() -> new AssertionError("Response body missing"));;

        // Update it as authorised user
        final DOC_REF_ENTITY authorisedEntityUpdate = createPopulatedEntity(uuid, name);
        docRefClient.update(
                authRule.authenticatedUser(authorisedUsername),
                uuid,
                authorisedEntityUpdate);

        // Get info as authorised user
        final DocRefInfo info = docRefClient.getInfo(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertTrue(info.getCreateTime() >= testStartTime);
        assertTrue(info.getUpdateTime() > info.getCreateTime());
        assertEquals(info.getCreateUser(), authRule.adminUser().getName());
        assertEquals(info.getUpdateUser(), authRule.authenticatedUser(authorisedUsername).getName());

        // Try to get info as unauthorised user
        try {
            docRefClient.getInfo(authRule.authenticatedUser(unauthorisedUsername), uuid);
            fail();
        } catch (UnauthorisedException e) {
            // good
        }

        // Try to get info as unauthenticated user
        try {
            docRefClient.getInfo(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
            fail("Should have failed as unauthenticated");
        } catch (UnauthenticatedException e) {
            // good
        }

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
    public void testGet() throws QueryApiException {
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
                .orElseThrow(() -> new AssertionError("Response body missing"));;

        docRefClient.get(authRule.adminUser(), uuid);

        docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        try {
            docRefClient.get(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
            fail();
        } catch (final UnauthenticatedException e) {
            // good
        }

        try {
            docRefClient.get(authRule.authenticatedUser(unauthorisedUsername), uuid);
            fail();
        } catch (final UnauthorisedException e) {
            // good
        }

        // Create, get (admin), get (authorized), get (unauthorised)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.GET_DOC_REF, uuid));
    }

    @Test
    public void testRename() throws QueryApiException {
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
        assertEquals(name2, renamedEntity.getName());

        // Check it has the new name
        final DOC_REF_ENTITY updated = docRefClient.get(authRule.adminUser(), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertEquals(name2, updated.getName());

        // Attempt rename with name3 as unauthenticated user
        try {
            docRefClient.renameDocument(
                    authRule.unauthenticatedUser(unauthenticatedUsername),
                    uuid,
                    name3);
            fail();
        } catch (UnauthenticatedException e) {
            // good
        }

        // Check it still has name2
        final DOC_REF_ENTITY updatesPostFailedRenames = docRefClient.get(authRule.adminUser(), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));
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
    public void testCopy() throws QueryApiException {
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
        assertEquals(uuid2, copiedEntity.getUuid());

        // Get the copy
        final DOC_REF_ENTITY updatedEntity = docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid2)
                .orElseThrow(() -> new AssertionError("Response body missing"));
        assertEquals(name, updatedEntity.getName());
        assertEquals(uuid2, updatedEntity.getUuid());

        // Attempt copy as unauthorised user
        try {
            docRefClient.copyDocument(
                    authRule.authenticatedUser(unauthorisedUsername),
                    uuid1,
                    uuid3);
            fail();
        } catch (UnauthorisedException e) {
            // good
        }

        try {
            docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid3);
            fail();
        } catch (NotFoundException e) {
            // good
        }

        // Attempt copy as unauthenticated user
        try {
            docRefClient.copyDocument(
                    authRule.unauthenticatedUser(unauthenticatedUsername),
                    uuid1,
                    uuid4);
            fail();
        } catch (UnauthenticatedException e) {
            // good
        }

        try {
            docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid4);
            fail();
        } catch (NotFoundException e) {
            // good
        }

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
    public void testDelete() throws QueryApiException {
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
        try {
            docRefClient.deleteDocument(
                    authRule.authenticatedUser(unauthorisedUsername),
                    uuid);
            fail();
        } catch (UnauthorisedException e) {
            // good
        }

        // Make sure a user that is not authenticated cannot delete it either
        try {
            docRefClient.deleteDocument(
                authRule.unauthenticatedUser(unauthenticatedUsername),
                uuid);
            fail();
        } catch (UnauthenticatedException e) {
            // good
        }

        // Ensure the document is still there
        docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Check the fully authorised user can delete it
        docRefClient.deleteDocument(authRule.authenticatedUser(authorisedUsername), uuid)
                .orElseThrow(() -> new AssertionError("Response body missing"));

        // Now check it has been deleted
        try {
            docRefClient.get(authRule.authenticatedUser(authorisedUsername), uuid);
            fail();
        } catch (NotFoundException e) {
            // good
        }

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
    public void testExport() throws QueryApiException {
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
        assertEquals(expectedExportValues, exportDTO.getValues());

        // Try exporting it as an unauthorised user
        try {
            docRefClient.exportDocument(authRule.authenticatedUser(unauthorisedUsername), uuid);
            fail();
        } catch (UnauthorisedException e) {
            // good
        }

        // Try exporting it as an unauthenticated user
        try {
            docRefClient.exportDocument(authRule.unauthenticatedUser(unauthenticatedUsername), uuid);
            fail();
        } catch (UnauthenticatedException e) {
            // good
        }

        // Create, update, export, export (forbidden)
        auditLogRule.check()
                .thereAreAtLeast(4)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.UPDATE_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.EXPORT_DOC_REF, uuid));
    }

    @Test
    public void testImport() throws QueryApiException {
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
