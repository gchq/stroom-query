package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.service.QueryApiException;
import stroom.query.security.ServiceUser;

import javax.ws.rs.core.Response;

/**
 * Common form of functions that want to do the following:
 * 1) Check authorisation for data
 * 2) Compose some response
 * 3) Audit the event, including any exceptions
 */
public class SimpleAuditWrapper extends BaseAuditWrapper<SimpleAuditWrapper> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAuditWrapper.class);

    @FunctionalInterface
    public interface AuthorisationSupplier {
        Boolean isAuthorised() throws QueryApiException;
    }

    @FunctionalInterface
    public interface ResponseSupplier {
        Response getResponse() throws QueryApiException;
    }

    private AuthorisationSupplier authorisationSupplier;

    private ResponseSupplier responseSupplier;

    public static SimpleAuditWrapper withUser(final ServiceUser user) {
        return new SimpleAuditWrapper(user);
    }

    private SimpleAuditWrapper(final ServiceUser user) {
        super(user);
    }

    public SimpleAuditWrapper withDefaultAuthSupplier() {
        return withAuthSupplier(() -> Boolean.TRUE);
    }

    public SimpleAuditWrapper withAuthSupplier(final AuthorisationSupplier authorisationSupplier) {
        this.authorisationSupplier = authorisationSupplier;
        return self();
    }

    public SimpleAuditWrapper withResponse(final ResponseSupplier responseSupplier) {
        this.responseSupplier = responseSupplier;
        return self();
    }

    @Override
    protected void checkArgs() {
        if (this.authorisationSupplier == null) {
            throw new IllegalArgumentException("Basic Authorisation Supplier must not be null");
        }
        if (this.responseSupplier == null){
            throw new IllegalArgumentException("Response Supplier must not be null");
        }
    }

    @Override
    protected Response audit(EventLoggingService eventLoggingService) {
        Response response = null;
        Exception exception = null;

        try {
            final Boolean isAuthorised = authorisationSupplier.isAuthorised();

            if (isAuthorised) {
                response = responseSupplier.getResponse();
            } else {
                response = Response.status(HttpStatus.FORBIDDEN_403).build();
            }

        } catch (final RuntimeException | QueryApiException e) {
            LOGGER.error("Failed to execute operation: " + e.getLocalizedMessage(), e);
            exception = e;
            response = Response.serverError().build();
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            event.getEventSource().getUser().setId(user().getName());

            populateEventDetail().populate(eventDetail, response, exception);

            eventLoggingService.log(event);
        }

        return response;
    }

    @Override
    protected SimpleAuditWrapper self() {
        return this;
    }
}
