/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.dropwizard.testing.ConfigOverride;
import org.eclipse.jetty.http.HttpStatus;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.docref.DocRef;
import stroom.query.authorisation.DocumentPermission;
import stroom.query.security.ServiceUser;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This rule effectively builds an Authentication and Authorisation service for use in integration tests.
 * <p>
 * It provides methods for getting authenticated and unauthenticated users, it also gives tests the ability
 * to grant permissions to specific users.
 */
public class StroomAuthenticationExtension extends WireMockServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomAuthenticationExtension.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final String ADMIN_USER = UUID.randomUUID().toString();
    private final ConcurrentHashMap<String, ServiceUser> authenticatedUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ServiceUser> unauthenticatedUsers = new ConcurrentHashMap<>();
    private RsaJsonWebKey jwk;
    private RsaJsonWebKey invalidJwk;

    public StroomAuthenticationExtension() {
        super(WireMockConfiguration.options().dynamicPort());
    }

    /**
     * Create a Dropwizard config override that can inject the dynamic port into the test auth service URL
     *
     * @return The Config Override for the Auth Service Token URL
     */
    public ConfigOverride authToken() {
        beforeAll();

        return ConfigOverride.config("token.publicKeyUrl",
                () -> String.format("http://localhost:%d/testAuthService/publicKey", this.port()));
    }

    /**
     * Creates a Dropwizard config override that can inject the dynamic port into the test authorisation service URL
     *
     * @return The Config override for the authorisation service URL.
     */
    public ConfigOverride authService() {
        beforeAll();

        return ConfigOverride.config("authorisationService.url",
                () -> String.format("http://localhost:%d/api/authorisation/v1", this.port()));
    }

    void beforeAll() {
        if (!isRunning()) {
            start();
            before();
            WireMock.configureFor("localhost", port());
        }
    }

    void afterAll() {
        if (isRunning()) {
            after();
            client.resetMappings();
            stop();
        }
    }

    private void before() {
        LOGGER.info("Setting Up Stroom Auth with Wiremock");

        //
        // Setup Authentication Service
        //
        try {
            String jwkId = UUID.randomUUID().toString();
            jwk = RsaJwkGenerator.generateJwk(2048);
            jwk.setKeyId(jwkId);
        } catch (final JoseException e) {
            fail(e.getLocalizedMessage());
            return;
        }

        //
        // Setup another public key, which can be used to generate credentials which are the right format, but won't authenticate with our service
        //
        try {
            String jwkId = UUID.randomUUID().toString();
            invalidJwk = RsaJwkGenerator.generateJwk(2048);
            invalidJwk.setKeyId(jwkId);
        } catch (final JoseException e) {
            fail(e.getLocalizedMessage());
            return;
        }

        stubFor(get(urlEqualTo("/testAuthService/publicKey"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody(jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY))
                        .withStatus(200)));

        //
        // Setup Authorisation Service for Doc Refs, by default allow nothing
        // Specific sub classes will have to setup specific authorisations.
        //
        stubFor(post(urlEqualTo("/api/authorisation/v1/isAuthorised"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                        .withBody("Mock disapproval for authorisation")
                        .withStatus(HttpStatus.UNAUTHORIZED_401)));
    }

    private void after() {
        authenticatedUsers.clear();
        unauthenticatedUsers.clear();
    }

    /**
     * Function that gets the default admin user, this user will generally have permission to do everything
     *
     * @return The default admin ServiceUser, with an API key
     */
    public ServiceUser adminUser() {
        return serviceUser(ADMIN_USER, authenticatedUsers, jwk);
    }

    /**
     * Utility function to generate an authenticated user.
     *
     * @param username The usernane of the required user.
     * @return the user, with a JWK tied to the valid public key credentials
     */
    public ServiceUser authenticatedUser(final String username) {
        return serviceUser(username, authenticatedUsers, jwk);
    }

    /**
     * Utility function to generate an unauthenticated user.
     *
     * @param username The usernane of the required user.
     * @return the user, with a JWK tied to the wrong public key credentials
     */
    public ServiceUser unauthenticatedUser(final String username) {
        return serviceUser(username, unauthenticatedUsers, invalidJwk);
    }

    /**
     * Function that starts building permissions for the admin user
     *
     * @return A permission builder tied to the admin user
     */
    public PermissionBuilder permitAdminUser() {
        return new PermissionBuilder(serviceUser(ADMIN_USER, authenticatedUsers, jwk));
    }

    /**
     * Utility function to generate an authenticated user and start building permissions.
     *
     * @param username The usernane of the required user.
     * @return a permission builder tied to the named authenticated user
     */
    public PermissionBuilder permitAuthenticatedUser(final String username) {
        return new PermissionBuilder(serviceUser(username, authenticatedUsers, jwk));
    }

    /**
     * Request a user with a given name, if the user does not exist, a new API key is created for that user.
     * Allows for the creation of multiple users, which may have different levels of access to doc refs for testing
     * that authorisation works.
     *
     * @param username  The usernane of the required user.
     * @param usersMap  map of users to use, there are authenticated and unauthenticated users
     * @param publicKey The public key that is tied to the users map
     * @return A created ServiceUser, with an API Key
     */
    private ServiceUser serviceUser(final String username,
                                    final ConcurrentHashMap<String, ServiceUser> usersMap,
                                    final RsaJsonWebKey publicKey) {
        return usersMap.computeIfAbsent(username, u -> {
            ServiceUser serviceUser = null;

            JwtClaims claims = new JwtClaims();
            claims.setIssuer("stroom");  // who creates the token and signs it
            claims.setSubject(u); // the subject/principal is whom the token is about

            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            jws.setKey(publicKey.getPrivateKey());
            jws.setDoKeyValidation(false);

            try {
                serviceUser = new ServiceUser.Builder()
                        .jwt(jws.getCompactSerialization())
                        .name(u)
                        .build();
            } catch (final JoseException e) {
                fail(e.getLocalizedMessage());
            }

            return serviceUser;
        });
    }

    private void givePermission(final ServiceUser serviceUser,
                                final DocRef docRef,
                                final String permissionName) {
        /**
         * Setup Authorisation Service for Doc Refs, by default allow everything
         * Specific sub classes may have more specific cases to test for unauthorised actions.
         */

        final Map<String, Object> request = new HashMap<>();
        request.put("docRef", docRef);
        request.put("permission", permissionName);

        String requestJson;
        try {
            requestJson = jacksonObjectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            fail(e.getLocalizedMessage());
            return;
        }

        stubFor(post(urlEqualTo("/api/authorisation/v1/isAuthorised"))
                .withRequestBody(equalToJson(requestJson))
                .withHeader("Authorization", containing(serviceUser.getJwt()))
                .willReturn(ok("Mock approval for authorisation")));
    }

    public class PermissionBuilder {
        private final List<ServiceUser> serviceUsers = new ArrayList<>();
        private final List<DocRef> docRefs = new ArrayList<>();
        private final List<String> permissionNames = new ArrayList<>();

        private PermissionBuilder(final ServiceUser serviceUser) {
            this.serviceUsers.add(serviceUser);
        }

        public PermissionBuilder andAuthenticatedUser(final String username) {
            return andUser(StroomAuthenticationExtension.this.authenticatedUser(username));
        }

        public PermissionBuilder andUser(final ServiceUser serviceUser) {
            this.serviceUsers.add(serviceUser);
            return this;
        }

        public PermissionBuilder docRef(final DocRef docRef) {
            // The name may not be supplied, so let's put with AND without name into the list
            // The wiremock matcher will expect exact JSON, so we need both options to work
            if (null != docRef.getName()) {
                this.docRefs.add(new DocRef.Builder()
                        .uuid(docRef.getUuid())
                        .type(docRef.getType())
                        .build());
            }
            this.docRefs.add(docRef);
            return this;
        }

        public PermissionBuilder docRef(final String uuid, final String type) {
            return this.docRef(new DocRef.Builder()
                    .uuid(uuid)
                    .type(type)
                    .build());
        }

        public PermissionBuilder permission(final DocumentPermission permission) {
            return this.permissionName(permission.getName());
        }

        public PermissionBuilder permissionName(final String permissionName) {
            this.permissionNames.add(permissionName);
            return this;
        }

        public void done() {
            serviceUsers.forEach(serviceUser ->
                    docRefs.forEach(docRef ->
                            permissionNames.forEach(permissionName ->
                                    StroomAuthenticationExtension.this.givePermission(serviceUser, docRef, permissionName)
                            )
                    )
            );
        }
    }
}
