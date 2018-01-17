package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.core.Response;
import java.util.function.Function;
import java.util.function.Supplier;

public class AuditWrapper {

    private final EventLoggingService eventLoggingService;

    public AuditWrapper(final EventLoggingService eventLoggingService) {
        this.eventLoggingService = eventLoggingService;
    }

    @FunctionalInterface
    public interface GetResponse {
        Response getResponse() throws Exception;
    }

    @FunctionalInterface
    public interface PopulateEventDetail {
        void populate(final Event.EventDetail eventDetail,
                      final Response response,
                      final Exception exception);
    }

    public Response auditFunction(final ServiceUser serviceUser,
                                  final Supplier<Boolean> checkAuthorisation,
                                  final GetResponse getResponse,
                                  final PopulateEventDetail populateEventDetail) {
        Response response = null;
        Exception exception = null;

        try {
            final Boolean isAuthorised = checkAuthorisation.get();

            if (isAuthorised) {
                response = getResponse.getResponse();
            } else {
                response = Response.status(HttpStatus.UNAUTHORIZED_401).build();
            }

        } catch (Exception e) {
            exception = e;
            response = Response.serverError().build();
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            if (null != serviceUser) {
                event.getEventSource().getUser().setId(serviceUser.getName());
            }

            populateEventDetail.populate(eventDetail, response, exception);

            eventLoggingService.log(event);
        }

        return response;
    }
}
