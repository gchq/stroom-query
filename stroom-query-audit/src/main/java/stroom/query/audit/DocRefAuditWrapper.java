package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.docref.DocRef;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.service.QueryApiException;
import stroom.query.security.ServiceUser;

import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * This form of Audit will check that the given doc ref can be found.
 * This should be used for functions that rely on the existence of a doc ref, but they do
 * not directly manipulate the doc ref itself.
 */
public class DocRefAuditWrapper<DOC_REF_ENTITY extends DocRefEntity>
        extends BaseAuditWrapper<DocRefAuditWrapper<DOC_REF_ENTITY>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefAuditWrapper.class);

    @FunctionalInterface
    public interface DocRefSupplier {
        Optional<DocRef> getDocRef() throws QueryApiException;
    }

    @FunctionalInterface
    public interface DocRefEntitySupplier<T extends DocRefEntity> {
        Optional<T> getDocRefEntity(DocRef docRef) throws QueryApiException;
    }

    @FunctionalInterface
    public interface DocRefAuthorisationSupplier {
        Boolean isAuthorised(DocRef docRef) throws QueryApiException;
    }

    @FunctionalInterface
    public interface ResponseSupplier<T extends DocRefEntity> {
        Response getResponse(T docRefEntity) throws QueryApiException;
    }

    private DocRefSupplier docRefSupplier;
    private DocRefAuthorisationSupplier docRefAuthorisationSupplier;
    private DocRefEntitySupplier<DOC_REF_ENTITY> docRefEntitySupplier;

    private ResponseSupplier<DOC_REF_ENTITY> responseSupplier;

    public static <D extends DocRefEntity> DocRefAuditWrapper<D> withUser(final ServiceUser user) {
        return new DocRefAuditWrapper<>(user);
    }

    private DocRefAuditWrapper(final ServiceUser user) {
        super(user);
    }

    @Override
    protected DocRefAuditWrapper<DOC_REF_ENTITY> self() {
        return this;
    }

    public DocRefAuditWrapper<DOC_REF_ENTITY> withDocRefSupplier(final DocRefSupplier docRefSupplier) {
        this.docRefSupplier = docRefSupplier;
        return this;
    }

    public DocRefAuditWrapper<DOC_REF_ENTITY> withDocRef(final DocRef docRef) {
        this.docRefSupplier = () -> Optional.of(docRef);
        return self();
    }

    public DocRefAuditWrapper<DOC_REF_ENTITY> withDocRefEntity(final DocRefEntitySupplier<DOC_REF_ENTITY> docRefEntitySupplier) {
        this.docRefEntitySupplier = docRefEntitySupplier;
        return self();
    }

    public DocRefAuditWrapper<DOC_REF_ENTITY> withAuthSupplier(final DocRefAuthorisationSupplier docRefAuthorisationSupplier) {
        this.docRefAuthorisationSupplier = docRefAuthorisationSupplier;
        return self();
    }

    public DocRefAuditWrapper<DOC_REF_ENTITY> withResponse(final ResponseSupplier<DOC_REF_ENTITY> responseSupplier) {
        this.responseSupplier = responseSupplier;
        return self();
    }

    @Override
    protected void checkArgs() {
        if (this.docRefSupplier == null){
            throw new IllegalArgumentException("Doc Ref Supplier must not be null");
        }
        if (this.docRefAuthorisationSupplier == null){
            throw new IllegalArgumentException("Doc Ref Authorisation Suplier must not be null");
        }
        if (this.docRefEntitySupplier == null){
            throw new IllegalArgumentException("Doc Ref Entity Supplier must not be null");
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
            final DocRef docRef;
            final Optional<DocRef> docRefOpt = docRefSupplier.getDocRef();

            if (docRefOpt.isPresent()) {
                docRef = docRefOpt.get();

                final Boolean isAuthorised = docRefAuthorisationSupplier.isAuthorised(docRef);

                if (isAuthorised) {
                    final Optional<DOC_REF_ENTITY> docRefEntityOpt =
                            docRefEntitySupplier.getDocRefEntity(docRef);

                    if (docRefEntityOpt.isPresent()) {
                        try {
                            return responseSupplier.getResponse(docRefEntityOpt.get());
                        } catch (Exception e) {
                            LOGGER.error(e.getLocalizedMessage(), e);
                            return Response.serverError().entity(e.getLocalizedMessage()).build();
                        }
                    } else {
                        return Response.status(HttpStatus.NOT_FOUND_404).build();
                    }
                } else {
                    response = Response.status(HttpStatus.FORBIDDEN_403).build();
                }
            } else {
                response = Response.status(HttpStatus.NOT_FOUND_404).build();
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
}
