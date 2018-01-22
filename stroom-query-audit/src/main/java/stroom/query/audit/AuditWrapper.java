package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class AuditWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditWrapper.class);

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

    /**
     * This form of Audit will check that the given doc ref can be found.
     * This should be used for functions that rely on the existence of a doc ref, but they do
     * not directly manipulate the doc ref itself.
     *
     * @param user The authenticated user
     * @param docRefService The doc ref service to use to try a 'GET'
     * @param docRef The doc ref that this function relies on
     * @param checkAuthorisation A function to check the authorisation
     * @param getResponse A function to generate the actual response, likely a lambda that delegates to a service layer
     * @param populateEventDetail A function that takes the request, response and exceptions and populates the audit log event
     * @return The response to be returned over REST interface
     */
    public Response auditFunction(final ServiceUser user,
                                  final DocRefService<?> docRefService,
                                  final DocRef docRef,
                                  final Supplier<Boolean> checkAuthorisation,
                                  final GetResponse getResponse,
                                  final PopulateEventDetail populateEventDetail) {
        return auditFunction(user,
                checkAuthorisation,
                () -> {
                    final Optional<?> docRefEntityOpt =
                            docRefService.get(user, docRef.getUuid());
                    if (docRefEntityOpt.isPresent()) {
                        return getResponse.getResponse();
                    } else {
                        return Response.status(HttpStatus.NOT_FOUND_404).build();
                    }
                },
                populateEventDetail);
    }

    /**
     * Common form of functions that want to do the following:
     * 1) Check authorisation for data
     *
     *
     * @param user The authenticated user
     * @param checkAuthorisation A function to check the authorisation
     * @param getResponse A function to generate the actual response, likely a lambda that delegates to a service layer
     * @param populateEventDetail A function that takes the request, response and exceptions and populates the audit log event
     * @return The response to be returned over REST interface
     */
    public Response auditFunction(final ServiceUser user,
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
                response = Response.status(HttpStatus.FORBIDDEN_403).build();
            }

        } catch (Exception e) {
            LOGGER.error("Failed to execute operation: " + e.getLocalizedMessage(), e);
            exception = e;
            response = Response.serverError().build();
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            if (null != user) {
                event.getEventSource().getUser().setId(user.getName());
            }

            populateEventDetail.populate(eventDetail, response, exception);

            eventLoggingService.log(event);
        }

        return response;
    }
}
