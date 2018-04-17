package stroom.query.audit.client;

import org.eclipse.jetty.http.HttpStatus;
import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.QueryApiException;
import stroom.query.audit.service.QueryService;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.util.Optional;

public class QueryServiceHttpClient implements QueryService, Closeable {

    private final String type;
    private final QueryResourceHttpClient httpClient;

    public QueryServiceHttpClient(final String type,
                                  final String baseUrl) {
        this.type = type;
        this.httpClient = new QueryResourceHttpClient(baseUrl);
    }

    @Override
    public void close() {
        httpClient.close();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) throws QueryApiException {
        final Response response = httpClient.getDataSource(user, docRef);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(DataSource.class));
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<SearchResponse> search(final ServiceUser user,
                                           final SearchRequest request) throws QueryApiException {
        final Response response = httpClient.search(user, request);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(SearchResponse.class));
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Boolean destroy(final ServiceUser user,
                           final QueryKey queryKey) throws QueryApiException {
        final Response response = httpClient.destroy(user, queryKey);

        if (response.getStatus() == HttpStatus.OK_200) {
            return response.readEntity(Boolean.class);
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<DocRef> getDocRefForQueryKey(final ServiceUser user,
                                                 final QueryKey queryKey) {
        return Optional.empty();
    }
}
