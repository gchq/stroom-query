package stroom.query.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.logging.Event;
import event.logging.EventLoggingService;
import event.logging.Outcome;
import event.logging.Query;
import event.logging.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.util.shared.QueryApiException;

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

    private final AuditWrapper<QueryApiException> auditWrapper;

    @Inject
    public AuditedQueryResourceImpl(final QueryResource queryResource,
                                    final EventLoggingService eventLoggingService) {
        this.queryResource = queryResource;
        this.eventLoggingService = eventLoggingService;
        this.auditWrapper = new AuditWrapper<>(eventLoggingService, QueryApiException.class, QueryApiException::new);
    }

    @Override
    public Response getDataSource(final DocRef docRef) throws QueryApiException {
        return auditWrapper.auditFunction(() -> queryResource.getDataSource(docRef),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DATA_SOURCE");
                    eventDetail.setDescription("Get Datasource For Document");

                    final Search search = new Search();
                    eventDetail.setSearch(search);

                    final Outcome outcome = new Outcome();
                    outcome.setSuccess(null != exception);
                    search.setOutcome(outcome);
                });
    }

    @Override
    public Response search(final SearchRequest request) throws QueryApiException {
        return auditWrapper.auditFunction(() -> queryResource.search(request),
                (eventDetail, response, exception) -> {
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
                });
    }

    @Override
    public Response destroy(final QueryKey queryKey) throws QueryApiException {
        return auditWrapper.auditFunction(() -> queryResource.destroy(queryKey),
                (eventDetail, response, exception) -> {
                    eventDetail.setTypeId("QUERY_DESTROY");
                    eventDetail.setDescription("Destroy a running query");

                    final Search search = new Search();
                    eventDetail.setSearch(search);

                    final Outcome outcome = new Outcome();
                    outcome.setSuccess(null != exception);
                    search.setOutcome(outcome);
                });
    }
}
