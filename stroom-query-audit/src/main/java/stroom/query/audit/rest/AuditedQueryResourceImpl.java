package stroom.query.audit.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.logging.EventLoggingService;
import event.logging.Outcome;
import event.logging.Query;
import event.logging.Search;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.AuditWrapper;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * A standard implementation of {@link QueryResource} which logs all activity to the {@link EventLoggingService eventLoggingService}
 * It passes all calls onto an inner implementation of QueryResource, which will be supplied by the specific application.
 */
public class AuditedQueryResourceImpl<T extends DocRefEntity> implements QueryResource {

    private final Logger LOGGER = LoggerFactory.getLogger(AuditedQueryResourceImpl.class);

    private final EventLoggingService eventLoggingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AuditWrapper auditWrapper;

    private final QueryService service;

    private final AuthorisationService authorisationService;

    private final DocRefService<T> docRefService;

    @Inject
    public AuditedQueryResourceImpl(final EventLoggingService eventLoggingService,
                                    final QueryService service,
                                    final AuthorisationService authorisationService,
                                    final DocRefService<T> docRefService) {
        this.eventLoggingService = eventLoggingService;
        this.service = service;
        this.authorisationService = authorisationService;
        this.auditWrapper = new AuditWrapper(eventLoggingService);
        this.docRefService = docRefService;
    }

    @Override
    public Response getDataSource(final ServiceUser user,
                                  final DocRef docRef){
        return auditWrapper.auditFunction(user,
                docRefService,
                docRef,
                () -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.READ),
                () -> service.getDataSource(user, docRef)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
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
    public Response search(final ServiceUser user,
                           final SearchRequest request){
        return auditWrapper.auditFunction(user,
                docRefService,
                request.getQuery().getDataSource(),
                () -> authorisationService.isAuthorised(user,
                        request.getQuery().getDataSource(),
                        DocumentPermission.READ),
                () -> service.search(user, request)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()),
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
    public Response destroy(final ServiceUser user,
                            final QueryKey queryKey) {
        // Attempt to retrieve the doc ref for the given query key
        // This is required in advance so permissions can be checked
        final DocRef queryDocRef;

        try {
            final Optional<DocRef> queryDocRefOpt = service.getDocRefForQueryKey(user, queryKey);

            if (queryDocRefOpt.isPresent()) {
                queryDocRef = queryDocRefOpt.get();
            } else {
                return Response.status(HttpStatus.NOT_FOUND_404).build();
            }
        } catch (final Exception e) {
            LOGGER.error("Could not get doc ref for query to destroy");
            return Response.serverError().build();
        }

        return auditWrapper.auditFunction(user,
                docRefService,
                queryDocRef,
                () -> authorisationService.isAuthorised(user,
                        queryDocRef,
                        DocumentPermission.READ),
                () -> {
                    final Boolean result = service.destroy(user, queryKey);
                    return Response
                            .ok(result)
                            .build();
                },
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
