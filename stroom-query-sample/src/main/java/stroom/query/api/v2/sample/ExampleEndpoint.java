package stroom.query.api.v2.sample;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

/**
 * This class serves as a basic example for a standard set of endpoints for a query API.
 */
@Api(value = "Example Query Endpoint", description = "En example endpoint for testing swagger annotations on the stroom-query-api model classes")
@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
public class ExampleEndpoint {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dataSource")
    @ApiOperation(
            value = "Submit a request for a data source definition, supplying the DocRef for the data source",
            response = DataSource.class)
    public Response getDataSource(@ApiParam("DocRef") final DocRef docRef) {

        return Response
                .accepted(new DataSource(Collections.emptyList()))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    @ApiOperation(value = "SearchResponse", response = SearchResponse.class)
    public Response search(@ApiParam("SearchRequest") final SearchRequest request) {

        return Response
                .accepted(new SearchResponse(null, null, null, null))
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/destroy")
    @ApiOperation(
            value = "Destroy a running query",
            response = Boolean.class)
    public Response destroy(@ApiParam("QueryKey") final QueryKey queryKey) {

        return Response
                .accepted(Boolean.TRUE)
                .build();
    }

}
