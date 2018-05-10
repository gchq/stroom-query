package stroom.query.testing.data;

import io.dropwizard.auth.Auth;
import stroom.query.security.ServiceUser;

import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/createTestData")
public interface CreateTestDataResource {
    @POST
    @Path("/{docRefUuid}/{seed}")
    Response createTestData(@Auth @NotNull ServiceUser user,
                            @PathParam("docRefUuid") String docRefUuid,
                            @PathParam("seed") String seed);
}
