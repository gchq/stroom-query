package stroom.query.audit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import event.logging.Event;
import event.logging.EventLoggingService;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import event.logging.Search;
import org.eclipse.jetty.http.HttpStatus;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.SimpleAuditWrapper;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

public class AuditedDocRefResourceImpl<T extends DocRefEntity> implements DocRefResource {
    private final Class<T> docRefEntityClass;

    private final DocRefService<T> service;

    private final EventLoggingService eventLoggingService;

    private final AuthorisationService authorisationService;

    @Inject
    public AuditedDocRefResourceImpl(final DocRefService<T> service,
                                     final EventLoggingService eventLoggingService,
                                     final AuthorisationService authorisationService,
                                     final DocRefEntity.ClassProvider<T> docRefEntityClassSupplier) {
        this.service = service;
        this.eventLoggingService = eventLoggingService;
        this.authorisationService = authorisationService;
        this.docRefEntityClass = docRefEntityClassSupplier.get();
    }

    public static final String GET_ALL_DOC_REFS = "GET_ALL_DOC_REFS";

    @Override
    public Response getAll(final ServiceUser user){
        return SimpleAuditWrapper.withUser(user)
                .withDefaultAuthSupplier()
                .withResponse(() -> Response.ok(service.getAll(user)).build())
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(GET_ALL_DOC_REFS);
                    eventDetail.setDescription("Get the list of doc refs hosted by this service");
                }).callAndAudit(eventLoggingService);
    }

    public static final String GET_DOC_REF = "GET_DOC_REF";

    @Override
    public Response get(final ServiceUser user,
                        final String uuid){
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.READ))
                .withResponse(() -> service.get(user, uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(GET_DOC_REF);
                    eventDetail.setDescription("Get a single doc ref");

                    final Search search = new Search();
                    search.setId(uuid);
                    eventDetail.setSearch(search);
                }).callAndAudit(eventLoggingService);
    }

    public static final String GET_DOC_REF_INFO = "GET_DOC_REF_INFO";

    @Override
    public Response getInfo(final ServiceUser user,
                            final String uuid){
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.READ))
                .withResponse(() -> service.getInfo(user, uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(GET_DOC_REF_INFO);
                    eventDetail.setDescription("Get info for a single doc ref");

                    final Search search = new Search();
                    search.setId(uuid);
                    eventDetail.setSearch(search);
                }).callAndAudit(eventLoggingService);
    }

    public static final String CREATE_DOC_REF = "CREATE_DOC_REF";

    @Override
    public Response createDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name,
                                   final String parentFolderUUID){
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(DocumentPermission.FOLDER)
                                .uuid(parentFolderUUID)
                                .build(),
                        DocumentPermission.CREATE.getTypedPermission(service.getType())))
                .withResponse(() -> service.createDocument(user, uuid, name)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(CREATE_DOC_REF);
                    eventDetail.setDescription("Create a Doc Ref");

                    final ObjectOutcome createObj = new ObjectOutcome();
                    final Outcome create = new Outcome();
                    createObj.setOutcome(create);
                    create.setDescription(String.format("Create document %s with name %s in folder %s", uuid, name, parentFolderUUID));
                    eventDetail.setCreate(createObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String UPDATE_DOC_REF = "UPDATE_DOC_REF";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response update(final ServiceUser user,
                           final String uuid,
                           final String updatedConfigJson) {
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.UPDATE))
                .withResponse(() -> {
                    final T updatedConfig = objectMapper.readValue(updatedConfigJson, docRefEntityClass);
                    return service.update(user, uuid, updatedConfig)
                            .map(d -> Response.ok(d).build())
                            .orElse(Response.noContent().build());
                })
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(UPDATE_DOC_REF);
                    eventDetail.setDescription("Update a Doc Ref");

                    final Event.EventDetail.Update updateObj = new Event.EventDetail.Update();
                    final Outcome update = new Outcome();
                    updateObj.setOutcome(update);
                    update.setDescription(String.format("Update document %s", uuid));
                    eventDetail.setUpdate(updateObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String COPY_DOC_REF = "COPY_DOC_REF";

    @Override
    public Response copyDocument(final ServiceUser user,
                                 final String originalUuid,
                                 final String copyUuid,
                                 final String parentFolderUUID){
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
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
                            DocumentPermission.CREATE.getTypedPermission(service.getType())))
                .withResponse(() -> service.copyDocument(user, originalUuid, copyUuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(COPY_DOC_REF);
                    eventDetail.setDescription("Copy a Doc Ref");

                    final ObjectOutcome createObj = new ObjectOutcome();
                    final Outcome create = new Outcome();
                    createObj.setOutcome(create);
                    create.setDescription(String.format("Create copy of %s to %s in folder %s", originalUuid, copyUuid, parentFolderUUID));
                    eventDetail.setCreate(createObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String MOVE_DOC_REF = "MOVE_DOC_REF";

    @Override
    public Response moveDocument(final ServiceUser user,
                                 final String uuid,
                                 final String parentFolderUUID){
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() ->   authorisationService.isAuthorised(user,
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
                                DocumentPermission.CREATE.getTypedPermission(service.getType())))
                .withResponse(() -> service.moveDocument(user, uuid)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(MOVE_DOC_REF);
                    eventDetail.setDescription("Move a Doc Ref");

                    final Event.EventDetail.Update updateObj = new Event.EventDetail.Update();
                    final Outcome update = new Outcome();
                    updateObj.setOutcome(update);
                    update.setDescription(String.format("Move document %s to %s", uuid, parentFolderUUID));
                    eventDetail.setUpdate(updateObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String RENAME_DOC_REF = "RENAME_DOC_REF";

    @Override
    public Response renameDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name){
        return SimpleAuditWrapper.withUser(user)
                .withDefaultAuthSupplier()
                .withResponse(() -> service.renameDocument(user, uuid, name)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(RENAME_DOC_REF);
                    eventDetail.setDescription("Rename a Doc Ref");

                    final Event.EventDetail.Update updateObj = new Event.EventDetail.Update();
                    final Outcome update = new Outcome();
                    updateObj.setOutcome(update);
                    update.setDescription(String.format("Rename document %s to %s", uuid, name));
                    eventDetail.setUpdate(updateObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String DELETE_DOC_REF = "DELETE_DOC_REF";

    @Override
    public Response deleteDocument(final ServiceUser user,
                                   final String uuid){
        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.DELETE))
                .withResponse(() -> service.deleteDocument(user, uuid).map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(DELETE_DOC_REF);
                    eventDetail.setDescription("Delete a Doc Ref");

                    final ObjectOutcome deleteObj = new ObjectOutcome();
                    final Outcome delete = new Outcome();
                    deleteObj.setOutcome(delete);
                    delete.setDescription(String.format("Delete document %s", uuid));
                    eventDetail.setDelete(deleteObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String IMPORT_DOC_REF = "IMPORT_DOC_REF";

    @Override
    public Response importDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap){
        return SimpleAuditWrapper.withUser(user)
                .withDefaultAuthSupplier()
                .withResponse(() -> service.importDocument(user, uuid, name, confirmed, dataMap)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(IMPORT_DOC_REF);
                    eventDetail.setDescription("Import a Doc Ref");

                    final ObjectOutcome createObj = new ObjectOutcome();
                    final Outcome create = new Outcome();
                    createObj.setOutcome(create);
                    create.setDescription(String.format("Import document %s with name %s, confirmed: %s", uuid, name, Boolean.toString(confirmed)));
                    eventDetail.setCreate(createObj);
                }).callAndAudit(eventLoggingService);
    }

    public static final String EXPORT_DOC_REF = "EXPORT_DOC_REF";

    @Override
    public Response exportDocument(final ServiceUser user,
                                   final String uuid){

        return SimpleAuditWrapper.withUser(user)
                .withAuthSupplier(() -> authorisationService.isAuthorised(user,
                        new DocRef.Builder()
                                .type(this.service.getType())
                                .uuid(uuid)
                                .build(),
                        DocumentPermission.EXPORT))
                .withResponse(() -> {
                    final ExportDTO result = service.exportDocument(user, uuid);
                    if (result.getValues().size() > 0) {
                        return Response.ok(result).build();
                    } else {
                        return Response.status(HttpStatus.NOT_FOUND_404)
                                .entity(result)
                                .build();
                    }
                })
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(EXPORT_DOC_REF);
                    eventDetail.setDescription("Export a single doc ref");

                    final Search search = new Search();
                    search.setId(uuid);
                    search.setDescription(String.format("Export Document %s", uuid));
                    eventDetail.setSearch(search);
                }).callAndAudit(eventLoggingService);
    }
}
