package stroom.query.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.logging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger LOGGER = LoggerFactory.getLogger(AuditedQueryResourceImpl.class);

    private final QueryResource queryResource;

    private final EventLoggingService eventLoggingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public Response search(final String dataSourceUuid, final SearchRequest request) {
        Response response;
        Exception exception = null;

        try {
            response = queryResource.search(dataSourceUuid, request);

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

            final Query query = new Query();
            try {
                final String requestJson = objectMapper.writeValueAsString(request);
                query.setRaw(requestJson);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Could not serialize request details for audit", e);
            }
            search.setQuery(query);

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
