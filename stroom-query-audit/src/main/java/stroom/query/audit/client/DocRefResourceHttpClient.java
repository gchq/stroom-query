package stroom.query.audit.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import stroom.query.audit.rest.DocRefResource;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DocRefResourceHttpClient<T> implements DocRefResource<T> {

    @FunctionalInterface
    private interface ImportUrlFunction {
        String getUrl(final String uuid, final String name, final Boolean confirmed);
    }

    @FunctionalInterface
    private interface TriStringFunction {
        String getUrl(final String one, final String two, final String three);
    }

    private final Client httpClient;
    private final String getAllUrl;
    private final Function<String, String> getUrl;
    private final Function<String, String> getInfoUrl;
    private final TriStringFunction createUrl;
    private final Function<String, String> updateUrl;
    private final TriStringFunction copyUrl;
    private final BiFunction<String, String, String> moveUrl;
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
        this.createUrl = (uuid, name, parentFolderUUID) -> String.format("%s/docRefApi/v1/create/%s/%s/%s",
                baseUrl,
                uuid,
                name,
                parentFolderUUID);
        this.updateUrl = (uuid) -> String.format("%s/docRefApi/v1/update/%s",
                baseUrl,
                uuid);
        this.copyUrl = (originalUuid, copyUuid, parentFolderUUID) -> String.format("%s/docRefApi/v1/copy/%s/%s/%s",
                baseUrl,
                originalUuid,
                copyUuid,
                parentFolderUUID);
        this.moveUrl = (uuid, parentFolderUUID) -> String.format("%s/docRefApi/v1/move/%s/%s",
                baseUrl,
                uuid,
                parentFolderUUID);
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
        httpClient = ClientBuilder.newClient(new ClientConfig().register(ClientResponse.class));
    }

    @Override
    public Response getAll(final ServiceUser authenticatedServiceUser) throws QueryApiException {
        return httpClient
                .target(getAllUrl)
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }

    @Override
    public Response get(final ServiceUser authenticatedServiceUser,
                        final String uuid) throws QueryApiException {
        return httpClient
                .target(getUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }

    @Override
    public Response getInfo(final ServiceUser authenticatedServiceUser,
                            final String uuid) throws QueryApiException {
        return httpClient
                .target(getInfoUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }

    @Override
    public Response createDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid,
                                   final String name,
                                   final String parentFolderUUID) throws QueryApiException {
        return httpClient
                .target(createUrl.getUrl(uuid, name, parentFolderUUID))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(""));
    }

    @Override
    public Response update(final ServiceUser authenticatedServiceUser,
                           final String uuid,
                           final T updatedConfig) throws QueryApiException {
        return httpClient
                .target(this.updateUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .put(Entity.json(updatedConfig));
    }

    @Override
    public Response copyDocument(final ServiceUser authenticatedServiceUser,
                                 final String originalUuid,
                                 final String copyUuid,
                                 final String parentFolderUUID) throws QueryApiException {
        return httpClient
                .target(copyUrl.getUrl(originalUuid, copyUuid, parentFolderUUID))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(""));
    }

    @Override
    public Response moveDocument(final ServiceUser authenticatedServiceUser,
                                 final String uuid,
                                 final String parentFolderUUID) throws QueryApiException {
        return httpClient
                .target(moveUrl.apply(uuid, parentFolderUUID))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .put(Entity.json(""));
    }

    @Override
    public Response renameDocument(final ServiceUser authenticatedServiceUser,
                                  final String uuid,
                                  final String name) throws QueryApiException {
        return httpClient
                .target(renameUrl.apply(uuid, name))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .put(Entity.json(""));
    }

    @Override
    public Response deleteDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid) throws QueryApiException {
        return httpClient
                .target(deleteUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .delete();
    }

    @Override
    public Response importDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap) throws QueryApiException {
        return httpClient
                .target(importUrl.getUrl(uuid, name, confirmed))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(dataMap));
    }


    @Override
    public Response exportDocument(final ServiceUser authenticatedServiceUser,
                                   final String uuid) throws QueryApiException {
        return httpClient
                .target(exportUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }
}
