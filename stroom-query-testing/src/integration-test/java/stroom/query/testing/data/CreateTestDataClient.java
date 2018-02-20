package stroom.query.testing.data;

import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.util.function.Function;

public class CreateTestDataClient implements CreateTestDataResource, Closeable {
    private final Client httpClient;

    private final Function<String, String> createTestDataUrl;

    public CreateTestDataClient(final String baseUrl) {
        this.createTestDataUrl = seed -> String.format("%s/createTestData/%s",
                baseUrl, seed);
        httpClient = ClientBuilder.newClient();
    }

    public void close() {
        if (null != httpClient) {
            this.httpClient.close();
        }
    }

    @Override
    public Response createTestData(final ServiceUser user,
                                   final String seed) {
        return httpClient
                .target(createTestDataUrl.apply(seed))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(""));
    }
}
