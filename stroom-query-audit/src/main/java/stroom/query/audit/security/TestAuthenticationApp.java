package stroom.query.audit.security;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import stroom.query.audit.client.SimpleJsonHttpClient;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A minimal test application to serve up a public key for test purposes.
 */
public class TestAuthenticationApp extends Application<TestAuthenticationApp.AuthConfig> {
    @Override
    public void run(final AuthConfig configuration,
                    final Environment environment) {
        environment.healthChecks().register("Up", new HealthCheck() {
            @Override
            protected Result check() {
                return (AuthResourceImpl.jwk != null) ?
                        Result.healthy("Json Web Key is available") :
                        Result.unhealthy("Json Web Key not available");
            }
        });
        environment.jersey().register(AuthResourceImpl.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(configuration).to(AuthConfig.class);
            }
        });
    }

    public static class AuthConfig extends Configuration {
        @Valid
        @NotNull
        @JsonProperty
        private String jwsIssuer = "stroom";

        @Valid
        @NotNull
        @JsonProperty
        private String algorithm;

        public String getJwsIssuer() {
            return jwsIssuer;
        }

        public void setJwsIssuer(String jwsIssuer) {
            this.jwsIssuer = jwsIssuer;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    /**
     * A rest interface that allows a test to get a valid token.
     * Also exposes a randomly generated public key for validation purposes
     */
    @Path("/testAuthService")
    @Produces(MediaType.APPLICATION_JSON)
    public interface AuthResource {

        /**
         * Returns the public key as JSON, replicates the structure of the full stroom authentication service.
         * @return The Json Web Key (JWK), public key only
         * @throws Exception If something goes wrong.
         */
        @GET
        @Path("/publicKey")
        Response getPublicKey() throws Exception;

        /**
         * Generates a new token that can be used by tests to access services that use
         * the co-located public key for verification.
         * @return A new token
         * @throws Exception If something goes wrong.
         */
        @GET
        @Path("/token")
        Response getToken() throws Exception;
    }

    public static Client client(final String host, final int port) {
        final String baseUrl = String.format("http://%s:%d", host, port);
        return new Client(baseUrl);
    }

    /**
     * A client implementation for using the Test Auth Resource
     */
    public static class Client implements AuthResource {

        private final String baseUrl;
        private String getTokenUrl;
        private String getPublicKeyUrl;
        private final SimpleJsonHttpClient<Exception> httpClient;

        public Client(final String baseUrl) {
            this.baseUrl = baseUrl;

            this.getTokenUrl = String.format("%s/testAuthService/token", this.baseUrl);
            this.getPublicKeyUrl = String.format("%s/testAuthService/publicKey", this.baseUrl);
            this.httpClient = new SimpleJsonHttpClient<>(Exception::new);
        }

        public ServiceUser getAuthenticatedUser() throws Exception {
            final Response loginResponse = this.getToken();
            final String jwtToken = loginResponse.getEntity().toString();
            return new ServiceUser.Builder()
                    .jwt(jwtToken)
                    .name("testSubject")
                    .build();
        }

        @Override
        public Response getPublicKey() throws Exception {
            return this.httpClient
                    .get(this.getPublicKeyUrl)
                    .send();
        }

        @Override
        public Response getToken() throws Exception {
            return this.httpClient
                    .get(this.getTokenUrl)
                    .send();
        }
    }

    /**
     * The implementation of the auth resource
     */
    public static class AuthResourceImpl implements AuthResource {

        /**
         * All instances of the resource need to use the same key.
         */
        private static RsaJsonWebKey jwk;

        static {
            try {
                String jwkId = UUID.randomUUID().toString();
                jwk = RsaJwkGenerator.generateJwk(2048);
                jwk.setKeyId(jwkId);
            } catch (Exception e) {

            }
        }

        private AuthConfig config;

        @Inject
        public AuthResourceImpl(AuthConfig config) {
            this.config = config;
        }

        public Response getPublicKey() {
            if (null != jwk) {
                return Response.ok(jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY)).build();
            } else {
                return Response.serverError().build();
            }
        }

        public Response getToken() {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(this.config.getJwsIssuer());  // who creates the token and signs it
            claims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
            claims.setGeneratedJwtId(); // a unique identifier for the token
            claims.setIssuedAtToNow();  // when the token was issued/created (now)
            claims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
            claims.setSubject("testSubject"); // the subject/principal is whom the token is about
            claims.setClaim("email","mail@example.com"); // additional claims/attributes about the subject can be added
            List<String> groups = Arrays.asList("group-one", "other-group", "group-three");
            claims.setStringListClaim("groups", groups); // multi-valued claims work too and will end up as a JSON array

            final JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setAlgorithmHeaderValue(this.config.getAlgorithm());
            jws.setKey(jwk.getPrivateKey());
            jws.setDoKeyValidation(false);

            try {
                return Response.ok(jws.getCompactSerialization()).build();
            }
            catch (JoseException e) {
                return Response.serverError().build();
            }
        }
    }

}
