package stroom.query.audit.client;

import stroom.query.audit.rest.DocRefResource;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DocRefResourceHttpClient<T> implements DocRefResource<T> {

    @FunctionalInterface
    private interface ImportUrlFunction {
        String getUrl(final String uuid, final String name, final Boolean confirmed);
    }

    private final SimpleJsonHttpClient<QueryApiException> httpClient;
    private final String getAllUrl;
    private final Function<String, String> getUrl;
    private final Function<String, String> getInfoUrl;
    private final BiFunction<String, String, String> createUrl;
    private final Function<String, String> updateUrl;
    private final BiFunction<String, String, String> copyUrl;
    private final Function<String, String> moveUrl;
    private final BiFunction<String, String, String> renameUrl;
    private final Function<String, String> deleteUrl;
    private final ImportUrlFunction importUrl;
    private final Function<String, String> exportUrl;

    public DocRefResourceHttpClient(final String baseUrl) {
        this.getAllUrl = String.format("%s/docRefApi/v1/",
                baseUrl);
        this.getUrl = (uuid) -> String.format("%s/docRefApi/v1/%s",
                baseUrl,
                uuid);
        this.getInfoUrl = (uuid) -> String.format("%s/docRefApi/v1/%s/info",
                baseUrl,
                uuid);
        this.createUrl = (uuid, name) -> String.format("%s/docRefApi/v1/create/%s/%s",
                baseUrl,
                uuid,
                name);
        this.updateUrl = (uuid) -> String.format("%s/docRefApi/v1/update/%s",
                baseUrl,
                uuid);
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
        this.importUrl = (uuid, name, confirmed) -> String.format("%s/docRefApi/v1/import/%s/%s/%s",
                baseUrl,
                uuid,
                name,
                confirmed);
        this.exportUrl = (uuid) -> String.format("%s/docRefApi/v1/export/%s",
                baseUrl,
                uuid);
        this.httpClient = new SimpleJsonHttpClient<>(QueryApiException::new);
    }

    @Override
    public Response getAll(final ServiceUser authenticatedServiceUser) throws QueryApiException {
        return httpClient
                .get(getAllUrl)
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response get(final ServiceUser authenticatedServiceUser,
                        final String uuid) throws QueryApiException {
        return httpClient
                .get(getUrl.apply(uuid))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response getInfo(final ServiceUser authenticatedServiceUser,
                            final String uuid) throws QueryApiException {
        return httpClient
                .get(getInfoUrl.apply(uuid))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response createDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid, final String name) throws QueryApiException {
        return httpClient
                .post(createUrl.apply(uuid, name))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response update(final ServiceUser authenticatedServiceUser,
                           final String uuid,
                           final T updatedConfig) throws QueryApiException {
        return httpClient
                .put(this.updateUrl.apply(uuid))
                .body(updatedConfig)
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response copyDocument(final ServiceUser authenticatedServiceUser,
                                 final String originalUuid,
                                 final String copyUuid) throws QueryApiException {
        return httpClient
                .post(copyUrl.apply(originalUuid, copyUuid))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response documentMoved(final ServiceUser authenticatedServiceUser,
                                  final String uuid) throws QueryApiException {
        return httpClient
                .put(moveUrl.apply(uuid))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response documentRenamed(final ServiceUser authenticatedServiceUser,
                                    final String uuid,
                                    final String name) throws QueryApiException {
        return httpClient
                .put(renameUrl.apply(uuid, name))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response deleteDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid) throws QueryApiException {
        return httpClient
                .delete(deleteUrl.apply(uuid))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }

    @Override
    public Response importDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap) throws QueryApiException {
        return httpClient
                .post(importUrl.getUrl(uuid, name, confirmed))
                .body(dataMap)
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }


    @Override
    public Response exportDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid) throws QueryApiException {
        return httpClient
                .get(exportUrl.apply(uuid))
                .jwt(authenticatedServiceUser.getJwt())
                .send();
    }
}
