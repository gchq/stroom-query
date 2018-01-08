package stroom.query.audit;

import com.codahale.metrics.annotation.Timed;
import stroom.query.api.v2.DocRef;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * This is the interface that Stroom uses for externally managed DocRefs.
 *
 */
@Path("/docRefApi/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface DocRefResource {

    @GET
    @Path("/")
    @Timed
    Response getAll() throws QueryApiException;

    /**
     * Retrieve the full config for the given DocRef
     * @param uuid              The UUID of the docRef to return
     * @return                  The full implementation specific config for this docRef.
     * @throws QueryApiException  If something goes wrong
     */
    @GET
    @Path("/{uuid}")
    @Timed
    Response get(@PathParam("uuid") String uuid) throws QueryApiException;

    /**
     * Retrieve the full config for the given DocRef
     * @param uuid              The UUID of the docRef to return
     * @return                  The DocRefInfo for the given DocRef
     * @throws QueryApiException  If something goes wrong
     */
    @GET
    @Path("/{uuid}/info")
    @Timed
    Response getInfo(@PathParam("uuid") String uuid) throws QueryApiException;

    /**
     * A new document has been created in Stroom
     *
     * @param uuid              The UUID of the document as created by stroom
     * @param name              The name of the document to be created.
     * @return A doc ref for the newly created document.
     * @throws QueryApiException  If something goes wrong
     */
    @POST
    @Path("/create/{uuid}/{name}")
    Response createDocument(@PathParam("uuid") String uuid,
                            @PathParam("name") String name) throws QueryApiException;

    /**
     * A notification from Stroom that a document is being copied. The external system should
     * copy it's configuration for the original into a new entity.
     *
     * @param originalUuid      The uuid of the document being copied
     * @param copyUuid          The uuid of the copy
     * @return A doc ref for the new document copy.
     * @throws QueryApiException  If something goes wrong
     */
    @POST
    @Path("/copy/{originalUuid}/{copyUuid}")
    Response copyDocument(@PathParam("originalUuid") String originalUuid,
                          @PathParam("copyUuid") String copyUuid) throws QueryApiException;

    /**
     * A Notification from Stroom that the document has been 'moved'. In most cases the external system
     * will not care about this.
     *
     * @param uuid             The uuid of the document that was moved
     * @return A doc ref for the moved document.
     * @throws QueryApiException  If something goes wrong
     */
    @PUT
    @Path("/move/{uuid}")
    Response documentMoved(@PathParam("uuid") String uuid) throws QueryApiException;

    /**
     * A notifiation from Stroom that the name of a document has been changed. Whilst the name belongs to stroom
     * it may be helpful for the external system to know what the name is, but the name should not be used for referencing
     * the DocRef between systems as it could easily be out of sync.
     *
     * @param uuid The uuid of the document you want to rename.
     * @param name The new name of the document.
     * @return A doc ref for the renamed document.
     * @throws QueryApiException  If something goes wrong
     */
    @PUT
    @Path("/rename/{uuid}/{name}")
    Response documentRenamed(@PathParam("uuid") String uuid,
                             @PathParam("name") String name) throws QueryApiException;

    /**
     * The document with this UUID is being deleted in Stroom.
     *
     * @param uuid The uuid of the document you want to delete.
     * @throws QueryApiException  If something goes wrong
     * @return No content if OK
     */
    @DELETE
    @Path("/delete/{uuid}")
    Response deleteDocument(@PathParam("uuid") String uuid) throws QueryApiException;

    /**
     * Import the data as a new document.
     * @param uuid The UUID of the document to import
     * @param name The name of the document to import
     * @param confirmed Is the import a confirmed one? If it isn't then this is just a dry run.
     * @param dataMap The data to import
     * @return The created document
     * @throws QueryApiException  If something goes wrong
     */
    @POST
    @Path("/import/{uuid}/{name}/{confirmed}")
    Response importDocument(@PathParam("uuid") String uuid,
                            @PathParam("name") String name,
                            @PathParam("confirmed") Boolean confirmed,
                            Map<String, String> dataMap) throws QueryApiException;

    /**
     * Export the given document
     * @param uuid The UUID of the document to export
     * @return A Map of Strings by Strings containing the exported data.
     * @throws QueryApiException  If something goes wrong
     */
    @GET
    @Path("/export/{uuid}")
    Response exportDocument(@PathParam("uuid") String uuid) throws QueryApiException;
}
