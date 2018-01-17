package stroom.query.audit.rest;

import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.AuditWrapper;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

public class AuditedDocRefResourceImpl<T> implements DocRefResource<T> {
    private final DocRefService<T> service;

    private final EventLoggingService eventLoggingService;

    private final AuditWrapper<QueryApiException> auditWrapper;

    private final AuthorisationService authorisationService;

    @Inject
    public AuditedDocRefResourceImpl(final DocRefService<T> service,
                                     final EventLoggingService eventLoggingService,
                                     final AuthorisationService authorisationService) {
        this.service = service;
        this.eventLoggingService = eventLoggingService;
        this.authorisationService = authorisationService;
        this.auditWrapper = new AuditWrapper<>(eventLoggingService, QueryApiException.class, QueryApiException::new);
    }

    @Override
    public Response getAll(final ServiceUser authenticatedServiceUser) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> Boolean.TRUE,
                () -> Response.ok(service.getAll()).build(),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REFS");
                    eventDetail.setDescription("Get the list of doc refs hosted by this service");
                });
    }

    @Override
    public Response get(final ServiceUser authenticatedServiceUser,
                        final String uuid) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> authorisationService.isAuthorised(authenticatedServiceUser,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.READ),
                () -> service.get(uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REF");
                    eventDetail.setDescription("Get a single doc ref");
                });
    }

    @Override
    public Response getInfo(final ServiceUser authenticatedServiceUser,
                            final String uuid) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> authorisationService.isAuthorised(authenticatedServiceUser,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.READ),
                () -> service.getInfo(uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REF_INFO");
                    eventDetail.setDescription("Get info for a single doc ref");
                });
    }

    @Override
    public Response createDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid,
                                   final String name,
                                   final String parentFolderUUID) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> authorisationService.isAuthorised(authenticatedServiceUser,
                        new DocRef.Builder()
                                .type(DocumentPermission.FOLDER)
                                .uuid(parentFolderUUID)
                                .build(),
                        DocumentPermission.CREATE.getTypedPermission(service.getType())),
                () -> service.createDocument(uuid, name)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("CREATE_DOC_REF");
                    eventDetail.setDescription("Create a Doc Ref");
                });
    }

    @Override
    public Response update(final ServiceUser authenticatedServiceUser,
                           final String uuid,
                           final T updatedConfig) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> authorisationService.isAuthorised(authenticatedServiceUser,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.UPDATE),
                () -> service.update(uuid, updatedConfig)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.noContent().build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("UPDATE_DOC_REF");
                    eventDetail.setDescription("Update a Doc Ref");
                });
    }

    @Override
    public Response copyDocument(final ServiceUser authenticatedServiceUser,
                                 final String originalUuid,
                                 final String copyUuid,
                                 final String parentFolderUUID) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () ->   authorisationService.isAuthorised(authenticatedServiceUser,
                            new DocRef.Builder()
                                    .type(this.service.getType())
                                    .uuid(originalUuid)
                                    .build(),
                            DocumentPermission.READ) &&
                        authorisationService.isAuthorised(authenticatedServiceUser,
                                new DocRef.Builder()
                                    .type(DocumentPermission.FOLDER)
                                    .uuid(parentFolderUUID)
                                    .build(),
                            DocumentPermission.CREATE.getTypedPermission(service.getType())),
                () -> service.copyDocument(originalUuid, copyUuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("COPY_DOC_REF");
                    eventDetail.setDescription("Copy a Doc Ref");
                });
    }

    @Override
    public Response moveDocument(final ServiceUser authenticatedServiceUser,
                                 final String uuid,
                                 final String parentFolderUUID) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () ->   authorisationService.isAuthorised(authenticatedServiceUser,
                            new DocRef.Builder()
                                    .type(this.service.getType())
                                    .uuid(uuid)
                                    .build(),
                            DocumentPermission.READ) &&
                        authorisationService.isAuthorised(authenticatedServiceUser,
                                new DocRef.Builder()
                                        .type(DocumentPermission.FOLDER)
                                        .uuid(parentFolderUUID)
                                        .build(),
                                DocumentPermission.CREATE.getTypedPermission(service.getType())),
                () -> service.documentMoved(uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("MOVE_DOC_REF");
                    eventDetail.setDescription("Move a Doc Ref");
                });
    }

    @Override
    public Response renameDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid,
                                   final String name) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> Boolean.TRUE,
                () -> service.documentRenamed(uuid, name)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("RENAME_DOC_REF");
                    eventDetail.setDescription("Rename a Doc Ref");
                });
    }

    @Override
    public Response deleteDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> authorisationService.isAuthorised(authenticatedServiceUser,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.DELETE),
                () -> service.deleteDocument(uuid).map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("DELETE_DOC_REF");
                    eventDetail.setDescription("Delete a Doc Ref");
                });
    }

    @Override
    public Response importDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap) throws QueryApiException {
        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> Boolean.TRUE,
                () -> service.importDocument(uuid, name, confirmed, dataMap)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("IMPORT_DOC_REF");
                    eventDetail.setDescription("Import a Doc Ref");
                });
    }

    @Override
    public Response exportDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid) throws QueryApiException {

        return auditWrapper.auditFunction(authenticatedServiceUser,
                () -> authorisationService.isAuthorised(authenticatedServiceUser,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.EXPORT),
                () -> {
                    final ExportDTO result = service.exportDocument(uuid);
                    if (result.getValues().size() > 0) {
                        return Response.ok(result).build();
                    } else {
                        return Response.status(HttpStatus.NOT_FOUND_404)
                                .entity(result)
                                .build();
                    }
                },
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("EXPORT_DOC_REF");
                    eventDetail.setDescription("Export a single doc ref");
                });
    }
}
