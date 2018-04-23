package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import stroom.security.ServiceUser;

import javax.ws.rs.core.Response;

/**
 * Instances of this are 'built' to manage audited function calls in a standard way.
 * Some function calls require Doc Refs, and therefore authorisation for actions on those Doc Refs,
 * others simply require to be called and audited with no doc ref checking
 */
public abstract class BaseAuditWrapper<CHILD_CLASS extends BaseAuditWrapper<?>> {

    private final ServiceUser user;

    private PopulateEventDetail populateEventDetail;

    @FunctionalInterface
    public interface PopulateEventDetail {
        void populate(final Event.EventDetail eventDetail,
                      final Response response,
                      final Exception exception);
    }

    public BaseAuditWrapper(final ServiceUser user) {
        this.user = user;
    }

    public CHILD_CLASS withPopulateAudit(final PopulateEventDetail populateEventDetail) {
        this.populateEventDetail = populateEventDetail;
        return self();
    }

    public Response callAndAudit(final EventLoggingService eventLoggingService) {
        if (this.user == null){
            throw new IllegalArgumentException("User must not be null");
        }
        if (this.populateEventDetail == null){
            throw new IllegalArgumentException("Event Detail Populator must not be null");
        }
        if (eventLoggingService == null){
            throw new IllegalArgumentException("Event Logging Service must not be null");
        }

        checkArgs();

        return audit(eventLoggingService);
    }

    protected abstract void checkArgs();

    protected abstract Response audit(EventLoggingService eventLoggingService);

    protected abstract CHILD_CLASS self();

    protected ServiceUser user() {
        return user;
    }

    protected PopulateEventDetail populateEventDetail() {
        return populateEventDetail;
    }
}
