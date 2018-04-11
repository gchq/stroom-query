package stroom.query.audit.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.rest.QueryResource;
import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.Closeable;

public class QueryResourceHttpClient implements QueryResource, Closeable {

    private final Client httpClient;

    private final String dataSourceUrl;
    private final String searchUrl;
    private final String destroyUrl;

    public QueryResourceHttpClient(final String baseUrl) {
        httpClient = ClientBuilder.newClient(new ClientConfig().register(ClientResponse.class));

        this.dataSourceUrl = String.format("%s/queryApi/v1/dataSource", baseUrl);
        this.searchUrl = String.format("%s/queryApi/v1/search", baseUrl);
        this.destroyUrl = String.format("%s/queryApi/v1/destroy", baseUrl);
    }

    public void close() {
        if (null != httpClient) {
            this.httpClient.close();
        }
    }

    @Override
    public Response getDataSource(final ServiceUser user,
                                  final DocRef docRef) {
        return httpClient
                .target(this.dataSourceUrl)
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(docRef));
    }

    @Override
    public Response search(final ServiceUser user,
                           final SearchRequest request) {
        return httpClient
                .target(this.searchUrl)
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(request));
    }

    @Override
    public Response destroy(final ServiceUser user,
                            final QueryKey queryKey) {
        return httpClient
                .target(this.destroyUrl)
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(queryKey));
    }
}
