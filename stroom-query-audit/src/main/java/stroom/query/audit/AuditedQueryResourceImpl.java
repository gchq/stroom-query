package stroom.query.audit;

import event.logging.Event;
import event.logging.EventLoggingService;
import event.logging.Outcome;
import event.logging.Search;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * A standard implementation of {@link QueryResource} which logs all activity to the {@link EventLoggingService eventLoggingService}
 * It passes all calls onto an inner implementation of QueryResource, which will be supplied by the specific application.
 */
public class AuditedQueryResourceImpl implements QueryResource {

    private final QueryResource queryResource;

    private final EventLoggingService eventLoggingService;

    @Inject
    public AuditedQueryResourceImpl(final QueryResource queryResource,
                                    final EventLoggingService eventLoggingService) {
        this.queryResource = queryResource;
        this.eventLoggingService = eventLoggingService;
    }

    @Override
    public Response getDataSource(final DocRef docRef) {
        Response response;
        Exception exception = null;

        try {
            response = queryResource.getDataSource(docRef);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("GET_DATA_SOURCE");
            eventDetail.setDescription("Get Datasource For Document");

            final Search search = new Search();
            eventDetail.setSearch(search);

            final Outcome outcome = new Outcome();
            outcome.setSuccess(null != exception);
            search.setOutcome(outcome);

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response search(final SearchRequest request) {
        Response response;
        Exception exception = null;

        try {
            response = queryResource.search(request);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("QUERY_SEARCH");
            eventDetail.setDescription("Run a Query over the data");

            final Search search = new Search();
            eventDetail.setSearch(search);

            final Outcome outcome = new Outcome();
            outcome.setSuccess(null != exception);
            search.setOutcome(outcome);

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response destroy(final QueryKey queryKey) {
        Response response;
        Exception exception = null;

        try {
            response = queryResource.destroy(queryKey);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("QUERY_DESTROY");
            eventDetail.setDescription("Destroy a running query");

            final Search search = new Search();
            eventDetail.setSearch(search);

            final Outcome outcome = new Outcome();
            outcome.setSuccess(null != exception);
            search.setOutcome(outcome);

            eventLoggingService.log(event);
        }
    }
}
