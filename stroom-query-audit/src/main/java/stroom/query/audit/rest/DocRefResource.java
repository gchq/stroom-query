package stroom.query.audit.rest;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import stroom.query.audit.security.ServiceUser;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * This is the interface that Stroom uses for externally managed DocRefs.
 */
@Path("/docRefApi/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface DocRefResource {

    /**
     * Retrieve the full config for the given DocRef
     * @param user Authenticated user passed in from web framework
     * @return                  The full list of doc refs
     */
    @GET
    @Path("/")
    @Timed
    Response getAll(@Auth @NotNull ServiceUser user);

    /**
     * Retrieve the full config for the given DocRef
     * @param user Authenticated user passed in from web framework
     * @param uuid              The UUID of the docRef to return
     * @return                  The full implementation specific config for this docRef.
     */
    @GET
    @Path("/{uuid}")
    @Timed
    Response get(@Auth @NotNull ServiceUser user,
                 @PathParam("uuid") String uuid);

    /**
     * Retrieve the full config for the given DocRef
     * @param user Authenticated user passed in from web framework
     * @param uuid              The UUID of the docRef to return
     * @return                  The DocRefInfo for the given DocRef
     */
    @GET
    @Path("/{uuid}/info")
    @Timed
    Response getInfo(@Auth @NotNull ServiceUser user,
                     @PathParam("uuid") String uuid);

    /**
     * A new document has been created in Stroom
     *
     * @param user Authenticated user passed in from web framework
     * @param uuid              The UUID of the document as created by stroom
     * @param name              The name of the document to be created.
     * @return A doc ref for the newly created document.
     */
    @POST
    @Path("/create/{uuid}/{name}")
    Response createDocument(@Auth @NotNull ServiceUser user,
                            @PathParam("uuid") String uuid,
                            @PathParam("name") String name);

    /**
     * Update the document
     * @param user Authenticated user passed in from web framework
     * @param uuid              The UUID of the document as created by stroom
     * @param updatedConfigJson The updated configuration
     * @return The updated config
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/update/{uuid}")
    Response update(@Auth ServiceUser user,
                    @PathParam("uuid") String uuid,
                    String updatedConfigJson);

    /**
     * A notification from Stroom that a document is being copied. The external system should
     * copy it's configuration for the original into a new entity.
     *
     * @param user Authenticated user passed in from web framework
     * @param originalUuid      The uuid of the document being copied
     * @param copyUuid          The uuid of the copy
     * @return A doc ref for the new document copy.
     */
    @POST
    @Path("/copy/{originalUuid}/{copyUuid}")
    Response copyDocument(@Auth @NotNull ServiceUser user,
                          @PathParam("originalUuid") String originalUuid,
                          @PathParam("copyUuid") String copyUuid);

    /**
     * A Notification from Stroom that the document has been 'moved'. In most cases the external system
     * will not care about this.
     *
     * @param user Authenticated user passed in from web framework
     * @param uuid             The uuid of the document that was moved
     * @return A doc ref for the moved document.
     */
    @PUT
    @Path("/move/{uuid}")
    Response moveDocument(@Auth @NotNull ServiceUser user,
                          @PathParam("uuid") String uuid);

    /**
     * A notifiation from Stroom that the name of a document has been changed. Whilst the name belongs to stroom
     * it may be helpful for the external system to know what the name is, but the name should not be used for referencing
     * the DocRef between systems as it could easily be out of sync.
     *
     * @param user Authenticated user passed in from web framework
     * @param uuid The uuid of the document you want to rename.
     * @param name The new name of the document.
     * @return A doc ref for the renamed document.
     */
    @PUT
    @Path("/rename/{uuid}/{name}")
    Response renameDocument(@Auth @NotNull ServiceUser user,
                            @PathParam("uuid") String uuid,
                            @PathParam("name") String name);

    /**
     * The document with this UUID is being deleted in Stroom.
     *
     * @param user Authenticated user passed in from web framework
     * @param uuid The uuid of the document you want to delete.
     * @return No content if OK
     */
    @DELETE
    @Path("/delete/{uuid}")
    Response deleteDocument(@Auth @NotNull ServiceUser user,
                            @PathParam("uuid") String uuid);

    /**
     * Import the data as a new document.
     * @param user Authenticated user passed in from web framework
     * @param uuid The UUID of the document to import
     * @param name The name of the document to import
     * @param confirmed Is the import a confirmed one? If it isn't then this is just a dry run.
     * @param dataMap The data to import
     * @return The created document
     */
    @POST
    @Path("/import/{uuid}/{name}/{confirmed}")
    Response importDocument(@Auth @NotNull ServiceUser user,
                            @PathParam("uuid") String uuid,
                            @PathParam("name") String name,
                            @PathParam("confirmed") Boolean confirmed,
                            Map<String, String> dataMap);

    /**
     * Export the given document
     * @param user Authenticated user passed in from web framework
     * @param uuid The UUID of the document to export
     * @return A Map of Strings by Strings containing the exported data.
     */
    @GET
    @Path("/export/{uuid}")
    Response exportDocument(@Auth @NotNull ServiceUser user,
                            @PathParam("uuid") String uuid);
}
