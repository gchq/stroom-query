package stroom.query.api.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "/v2", description = "my text")
@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
public class ExampleEndpoint {


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    //swagger plugin seems to require APIOperation
    @ApiOperation(value = "xxxx", response = SearchResponse.class)
    public SearchResponse search(final SearchRequest request) {
        return new SearchResponse(null, null, null, null);
    }

}
