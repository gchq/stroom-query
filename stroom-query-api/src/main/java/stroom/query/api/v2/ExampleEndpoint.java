package stroom.query.api.v2;

import io.swagger.annotations.Api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api
@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
public class ExampleEndpoint {


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    public SearchResponse search(final SearchRequest request) {
        return new SearchResponse(null, null, null, null);
    }

}
