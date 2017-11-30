package stroom.query.audit;

import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.core.Response;

public class QueryResourceHttpClient implements QueryResource {

    private final SimpleJsonHttpClient<QueryApiException> httpClient;
    private final String baseUrl;

    private final String dataSourceUrl;
    private final String searchUrl;
    private final String destroyUrl;

    public QueryResourceHttpClient(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new SimpleJsonHttpClient<>(QueryApiException::new);

        this.dataSourceUrl = String.format("%s/queryApi/v1/dataSource", baseUrl);
        this.searchUrl = String.format("%s/queryApi/v1/search", baseUrl);
        this.destroyUrl = String.format("%s/queryApi/v1/destroy", baseUrl);
    }

    @Override
    public Response getDataSource(final DocRef docRef) throws QueryApiException {
        return httpClient.post(this.dataSourceUrl)
                .body(docRef)
                .send();
    }

    @Override
    public Response search(final SearchRequest request) throws QueryApiException {
        return httpClient.post(this.searchUrl)
                .body(request)
                .send();
    }

    @Override
    public Response destroy(final QueryKey queryKey) throws QueryApiException {
        return httpClient.post(this.destroyUrl)
                .body(queryKey)
                .send();
    }
}
