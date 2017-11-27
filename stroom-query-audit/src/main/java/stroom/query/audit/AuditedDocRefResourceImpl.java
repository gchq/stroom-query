package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class AuditedDocRefResourceImpl implements DocRefResource {
    private final DocRefResource docRefResource;

    private final EventLoggingService eventLoggingService;

    @Inject
    public AuditedDocRefResourceImpl(final DocRefResource docRefResource,
                                    final EventLoggingService eventLoggingService) {
        this.docRefResource = docRefResource;
        this.eventLoggingService = eventLoggingService;
    }

    @Override
    public Response getAll() throws DocRefException {
        Response response;
        Exception exception = null;

        try {
            response = docRefResource.getAll();

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("GET_DOC_REFS");
            eventDetail.setDescription("Get the list of doc refs hosted by this service");

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response get(final String uuid) throws DocRefException {
        Response response;
        Exception exception = null;

        try {
            response = docRefResource.get(uuid);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("GET_DOC_REF");
            eventDetail.setDescription("Get a single doc ref");

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response createDocument(final String uuid,
                                   final String name) throws DocRefException {
        Response response;
        Exception exception = null;

        try {
            response = docRefResource.createDocument(uuid, name);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("CREATE_DOC_REF");
            eventDetail.setDescription("Create a Doc Ref");

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response copyDocument(final String originalUuid,
                                 final String copyUuid) throws DocRefException {
        Response response;
        Exception exception = null;

        try {
            response = docRefResource.copyDocument(originalUuid, copyUuid);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("COPY_DOC_REF");
            eventDetail.setDescription("Copy a Doc Ref");

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response documentMoved(final String uuid) throws DocRefException {

        Response response;
        Exception exception = null;

        try {
            response = docRefResource.documentMoved(uuid);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("MOVE_DOC_REF");
            eventDetail.setDescription("Move a Doc Ref");

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response documentRenamed(final String uuid,
                                    final String name) throws DocRefException {
        Response response;
        Exception exception = null;

        try {
            response = docRefResource.documentRenamed(uuid, name);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("RENAME_DOC_REF");
            eventDetail.setDescription("Rename a Doc Ref");

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response deleteDocument(final String uuid) throws DocRefException {
        Response response;
        Exception exception = null;

        try {
            response = docRefResource.deleteDocument(uuid);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("DELETE_DOC_REF");
            eventDetail.setDescription("Delete a Doc Ref");

            eventLoggingService.log(event);
        }

    }
}
