package stroom.query.audit.rest;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * This is the interface that Stroom uses for externally managed DocRefs.
 * @param <T> The DTO class that encapsulates the fully detailed version of the DocRefResource
 */
@Path("/docRefApi/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface DocRefResource<T extends DocRefEntity> {

    /**
     * Retrieve the full config for the given DocRef
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @return                  The full list of doc refs
     */
    @GET
    @Path("/")
    @Timed
    Response getAll(@Auth @NotNull ServiceUser authenticatedServiceUser);

    /**
     * Retrieve the full config for the given DocRef
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid              The UUID of the docRef to return
     * @return                  The full implementation specific config for this docRef.
     */
    @GET
    @Path("/{uuid}")
    @Timed
    Response get(@Auth @NotNull ServiceUser authenticatedServiceUser,
                 @PathParam("uuid") String uuid);

    /**
     * Retrieve the full config for the given DocRef
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid              The UUID of the docRef to return
     * @return                  The DocRefInfo for the given DocRef
     */
    @GET
    @Path("/{uuid}/info")
    @Timed
    Response getInfo(@Auth @NotNull ServiceUser authenticatedServiceUser,
                     @PathParam("uuid") String uuid);

    /**
     * A new document has been created in Stroom
     *
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid              The UUID of the document as created by stroom
     * @param name              The name of the document to be created.
     * @param parentFolderUUID  The destination parent folder
     * @return A doc ref for the newly created document.
     */
    @POST
    @Path("/create/{uuid}/{name}/{parentFolderUUID}")
    Response createDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                            @PathParam("uuid") String uuid,
                            @PathParam("name") String name,
                            @PathParam("parentFolderUUID") final String parentFolderUUID);

    /**
     * Update the document
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid              The UUID of the document as created by stroom
     * @param updatedConfig The updated configuration
     * @return The updated config
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/update/{uuid}")
    Response update(@Auth ServiceUser authenticatedServiceUser,
                    @PathParam("uuid") String uuid,
                    T updatedConfig);

    /**
     * A notification from Stroom that a document is being copied. The external system should
     * copy it's configuration for the original into a new entity.
     *
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param originalUuid      The uuid of the document being copied
     * @param copyUuid          The uuid of the copy
     * @param parentFolderUUID  The destination parent folder
     * @return A doc ref for the new document copy.
     */
    @POST
    @Path("/copy/{originalUuid}/{copyUuid}/{parentFolderUUID}")
    Response copyDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                          @PathParam("originalUuid") String originalUuid,
                          @PathParam("copyUuid") String copyUuid,
                          @PathParam("parentFolderUUID") final String parentFolderUUID);

    /**
     * A Notification from Stroom that the document has been 'moved'. In most cases the external system
     * will not care about this.
     *
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid             The uuid of the document that was moved
     * @param parentFolderUUID The destination parent folder
     * @return A doc ref for the moved document.
     */
    @PUT
    @Path("/move/{uuid}/{parentFolderUUID}")
    Response moveDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                          @PathParam("uuid") String uuid,
                          @PathParam("parentFolderUUID") final String parentFolderUUID);

    /**
     * A notifiation from Stroom that the name of a document has been changed. Whilst the name belongs to stroom
     * it may be helpful for the external system to know what the name is, but the name should not be used for referencing
     * the DocRef between systems as it could easily be out of sync.
     *
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid The uuid of the document you want to rename.
     * @param name The new name of the document.
     * @return A doc ref for the renamed document.
     */
    @PUT
    @Path("/rename/{uuid}/{name}")
    Response renameDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                            @PathParam("uuid") String uuid,
                            @PathParam("name") String name);

    /**
     * The document with this UUID is being deleted in Stroom.
     *
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid The uuid of the document you want to delete.
     * @return No content if OK
     */
    @DELETE
    @Path("/delete/{uuid}")
    Response deleteDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                            @PathParam("uuid") String uuid);

    /**
     * Import the data as a new document.
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid The UUID of the document to import
     * @param name The name of the document to import
     * @param confirmed Is the import a confirmed one? If it isn't then this is just a dry run.
     * @param dataMap The data to import
     * @return The created document
     */
    @POST
    @Path("/import/{uuid}/{name}/{confirmed}")
    Response importDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                            @PathParam("uuid") String uuid,
                            @PathParam("name") String name,
                            @PathParam("confirmed") Boolean confirmed,
                            Map<String, String> dataMap);

    /**
     * Export the given document
     * @param authenticatedServiceUser Authenticated user passed in from web framework
     * @param uuid The UUID of the document to export
     * @return A Map of Strings by Strings containing the exported data.
     */
    @GET
    @Path("/export/{uuid}")
    Response exportDocument(@Auth @NotNull ServiceUser authenticatedServiceUser,
                            @PathParam("uuid") String uuid);
}