package stroom.query.audit.service;

import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import java.util.Optional;

public interface QueryService {
    Optional<DataSource> getDataSource(ServiceUser authenticatedServiceUser,
                                       DocRef docRef) throws QueryApiException;

    Optional<SearchResponse> search(ServiceUser authenticatedServiceUser,
                                    SearchRequest request) throws QueryApiException;

    Boolean destroy(ServiceUser authenticatedServiceUser,
                    QueryKey queryKey) throws QueryApiException;
}
