package stroom.query.audit.service;

import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.security.ServiceUser;

import java.util.Optional;

public interface QueryService {
    Optional<DataSource> getDataSource(ServiceUser user,
                                       DocRef docRef) throws Exception;

    Optional<SearchResponse> search(ServiceUser user,
                                    SearchRequest request) throws Exception;

    Boolean destroy(ServiceUser user,
                    QueryKey queryKey) throws Exception;
}
