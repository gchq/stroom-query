package stroom.query.testing.data;

import io.dropwizard.auth.Auth;
import stroom.query.audit.security.ServiceUser;

import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/createTestData")
@Produces(MediaType.APPLICATION_JSON)
public interface CreateTestDataResource {
    @POST
    @Path("/{seed}")
    Response createTestData(@Auth @NotNull ServiceUser user,
                            @PathParam("seed") String seed) throws Exception;
}
