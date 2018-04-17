package stroom.query.testing;

import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.rest.QueryResource;
import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Used in tests to monitor calls made to a query resource,
 * it forwards the calls to the wrapped client.
 *
 * It keeps track of all the calls made to it, so that tests can assert
 * the correct calls are being made.
 */
public class QueryResourceSpy<WRAPPED extends QueryResource & Closeable>
        implements QueryResource, Closeable {

    private final WRAPPED wrapped;

    public static <W extends QueryResource & Closeable>
    QueryResourceSpy wrapping(final W wrapped) {
        return new QueryResourceSpy<>(wrapped);
    }

    private QueryResourceSpy(final WRAPPED wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void close() throws IOException {
        this.wrapped.close();
    }

    /**
     * Inner class to encapsulate calls made to get data source
     */
    public class GetDataSourceCall {
        private final ServiceUser user;
        private final DocRef docRef;
        private final Response response;

        private GetDataSourceCall(final ServiceUser user,
                                  final DocRef docRef,
                                  final Response response) {
            this.user = user;
            this.docRef = docRef;
            this.response = response;
        }

        public ServiceUser getUser() {
            return user;
        }

        public DocRef getDocRef() {
            return docRef;
        }

        public Response getResponse() {
            return response;
        }
    }

    private final List<GetDataSourceCall> getDataSourceCalls = new ArrayList<>();

    @Override
    public Response getDataSource(final ServiceUser user,
                                  final DocRef docRef) {
        Response response = wrapped.getDataSource(user, docRef);
        getDataSourceCalls.add(new GetDataSourceCall(user, docRef, response));
        return response;
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
        private final Response response;

        private SearchCall(final ServiceUser user,
                           final SearchRequest request,
                           final Response response) {
            this.user = user;
            this.request = request;
            this.response = response;
        }

        public ServiceUser getUser() {
            return user;
        }

        public SearchRequest getRequest() {
            return request;
        }

        public Response getResponse() {
            return response;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SearchCall{");
            sb.append("user=").append(user);
            sb.append(", request=").append(request);
            sb.append(", response=").append(response);
            sb.append('}');
            return sb.toString();
        }
    }

    private final List<SearchCall> searchCalls = new ArrayList<>();

    @Override
    public Response search(final ServiceUser user,
                           final SearchRequest request) {
        Response response = wrapped.search(user, request);
        searchCalls.add(new SearchCall(user, request, response));
        return response;
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
        private final Response response;

        private DestroyCall(final ServiceUser user,
                           final QueryKey queryKey,
                           final Response response) {
            this.user = user;
            this.queryKey = queryKey;
            this.response = response;
        }

        public ServiceUser getUser() {
            return user;
        }

        public QueryKey getQueryKey() {
            return queryKey;
        }

        public Response getResponse() {
            return response;
        }
    }

    private final List<DestroyCall> destroyCalls = new ArrayList<>();

    @Override
    public Response destroy(final ServiceUser user,
                            final QueryKey queryKey) {
        Response response = wrapped.destroy(user, queryKey);
        destroyCalls.add(new DestroyCall(user, queryKey, response));
        return response;
    }

    public List<DestroyCall> getDestroyCalls() {
        return destroyCalls;
    }

    public void clearCalls() {
        Stream.of(getDataSourceCalls, searchCalls, destroyCalls).forEach(List::clear);
    }
}
