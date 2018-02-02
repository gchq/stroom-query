/*
 *
 *   Copyright 2017 Crown Copyright
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package stroom.query.audit.authorisation;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.security.ServiceUser;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class AuthorisationServiceImpl implements AuthorisationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorisationServiceImpl.class);

    private final Client httpClient;
    private final String isAuthorisedUrl;

    @Inject
    public AuthorisationServiceImpl(final AuthorisationServiceConfig config) {
        this.httpClient = ClientBuilder.newClient(new ClientConfig().register(ClientResponse.class));
        this.isAuthorisedUrl = config.getIsAuthorisedUrl();
    }

    @Override
    public boolean isAuthorised(final ServiceUser serviceUser,
                                final DocRef docRef,
                                final String permissionName) {
        boolean isUserAuthorised;

        Response response = null;
        try {
            final Map<String, Object> request = new HashMap<>();
            request.put("docRef", new DocRef.Builder()
                    .uuid(docRef.getUuid())
                    .type(docRef.getType())
                    .build()); // stripping out the name
            request.put("permission", permissionName);

            response = httpClient
                    .target(this.isAuthorisedUrl)
                    .request()
                    .header("Authorization", "Bearer " + serviceUser.getJwt())
                    .post(Entity.json(request));

            switch (response.getStatus()) {
                case HttpStatus.UNAUTHORIZED_401:
                    isUserAuthorised = false;
                    break;
                case HttpStatus.OK_200:
                    isUserAuthorised = true;
                    break;
                case HttpStatus.NOT_FOUND_404:
                    isUserAuthorised = false;
                    LOGGER.error("Received a 404 when trying to access the authorisation service! I am unable to check authorisation so all requests will be rejected until this is fixed. Is the service location correctly configured? Is the service running? The URL I tried was: {}", this.isAuthorisedUrl);
                    break;
                default:
                    isUserAuthorised = false;
                    LOGGER.error("Tried to check authorisation for a user but got an unknown response!",
                            response.getStatus());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not request authorisation " + e.getLocalizedMessage());
            isUserAuthorised = false;
        } finally {
            if (null != response) {
                response.close();
            }
        }

        return isUserAuthorised;
    }
}
