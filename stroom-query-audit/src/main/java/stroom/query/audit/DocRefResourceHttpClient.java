package stroom.query.audit;

import stroom.util.shared.QueryApiException;

import javax.ws.rs.core.Response;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DocRefResourceHttpClient implements DocRefResource {

    private final SimpleJsonHttpClient<QueryApiException> httpClient;
    private final String getAllUrl;
    private final Function<String, String> getUrl;
    private final BiFunction<String, String, String> createUrl;
    private final BiFunction<String, String, String> copyUrl;
    private final Function<String, String> moveUrl;
    private final BiFunction<String, String, String> renameUrl;
    private final Function<String, String> deleteUrl;

    public DocRefResourceHttpClient(final String baseUrl) {
        this.getAllUrl = String.format("%s/docRefApi/v1/",
                baseUrl);
        this.getUrl = (uuid) -> String.format("%s/docRefApi/v1/%s",
                baseUrl,
                uuid);
        this.createUrl = (uuid, name) -> String.format("%s/docRefApi/v1/create/%s/%s",
                baseUrl,
                uuid,
                name);
        this.copyUrl = (originalUuid, copyUuid) -> String.format("%s/docRefApi/v1/copy/%s/%s",
                baseUrl,
                originalUuid,
                copyUuid);
        this.moveUrl = (uuid) -> String.format("%s/docRefApi/v1/move/%s/%s",
                baseUrl,
                uuid);
        this.renameUrl = (uuid, name) -> String.format("%s/docRefApi/v1/rename/%s/%s",
                baseUrl,
                uuid,
                name);
        this.deleteUrl = (uuid) -> String.format("%s/docRefApi/v1/delete/%s",
                baseUrl,
                uuid);
        this.httpClient = new SimpleJsonHttpClient<>(QueryApiException::new);
    }

    @Override
    public Response getAll() throws QueryApiException {
        return httpClient
                .get(getAllUrl)
                .send();
    }

    @Override
    public Response get(String uuid) throws QueryApiException {
        return httpClient
                .get(getUrl.apply(uuid))
                .send();
    }

    @Override
    public Response createDocument(final String uuid, final String name) throws QueryApiException {
        return httpClient
                .post(createUrl.apply(uuid, name))
                .send();
    }

    @Override
    public Response copyDocument(final String originalUuid,
                                 final String copyUuid) throws QueryApiException {
        return httpClient
                .post(copyUrl.apply(originalUuid, copyUuid))
                .send();
    }

    @Override
    public Response documentMoved(final String uuid) throws QueryApiException {
        return httpClient
                .put(moveUrl.apply(uuid))
                .send();
    }

    @Override
    public Response documentRenamed(final String uuid,
                                    final String name) throws QueryApiException {
        return httpClient
                .put(renameUrl.apply(uuid, name))
                .send();
    }

    @Override
    public Response deleteDocument(final String uuid) throws QueryApiException {
        return httpClient
                .delete(deleteUrl.apply(uuid))
                .send();
    }
}
