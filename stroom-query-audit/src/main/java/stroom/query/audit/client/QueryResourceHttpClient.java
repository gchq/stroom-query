package stroom.query.audit.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.rest.QueryResource;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class QueryResourceHttpClient implements QueryResource {

    private final Client httpClient;
    private final String baseUrl;

    private final String dataSourceUrl;
    private final String searchUrl;
    private final String destroyUrl;

    public QueryResourceHttpClient(final String baseUrl) {
        this.baseUrl = baseUrl;
        httpClient = ClientBuilder.newClient(new ClientConfig().register(ClientResponse.class));

        this.dataSourceUrl = String.format("%s/queryApi/v1/dataSource", baseUrl);
        this.searchUrl = String.format("%s/queryApi/v1/search", baseUrl);
        this.destroyUrl = String.format("%s/queryApi/v1/destroy", baseUrl);
    }

    @Override
    public Response getDataSource(final ServiceUser authenticatedServiceUser,
                                  final DocRef docRef) throws QueryApiException {
        return httpClient
                .target(this.dataSourceUrl)
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(docRef));
    }

    @Override
    public Response search(final ServiceUser authenticatedServiceUser,
                           final SearchRequest request) throws QueryApiException {
        return httpClient
                .target(this.searchUrl)
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(request));
    }

    @Override
    public Response destroy(final ServiceUser authenticatedServiceUser,
                            final QueryKey queryKey) throws QueryApiException {
        return httpClient
                .target(this.destroyUrl)
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(queryKey));
    }
}
