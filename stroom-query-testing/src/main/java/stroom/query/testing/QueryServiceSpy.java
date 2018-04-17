package stroom.query.testing;

import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.QueryApiException;
import stroom.query.audit.service.QueryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class QueryServiceSpy<WRAPPED extends QueryService> implements QueryService {
    private final WRAPPED wrapped;

    public static <W extends QueryService>
    QueryServiceSpy wrapping(final W wrapped) {
        return new QueryServiceSpy<>(wrapped);
    }

    private QueryServiceSpy(final WRAPPED wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getType() {
        return wrapped.getType();
    }

    /**
     * Inner class to encapsulate calls made to get data source
     */
    public class GetDataSourceCall {
        private final ServiceUser user;
        private final DocRef docRef;
        private final DataSource response;
        private final QueryApiException exception;

        private GetDataSourceCall(final ServiceUser user,
                                  final DocRef docRef,
                                  final DataSource response) {
            this(user, docRef, response, null);
        }

        private GetDataSourceCall(final ServiceUser user,
                                  final DocRef docRef,
                                  final QueryApiException exception) {
            this(user, docRef, null, exception);
        }
        private GetDataSourceCall(final ServiceUser user,
                                  final DocRef docRef,
                                  final DataSource response,
                                  final QueryApiException exception) {
            this.user = user;
            this.docRef = docRef;
            this.response = response;
            this.exception = exception;
        }

        public ServiceUser getUser() {
            return user;
        }

        public DocRef getDocRef() {
            return docRef;
        }

        public DataSource getResponse() {
            return response;
        }

        public QueryApiException getException() {
            return exception;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("GetDataSourceCall{");
            sb.append("user=").append(user);
            sb.append(", docRef=").append(docRef);
            sb.append(", response=").append(response);
            sb.append(", exception=").append(exception);
            sb.append('}');
            return sb.toString();
        }
    }

    private final List<GetDataSourceCall> getDataSourceCalls = new ArrayList<>();

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) throws QueryApiException {
        try {
            final DataSource response = wrapped.getDataSource(user, docRef)
                    .orElseThrow(() -> new QueryApiException("Missing response"));
            getDataSourceCalls.add(new GetDataSourceCall(user, docRef, response));
            return Optional.of(response);
        } catch (final QueryApiException e) {
            getDataSourceCalls.add(new GetDataSourceCall(user, docRef, e));
            throw e;
        }
    }

    public List<GetDataSourceCall> getGetDataSourceCalls() {
        return getDataSourceCalls;
    }

    /**
     * Inner class to encapsulate calls made to search
     */
    public static class SearchCall {
        private final ServiceUser user;
        private final SearchRequest request;
        private final SearchResponse response;
        private final QueryApiException exception;

        private SearchCall(final ServiceUser user,
                           final SearchRequest request,
                           final SearchResponse response) {
            this(user, request, response, null);
        }

        private SearchCall(final ServiceUser user,
                           final SearchRequest request,
                           final QueryApiException exception) {
            this(user, request, null, exception);
        }
        private SearchCall(final ServiceUser user,
                           final SearchRequest request,
                           final SearchResponse response,
                           final QueryApiException exception) {
            this.user = user;
            this.request = request;
            this.response = response;
            this.exception = exception;
        }

        public ServiceUser getUser() {
            return user;
        }

        public SearchRequest getRequest() {
            return request;
        }

        public SearchResponse getResponse() {
            return response;
        }

        public QueryApiException getException() {
            return exception;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SearchCall{");
            sb.append("user=").append(user);
            sb.append(", request=").append(request);
            sb.append(", response=").append(response);
            sb.append(", exception=").append(exception);
            sb.append('}');
            return sb.toString();
        }
    }

    private final List<SearchCall> searchCalls = new ArrayList<>();

    @Override
    public Optional<SearchResponse> search(final ServiceUser user,
                                           final SearchRequest request) throws QueryApiException {
        try {
            final SearchResponse response = wrapped.search(user, request)
                    .orElseThrow(() -> new QueryApiException("Missing response"));
            searchCalls.add(new SearchCall(user, request, response));
            return Optional.of(response);
        } catch (final QueryApiException e) {
            searchCalls.add(new SearchCall(user, request, e));
            throw e;
        }
    }

    public List<SearchCall> getSearchCalls() {
        return searchCalls;
    }

    /**
     * Inner class to encapsulate calls made to destroy
     */
    public class DestroyCall {
        private final ServiceUser user;
        private final QueryKey queryKey;
        private final Boolean response;
        private final QueryApiException exception;

        private DestroyCall(final ServiceUser user,
                            final QueryKey queryKey,
                            final QueryApiException exception) {
            this(user, queryKey, null, exception);
        }
        private DestroyCall(final ServiceUser user,
                            final QueryKey queryKey,
                            final Boolean response) {
            this(user, queryKey, response, null);
        }
        private DestroyCall(final ServiceUser user,
                            final QueryKey queryKey,
                            final Boolean response,
                            final QueryApiException exception) {
            this.user = user;
            this.queryKey = queryKey;
            this.response = response;
            this.exception = exception;
        }

        public ServiceUser getUser() {
            return user;
        }

        public QueryKey getQueryKey() {
            return queryKey;
        }

        public Boolean getResponse() {
            return response;
        }

        public QueryApiException getException() {
            return exception;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("DestroyCall{");
            sb.append("user=").append(user);
            sb.append(", queryKey=").append(queryKey);
            sb.append(", response=").append(response);
            sb.append(", exception=").append(exception);
            sb.append('}');
            return sb.toString();
        }
    }

    private final List<DestroyCall> destroyCalls = new ArrayList<>();

    @Override
    public Boolean destroy(final ServiceUser user,
                           final QueryKey queryKey) throws QueryApiException {
        try {
            final Boolean response = wrapped.destroy(user, queryKey);
            destroyCalls.add(new DestroyCall(user, queryKey, response));
            return response;
        } catch (final QueryApiException e) {
            destroyCalls.add(new DestroyCall(user, queryKey, e));
            throw e;
        }
    }

    public List<DestroyCall> getDestroyCalls() {
        return destroyCalls;
    }

    public void clearCalls() {
        Stream.of(getDataSourceCalls, searchCalls, destroyCalls).forEach(List::clear);
    }

    @Override
    public Optional<DocRef> getDocRefForQueryKey(final ServiceUser user,
                                                 final QueryKey queryKey) throws QueryApiException {
        return Optional.empty();
    }
}
