package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.core.Response;
import java.util.function.Function;
import java.util.function.Supplier;

public class AuditWrapper<E extends Throwable> {

    private final Class<E> throwableClass;
    private final EventLoggingService eventLoggingService;
    private final Function<Throwable, E> createException;

    public AuditWrapper(final EventLoggingService eventLoggingService,
                        final Class<E> throwableClass,
                        final Function<Throwable, E> createException) {
        this.eventLoggingService = eventLoggingService;
        this.throwableClass = throwableClass;
        this.createException = createException;
    }

    @FunctionalInterface
    public interface GetResponse<E extends Throwable> {
        Response getResponse() throws E;
    }

    @FunctionalInterface
    public interface PopulateEventDetail<E extends Throwable> {
        void populate(final Event.EventDetail eventDetail,
                      final Response response,
                      final E exception);
    }

    public Response auditFunction(final ServiceUser serviceUser,
                                  final Supplier<Boolean> checkAuthorisation,
                                  final GetResponse<E> getResponse,
                                  final PopulateEventDetail<E> populateEventDetail) throws E {
        Response response = null;
        E exception = null;

        try {
            final Boolean isAuthorised = checkAuthorisation.get();

            if (isAuthorised) {
                response = getResponse.getResponse();
            } else {
                response = Response.status(HttpStatus.UNAUTHORIZED_401).build();
            }

            return response;
        } catch(final Throwable e) {
            if (e.getClass().isAssignableFrom(this.throwableClass)) {
                exception = (E) e;
            } else {
                exception = createException.apply(e);
            }
            throw exception;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            if (null != serviceUser) {
                event.getEventSource().getUser().setId(serviceUser.getName());
            }

            populateEventDetail.populate(eventDetail, response, exception);

            eventLoggingService.log(event);
        }
    }
}
