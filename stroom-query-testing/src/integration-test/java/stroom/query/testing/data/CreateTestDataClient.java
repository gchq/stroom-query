package stroom.query.testing.data;

import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.util.function.BiFunction;

public class CreateTestDataClient implements CreateTestDataResource, Closeable {
    private final Client httpClient;

    private final BiFunction<String, String, String> createTestDataUrl;

    public CreateTestDataClient(final String baseUrl) {
        this.createTestDataUrl = (docRefUuid, seed) -> String.format("%s/createTestData/%s/%s",
                baseUrl, docRefUuid, seed);
        httpClient = ClientBuilder.newClient();
    }

    public void close() {
        if (null != httpClient) {
            this.httpClient.close();
        }
    }

    @Override
    public Response createTestData(final ServiceUser user,
                                   final String docRefUuid,
                                   final String seed) {
        return httpClient
                .target(createTestDataUrl.apply(docRefUuid, seed))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(""));
    }
}
