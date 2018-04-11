package stroom.query.audit.client;

import org.eclipse.jetty.http.HttpStatus;
import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.QueryService;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.util.Optional;

public class QueryServiceHttpClient implements QueryService, Closeable {

    private final QueryResourceHttpClient httpClient;

    public QueryServiceHttpClient(final String baseUrl) {
        this.httpClient = new QueryResourceHttpClient(baseUrl);
    }

    @Override
    public void close() {
        httpClient.close();
    }

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) {
        final Response response = httpClient.getDataSource(user, docRef);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(DataSource.class));
        } else {
            response.close();
        }

        return Optional.empty();
    }

    @Override
    public Optional<SearchResponse> search(final ServiceUser user,
                                           final SearchRequest request) {
        final Response response = httpClient.search(user, request);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(SearchResponse.class));
        } else {
            response.close();
        }

        return Optional.empty();
    }

    @Override
    public Boolean destroy(final ServiceUser user,
                           final QueryKey queryKey) {
        final Response response = httpClient.destroy(user, queryKey);

        if (response.getStatus() == HttpStatus.OK_200) {
            return response.readEntity(Boolean.class);
        } else {
            response.close();
        }

        return false;
    }

    @Override
    public Optional<DocRef> getDocRefForQueryKey(final ServiceUser user,
                                                 final QueryKey queryKey) {
        return Optional.empty();
    }
}
