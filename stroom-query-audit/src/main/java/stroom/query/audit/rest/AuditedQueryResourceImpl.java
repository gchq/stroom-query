package stroom.query.audit.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.logging.EventLoggingService;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import event.logging.Query;
import event.logging.Search;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.DocRefAuditWrapper;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * A standard implementation of {@link QueryResource} which logs all activity to the {@link EventLoggingService eventLoggingService}
 * It passes all calls onto an inner implementation of QueryResource, which will be supplied by the specific application.
 */
public class AuditedQueryResourceImpl<T extends DocRefEntity> implements QueryResource {

    private final Logger LOGGER = LoggerFactory.getLogger(AuditedQueryResourceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EventLoggingService eventLoggingService;

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
        this.docRefService = docRefService;
    }

    @Override
    public Response getDataSource(final ServiceUser user,
                                  final DocRef docRef){
        return DocRefAuditWrapper.<T>withUser(user)
                .withDocRef(docRef)
                .withDocRefEntity(d -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(d -> authorisationService.isAuthorised(user,
                        d,
                        DocumentPermission.READ))
                .withResponse(docRefEntity -> service.getDataSource(user, docRef)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId("GET_DATA_SOURCE");
                    eventDetail.setDescription("Get Datasource For Document");

                    final Search search = new Search();
                    search.setId(docRef.getUuid());
                    search.setType(docRef.getType());
                    search.setName(docRef.getName());
                    eventDetail.setSearch(search);

                    final Outcome outcome = new Outcome();
                    outcome.setSuccess(null != exception);
                    search.setOutcome(outcome);
                }).callAndAudit(eventLoggingService);
    }

    @Override
    public Response search(final ServiceUser user,
                           final SearchRequest request){
        return DocRefAuditWrapper.<T>withUser(user)
                .withDocRef(request.getQuery().getDataSource())
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(docRef -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.READ))
                .withResponse(docRefEntity -> service.search(user, request)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404)
                                .build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId("QUERY_SEARCH");
                    eventDetail.setDescription("Run a Query over the data");

                    final Search search = new Search();
                    if (null != request.getQuery() && null != request.getQuery().getDataSource()) {
                        search.setId(request.getQuery().getDataSource().getUuid());
                        search.setType(request.getQuery().getDataSource().getType());
                        search.setName(request.getQuery().getDataSource().getName());
                    }
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
                }).callAndAudit(eventLoggingService);
    }

    @Override
    public Response destroy(final ServiceUser user,
                            final QueryKey queryKey) {
        return DocRefAuditWrapper.<T>withUser(user)
                .withDocRefSupplier(() -> service.getDocRefForQueryKey(user, queryKey))
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(docRef -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.READ))
                .withResponse(docRefEntity -> {
                    final Boolean result = service.destroy(user, queryKey);
                    return Response
                            .ok(result)
                            .build();
                })
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId("QUERY_DESTROY");
                    eventDetail.setDescription("Destroy a running query");

                    final ObjectOutcome deleteObj = new ObjectOutcome();
                    final Outcome delete = new Outcome();
                    deleteObj.setOutcome(delete);
                    delete.setDescription(String.format("Destroy query %s", queryKey.getUuid()));
                    eventDetail.setDelete(deleteObj);
                }).callAndAudit(eventLoggingService);
    }
}
