package stroom.query.audit;

import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the interface that Stroom Dashboards expect to use when talking to external data sources.
 */
@Path("/queryApi/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface QueryResource {
    int MIN_ID_LENGTH = 3;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dataSource")
    Response getDataSource(DocRef docRef);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search/{dataSourceUuid}")
    Response search(@Validated
                    @PathParam("dataSourceUuid")
                    @NotNull
                    @Length(min=MIN_ID_LENGTH) String dataSourceUuid, SearchRequest request);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/destroy")
    Response destroy(QueryKey queryKey);
}
