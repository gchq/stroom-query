package stroom.query.audit;

import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the interface that Stroom Dashboards expect to use when talking to external data sources.
 *
 */
@Path("/queryApi/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface QueryResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dataSource")
    Response getDataSource(DocRef docRef) throws QueryApiException;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    Response search(SearchRequest request) throws QueryApiException;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/destroy")
    Response destroy(QueryKey queryKey) throws QueryApiException;
}
