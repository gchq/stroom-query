package stroom.query.audit;

import event.logging.EventLoggingService;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;

public class AuditedDocRefResourceImpl implements DocRefResource {
    private final DocRefResource docRefResource;

    private final EventLoggingService eventLoggingService;

    private final AuditWrapper<QueryApiException> auditWrapper;

    @Inject
    public AuditedDocRefResourceImpl(final DocRefResource docRefResource,
                                     final EventLoggingService eventLoggingService) {
        this.docRefResource = docRefResource;
        this.eventLoggingService = eventLoggingService;
        this.auditWrapper = new AuditWrapper<>(eventLoggingService, QueryApiException.class, QueryApiException::new);
    }

    @Override
    public Response getAll() throws QueryApiException {
        return auditWrapper.auditFunction(docRefResource::getAll,
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REFS");
                    eventDetail.setDescription("Get the list of doc refs hosted by this service");
                });
    }

    @Override
    public Response get(final String uuid) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.get(uuid),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DOC_REF");
                    eventDetail.setDescription("Get a single doc ref");
                });
    }

    @Override
    public Response createDocument(final String uuid,
                                   final String name) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.createDocument(uuid, name),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("CREATE_DOC_REF");
                    eventDetail.setDescription("Create a Doc Ref");
                });
    }

    @Override
    public Response copyDocument(final String originalUuid,
                                 final String copyUuid) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.copyDocument(originalUuid, copyUuid),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("COPY_DOC_REF");
                    eventDetail.setDescription("Copy a Doc Ref");
                });
    }

    @Override
    public Response documentMoved(final String uuid) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.documentMoved(uuid),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("MOVE_DOC_REF");
                    eventDetail.setDescription("Move a Doc Ref");
                });
    }

    @Override
    public Response documentRenamed(final String uuid,
                                    final String name) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.documentRenamed(uuid, name),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("RENAME_DOC_REF");
                    eventDetail.setDescription("Rename a Doc Ref");
                });
    }

    @Override
    public Response deleteDocument(final String uuid) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.deleteDocument(uuid),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("DELETE_DOC_REF");
                    eventDetail.setDescription("Delete a Doc Ref");
                });
    }

    @Override
    public Response importDocument(final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap) throws QueryApiException {
        return auditWrapper.auditFunction(() -> docRefResource.importDocument(uuid, name, confirmed, dataMap),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("IMPORT_DOC_REF");
                    eventDetail.setDescription("Import a Doc Ref");
                });
    }

    @Override
    public Response exportDocument(final String uuid) throws QueryApiException {

        return auditWrapper.auditFunction(() -> docRefResource.exportDocument(uuid),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("EXPORT_DOC_REF");
                    eventDetail.setDescription("Export a single doc ref");
                });
    }
}
