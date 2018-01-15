package stroom.query.audit.service;

import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.util.shared.QueryApiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic form of DocRef service, templated to the class the encapsulates the DocRef
 * @param <T> The class that represents the full document
 */
public interface DocRefService <T> {
    /**
     * Retrieve all of the index entities currently registered
     * @return The list of all known index entities
     * @throws QueryApiException  If something goes wrong
     */
    List<T> getAll() throws QueryApiException;

    /**
     * Retrieve the full config for the given DocRef
     * @param uuid              The UUID of the docRef to return
     * @return                  The full implementation specific config for this docRef.
     * @throws QueryApiException  If something goes wrong
     */
    Optional<T> get(String uuid) throws QueryApiException;

    /**
     * Retrieve the info about a doc ref
     * @param uuid The UUID of the doc ref to find
     * @return The DocRefInfo for the UUID
     * @throws QueryApiException If something goes wrong
     */
    Optional<DocRefInfo> getInfo(String uuid) throws QueryApiException;

    /**
     * A new document has been created in Stroom
     *
     * @param uuid              The UUID of the document as created by stroom
     * @param name              The name of the document to be created.
     * @return The new index entity
     * @throws QueryApiException  If something goes wrong
     */
    Optional<T> createDocument(String uuid, String name) throws QueryApiException;

    /**
     * Used to update a specific document.
     * This will be used by our user interface to configure the underlying index and the stroom references to it
     * @param uuid The UUID of DocRef used to store the index configuration
     * @param updatedConfig The updated configuration
     * @return The updated config
     * @throws QueryApiException  If something goes wrong
     */
    Optional<T> update(String uuid, T updatedConfig) throws QueryApiException;

    /**
     * A notification from Stroom that a document is being copied. The external system should
     * copy it's configuration for the original into a new entity.
     *
     * @param originalUuid      The uuid of the document being copied
     * @param copyUuid          The uuid of the copy
     * @return The new index entity
     * @throws QueryApiException  If something goes wrong
     */
    Optional<T> copyDocument(String originalUuid, String copyUuid) throws QueryApiException;

    /**
     * A Notification from Stroom that the document has been 'moved'. In most cases the external system
     * will not care about this.
     *
     * @param uuid             The uuid of the document that was moved
     * @return The updated index entity
     * @throws QueryApiException  If something goes wrong
     */
    Optional<T> documentMoved(String uuid) throws QueryApiException;

    /**
     * A notifiation from Stroom that the name of a document has been changed. Whilst the name belongs to stroom
     * it may be helpful for the external system to know what the name is, but the name should not be used for referencing
     * the DocRef between systems as it could easily be out of sync.
     *
     * @param uuid The uuid of the document you want to rename.
     * @param name The new name of the document.
     * @return The updated index entity
     * @throws QueryApiException  If something goes wrong
     */
    Optional<T> documentRenamed(String uuid, String name) throws QueryApiException;

    /**
     * The document with this UUID is being deleted in Stroom.
     *
     * @param uuid The uuid of the document you want to delete.
     * @throws QueryApiException  If something goes wrong
     * @return Optional boolean, if missing, the document could not be found, if false, it could not be deleted
     */
    Optional<Boolean> deleteDocument(String uuid) throws QueryApiException;

    /**
     * Used to export the full details of a document for transfer.
     * @param uuid The UUID of the document to export
     * @return The exported data
     * @throws QueryApiException If something goes wrong
     */
    ExportDTO exportDocument(String uuid) throws QueryApiException;

    /**
     * Used to import a document into the system
     * @param uuid The UUID of the document to import
     * @param name The Name of the document to import
     * @param confirmed Used to indicate if this is a dry run
     * @param dataMap The data that gives all the implementation specific details
     * @return The imported document
     * @throws QueryApiException If something goes wrong
     */
    Optional<T> importDocument(String uuid,
                               String name,
                               Boolean confirmed,
                               Map<String, String> dataMap) throws QueryApiException;
}
