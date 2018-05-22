package stroom.query.audit.service;

import stroom.datasource.api.v2.DataSource;
import stroom.docref.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.security.ServiceUser;

import java.util.Optional;

public interface QueryService {
    /**
     * Get the doc ref type that this service wraps.
     *
     * @return The doc ref type name
     */
    String getType();

    /**
     * Get the details of the DataSource given by the DocRef.
     * Used to build user interfaces for querying the specific data.
     *
     * @param user   The authenticated user
     * @param docRef The Doc Ref of the DataSource to fetch
     * @return The DataSource definition for the given doc ref
     * @throws QueryApiException if anything goes wrong
     */
    Optional<DataSource> getDataSource(ServiceUser user,
                                       DocRef docRef) throws QueryApiException;

    /**
     * Conduct a search on the data, it may be a successive call for long running searches.
     *
     * @param user    The authenticated user
     * @param request The details of the search
     * @return An optional search response.
     * @throws QueryApiException if anything goes wrong
     */
    Optional<SearchResponse> search(ServiceUser user,
                                    SearchRequest request) throws QueryApiException;

    /**
     * Destroy any existing query being conducted under the given key.
     *
     * @param user     The authenticated user
     * @param queryKey The query key that was given with the search request.
     * @return Success indicator
     * @throws QueryApiException if anything goes wrong
     */
    Boolean destroy(ServiceUser user,
                    QueryKey queryKey) throws QueryApiException;

    /**
     * Used by REST layer to retrieve the doc ref for a given query key.
     * Primarily used to check the permissions for the given doc ref.
     *
     * @param user     The authenticated user
     * @param queryKey The query key, it should match a current query
     * @return The DocRef of the query, if found, if not found the result will be empty.
     * @throws QueryApiException if anything goes wrong
     */
    Optional<DocRef> getDocRefForQueryKey(ServiceUser user,
                                          QueryKey queryKey) throws QueryApiException;
}
