package stroom.query.audit.service;

import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.DocRefInfo;
import stroom.query.audit.ExportDTO;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.security.ServiceUser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic form of DocRef service, templated to the class the encapsulates the DocRef
 * @param <T> The class that represents the full document
 */
public interface DocRefService <T extends DocRefEntity> {
    /**
     * Get the doc ref type that this service wraps.
     * @return The doc ref type name
     */
    String getType();

    /**
     * Retrieve all of the index entities currently registered
     * @param user The logged in user
     * @if anything goes wrong
     * @return The list of all known index entities
     */
    List<T> getAll(ServiceUser user) ;

    /**
     * Retrieve the full config for the given DocRef
     * @param user The logged in user
     * @param uuid              The UUID of the docRef to return
     * @if anything goes wrong
     * @return                  The full implementation specific config for this docRef.
     */
    Optional<T> get(ServiceUser user, String uuid) ;

    /**
     * Retrieve the info about a doc ref
     * @param user The logged in user
     * @param uuid The UUID of the doc ref to find
     * @if anything goes wrong
     * @return The DocRefInfo for the UUID
     */
    default Optional<DocRefInfo> getInfo(ServiceUser user, String uuid) {
        return get(user, uuid).map(d -> new DocRefInfo.Builder()
                .docRef(new DocRef.Builder()
                        .uuid(d.getUuid())
                        .name(d.getName())
                        .type(getType())
                        .build())
                .createUser(d.getCreateUser())
                .createTime(d.getCreateTime())
                .updateUser(d.getUpdateUser())
                .updateTime(d.getUpdateTime())
                .build());
    }

    /**
     * A new document has been created in Stroom
     *
     * @param user The logged in user
     * @param uuid              The UUID of the document as created by stroom
     * @param name              The name of the document to be created.
     * @if anything goes wrong
     * @return The new index entity
     */
    Optional<T> createDocument(ServiceUser user, String uuid, String name) ;

    /**
     * Used to update a specific document.
     * This will be used by our user interface to configure the underlying index and the stroom references to it
     * @param user The logged in user
     * @param uuid The UUID of DocRef used to store the index configuration
     * @param updatedConfig The updated configuration
     * @if anything goes wrong
     * @return The updated config
     */
    Optional<T> update(ServiceUser user, String uuid, T updatedConfig) ;

    /**
     * A notification from Stroom that a document is being copied. The external system should
     * copy it's configuration for the original into a new entity.
     *
     * @param user The logged in user
     * @param originalUuid      The uuid of the document being copied
     * @param copyUuid          The uuid of the copy
     * @if anything goes wrong
     * @return The new index entity
     */
    Optional<T> copyDocument(ServiceUser user, String originalUuid, String copyUuid) ;

    /**
     * A Notification from Stroom that the document has been 'moved'. In most cases the external system
     * will not care about this.
     *
     * @param user The logged in user
     * @param uuid             The uuid of the document that was moved
     * @if anything goes wrong
     * @return The updated index entity
     */
    Optional<T> moveDocument(ServiceUser user, String uuid) ;

    /**
     * A notifiation from Stroom that the name of a document has been changed. Whilst the name belongs to stroom
     * it may be helpful for the external system to know what the name is, but the name should not be used for referencing
     * the DocRef between systems as it could easily be out of sync.
     *
     * @param user The logged in user
     * @param uuid The uuid of the document you want to rename.
     * @param name The new name of the document.
     * @if anything goes wrong
     * @return The updated index entity
     */
    Optional<T> renameDocument(ServiceUser user, String uuid, String name) ;

    /**
     * The document with this UUID is being deleted in Stroom.
     *
     * @param user The logged in user
     * @param uuid The uuid of the document you want to delete.
     * @if anything goes wrong
     * @return Optional boolean, if missing, the document could not be found, if false, it could not be deleted
     */
    Optional<Boolean> deleteDocument(ServiceUser user, String uuid) ;

    /**
     * Used to export the full details of a document for transfer.
     * @param user The logged in user
     * @param uuid The UUID of the document to export
     * @if anything goes wrong
     * @return The exported data
     */
    ExportDTO exportDocument(ServiceUser user, String uuid) ;

    /**
     * Used to import a document into the system
     * @param user The logged in user
     * @param uuid The UUID of the document to import
     * @param name The Name of the document to import
     * @param confirmed Used to indicate if this is a dry run
     * @param dataMap The data that gives all the implementation specific details
     * @if anything goes wrong
     * @return The imported document
     */
    Optional<T> importDocument(ServiceUser user,
                               String uuid,
                               String name,
                               Boolean confirmed,
                               Map<String, String> dataMap) ;
}
