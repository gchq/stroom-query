package stroom.query.audit.client;

import stroom.query.audit.rest.DocRefResource;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DocRefResourceHttpClient<T extends DocRefEntity> implements DocRefResource, Closeable {

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
        httpClient = ClientBuilder.newClient();
    }

    public void close() {
        if (null != httpClient) {
            this.httpClient.close();
        }
    }

    @Override
    public Response getAll(final ServiceUser user){
        return httpClient
                .target(getAllUrl)
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .get();
    }

    @Override
    public Response get(final ServiceUser user,
                        final String uuid){
        return httpClient
                .target(getUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .get();
    }

    @Override
    public Response getInfo(final ServiceUser user,
                            final String uuid){
        return httpClient
                .target(getInfoUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .get();
    }

    @Override
    public Response createDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name,
                                   final String parentFolderUUID){
        return httpClient
                .target(createUrl.getUrl(uuid, name, parentFolderUUID))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(""));
    }

    public Response update(final ServiceUser user,
                           final String uuid,
                           final T updatedConfig){
        return httpClient
                .target(this.updateUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .put(Entity.json(updatedConfig));
    }

    @Override
    public Response update(final ServiceUser user,
                           final String uuid,
                           final String updatedConfig){
        return httpClient
                .target(this.updateUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .put(Entity.json(updatedConfig));
    }

    @Override
    public Response copyDocument(final ServiceUser user,
                                 final String originalUuid,
                                 final String copyUuid,
                                 final String parentFolderUUID){
        return httpClient
                .target(copyUrl.getUrl(originalUuid, copyUuid, parentFolderUUID))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(""));
    }

    @Override
    public Response moveDocument(final ServiceUser user,
                                 final String uuid,
                                 final String parentFolderUUID){
        return httpClient
                .target(moveUrl.apply(uuid, parentFolderUUID))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .put(Entity.json(""));
    }

    @Override
    public Response renameDocument(final ServiceUser user,
                                  final String uuid,
                                  final String name){
        return httpClient
                .target(renameUrl.apply(uuid, name))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .put(Entity.json(""));
    }

    @Override
    public Response deleteDocument(final ServiceUser user,
                                   final String uuid){
        return httpClient
                .target(deleteUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .delete();
    }

    @Override
    public Response importDocument(final ServiceUser user,
                                   final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap){
        return httpClient
                .target(importUrl.getUrl(uuid, name, confirmed))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .post(Entity.json(dataMap));
    }


    @Override
    public Response exportDocument(final ServiceUser user,
                                   final String uuid){
        return httpClient
                .target(exportUrl.apply(uuid))
                .request()
                .header("Authorization", "Bearer " + user.getJwt())
                .get();
    }
}
