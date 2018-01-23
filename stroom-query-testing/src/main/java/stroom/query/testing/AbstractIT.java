package stroom.query.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.http.HttpStatus;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.logback.FifoLogbackAppender;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefEntity;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Generic form of an integration test for a Dropwizard App that uses the authentication and authorisation services.
 * Provides mocked out version of both services, and functions for getting new users.
 *
 * @param <CONFIG_CLASS> The Dropwizard Configuration class
 * @param <APP_CLASS> The Dropwizard Application class, which is tied to the CONFIG_CLASS
 */
public abstract class AbstractIT<DOC_REF_ENTITY extends DocRefEntity,
        CONFIG_CLASS extends Configuration,
        APP_CLASS extends Application<CONFIG_CLASS>> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);

    private final Class<APP_CLASS> appClass;
    private final Class<DOC_REF_ENTITY> docRefEntityClass;
    private String appHost;
    private final String docRefType;

    protected AbstractIT(final Class<APP_CLASS> appClass,
                         final Class<DOC_REF_ENTITY> docRefEntityClass,
                         final String docRefType) {
        this.appClass = appClass;
        this.appRule =
                new DropwizardAppRule<>(this.appClass, resourceFilePath("config.yml"));

        this.docRefType = docRefType;
        this.docRefEntityClass = docRefEntityClass;
    }

    protected void checkAuditLogs(final int expected) {
        final List<Object> records = FifoLogbackAppender.popLogs();

        LOGGER.info(String.format("Expected %d records, received %d", expected, records.size()));

        assertEquals(expected, records.size());
    }

    protected Class<APP_CLASS> getAppClass() {
        return appClass;
    }

    protected Class<DOC_REF_ENTITY> getDocRefEntityClass() {
        return docRefEntityClass;
    }

    protected String getDocRefType() {
        return docRefType;
    }

    protected final DocRef getDocRef(final DOC_REF_ENTITY elasticIndexConfig) {
        return new DocRef.Builder()
                .uuid(elasticIndexConfig.getUuid())
                .type(this.docRefType)
                .build();
    }

    @Rule
    public final DropwizardAppRule<CONFIG_CLASS> appRule;

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(
            WireMockConfiguration.options().port(10080));

    protected static final String ADMIN_USER = "testAdminUser";
    protected static final String LOCALHOST = "localhost";

    private static RsaJsonWebKey jwk;
    private static final ConcurrentHashMap<String, ServiceUser> authenticatedUsers = new ConcurrentHashMap<>();

    private static RsaJsonWebKey invalidJwk;
    private static final ConcurrentHashMap<String, ServiceUser> unauthenticatedUsers = new ConcurrentHashMap<>();

    protected static final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
            = new com.fasterxml.jackson.databind.ObjectMapper();

    @BeforeClass
    public static void beforeAbstractClass() {

        //
        // This class will likely be extended multiple times, so clear our any users from previous runs.
        //
        authenticatedUsers.clear();
        unauthenticatedUsers.clear();

        //
        // Setup Authentication Service
        //
        try {
            String jwkId = UUID.randomUUID().toString();
            jwk = RsaJwkGenerator.generateJwk(2048);
            jwk.setKeyId(jwkId);
        } catch (Exception e) {
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
        } catch (Exception e) {
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

    /**
     * Function that gets the default admin user, this user will generally have permission to do everything
     * @return The default admin ServiceUser, with an API key
     */
    protected static ServiceUser adminUser() {
        return serviceUser(ADMIN_USER, authenticatedUsers, jwk);
    }

    /**
     * Utility function to generate an authenticated user.
     * @param username The usernane of the required user.
     * @return the user, with a JWK tied to the valid public key credentials
     */
    protected static ServiceUser authenticatedUser(final String username) {
        return serviceUser(username, authenticatedUsers, jwk);
    }

    /**
     * Utility function to generate an unauthenticated user.
     * @param username The usernane of the required user.
     * @return the user, with a JWK tied to the wrong public key credentials
     */
    protected static ServiceUser unauthenticatedUser(final String username) {
        return serviceUser(username, unauthenticatedUsers, invalidJwk);
    }

    /**
     * Request a user with a given name, if the user does not exist, a new API key is created for that user.
     * Allows for the creation of multiple users, which may have different levels of access to doc refs for testing
     * that authorisation works.
     * @param username The usernane of the required user.
     * @param usersMap map of users to use, there are authenticated and unauthenticated users
     * @param publicKey The public key that is tied to the users map
     * @return A created ServiceUser, with an API Key
     */
    private static ServiceUser serviceUser(final String username,
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
            } catch (JoseException e) {
                fail(e.getLocalizedMessage());
            }

            return serviceUser;
        });
    }

    protected void giveFolderCreatePermission(final ServiceUser serviceUser,
                                              final String folderUuid) {

        givePermission(serviceUser, new DocRef.Builder()
                        .type(DocumentPermission.FOLDER)
                        .uuid(folderUuid)
                        .build(),
                DocumentPermission.CREATE.getTypedPermission(this.docRefType));
    }

    protected void giveDocumentPermission(final ServiceUser serviceUser,
                                          final String uuid,
                                          final DocumentPermission permission) {
        givePermission(serviceUser, new DocRef.Builder()
                        .type(this.docRefType)
                        .uuid(uuid)
                        .build(),
                permission.getName());
    }

    protected void givePermission(final ServiceUser serviceUser,
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

    /**
     * Used to get the root URL of the dropwizard app, will be required by HTTP Client libraries.
     * @return The full URL of the root of the dropwizard app. (http://hostname:port)
     */
    protected String getAppHost() {
        return appHost;
    }

    protected <T> T getFromBody(final Response response, Class<T> theClass) {
        try {
            return jacksonObjectMapper.readValue(response.readEntity(String.class), theClass);
        } catch (IOException e) {
            fail(e.getLocalizedMessage());
            return null;
        }

    }

    protected DOC_REF_ENTITY getEntityFromBody(final Response response) {
        return getFromBody(response, getDocRefEntityClass());
    }

    @Before
    public final void beforeAbstractTest() {
        final int appPort = appRule.getLocalPort();
        appHost = String.format("http://%s:%d", LOCALHOST, appPort);
        FifoLogbackAppender.popLogs();
    }
}
