package stroom.query.audit.rest;

import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.AuditWrapper;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

public class AuditedDocRefResourceImpl<T extends DocRefEntity> implements DocRefResource<T> {
    private final DocRefService<T> service;

    private final EventLoggingService eventLoggingService;

    private final AuditWrapper auditWrapper;

    private final AuthorisationService authorisationService;

    @Inject
    public AuditedDocRefResourceImpl(final DocRefService<T> service,
                                     final EventLoggingService eventLoggingService,
                                     final AuthorisationService authorisationService) {
        this.service = service;
        this.eventLoggingService = eventLoggingService;
        this.authorisationService = authorisationService;
        this.auditWrapper = new AuditWrapper(eventLoggingService);
    }

    @Override
    public Response getAll(final ServiceUser user){
        return auditWrapper.auditFunction(user,
                () -> Boolean.TRUE,
                () -> Response.ok(service.getAll(user)).build(),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REFS");
                    eventDetail.setDescription("Get the list of doc refs hosted by this service");
                });
    }

    @Override
    public Response get(final ServiceUser user,
                        final String uuid){
        return auditWrapper.auditFunction(user,
                () -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.READ),
                () -> service.get(user, uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REF");
                    eventDetail.setDescription("Get a single doc ref");
                });
    }

    @Override
    public Response getInfo(final ServiceUser user,
                            final String uuid){
        return auditWrapper.auditFunction(user,
                () -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.READ),
                () -> service.getInfo(user, uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REF_INFO");
                    eventDetail.setDescription("Get info for a single doc ref");
                });
    }

    @Override
    public Response createDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name,
                                   final String parentFolderUUID){
        return auditWrapper.auditFunction(user,
                () -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(DocumentPermission.FOLDER)
                                .uuid(parentFolderUUID)
                                .build(),
                        DocumentPermission.CREATE.getTypedPermission(service.getType())),
                () -> service.createDocument(user, uuid, name)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("CREATE_DOC_REF");
                    eventDetail.setDescription("Create a Doc Ref");
                });
    }

    @Override
    public Response update(final ServiceUser user,
                           final String uuid,
                           final T updatedConfig){
        return auditWrapper.auditFunction(user,
                () -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.UPDATE),
                () -> service.update(user, uuid, updatedConfig)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.noContent().build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("UPDATE_DOC_REF");
                    eventDetail.setDescription("Update a Doc Ref");
                });
    }

    @Override
    public Response copyDocument(final ServiceUser user,
                                 final String originalUuid,
                                 final String copyUuid,
                                 final String parentFolderUUID){
        return auditWrapper.auditFunction(user,
                () ->   authorisationService.isAuthorised(user,
                            new DocRef.Builder()
                                    .type(this.service.getType())
                                    .uuid(originalUuid)
                                    .build(),
                            DocumentPermission.READ) &&
                        authorisationService.isAuthorised(user,
                                new DocRef.Builder()
                                    .type(DocumentPermission.FOLDER)
                                    .uuid(parentFolderUUID)
                                    .build(),
                            DocumentPermission.CREATE.getTypedPermission(service.getType())),
                () -> service.copyDocument(user, originalUuid, copyUuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("COPY_DOC_REF");
                    eventDetail.setDescription("Copy a Doc Ref");
                });
    }

    @Override
    public Response moveDocument(final ServiceUser user,
                                 final String uuid,
                                 final String parentFolderUUID){
        return auditWrapper.auditFunction(user,
                () ->   authorisationService.isAuthorised(user,
                            new DocRef.Builder()
                                    .type(this.service.getType())
                                    .uuid(uuid)
                                    .build(),
                            DocumentPermission.READ) &&
                        authorisationService.isAuthorised(user,
                                new DocRef.Builder()
                                        .type(DocumentPermission.FOLDER)
                                        .uuid(parentFolderUUID)
                                        .build(),
                                DocumentPermission.CREATE.getTypedPermission(service.getType())),
                () -> service.moveDocument(user, uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("MOVE_DOC_REF");
                    eventDetail.setDescription("Move a Doc Ref");
                });
    }

    @Override
    public Response renameDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name){
        return auditWrapper.auditFunction(user,
                () -> Boolean.TRUE,
                () -> service.renameDocument(user, uuid, name)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("RENAME_DOC_REF");
                    eventDetail.setDescription("Rename a Doc Ref");
                });
    }

    @Override
    public Response deleteDocument(final ServiceUser user,
                                   final String uuid){
        return auditWrapper.auditFunction(user,
                () -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.DELETE),
                () -> service.deleteDocument(user, uuid).map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("DELETE_DOC_REF");
                    eventDetail.setDescription("Delete a Doc Ref");
                });
    }

    @Override
    public Response importDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap){
        return auditWrapper.auditFunction(user,
                () -> Boolean.TRUE,
                () -> service.importDocument(user, uuid, name, confirmed, dataMap)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("IMPORT_DOC_REF");
                    eventDetail.setDescription("Import a Doc Ref");
                });
    }

    @Override
    public Response exportDocument(final ServiceUser user,
                                   final String uuid){

        return auditWrapper.auditFunction(user,
                () -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.EXPORT),
                () -> {
                    final ExportDTO result = service.exportDocument(user, uuid);
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
