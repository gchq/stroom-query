package stroom.query.audit.security;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Allows the lazy loading of the public key. This is required because the order of services being stood up
 * cannot be guaranteed.
 */
public class RobustJwtAuthFilter implements ContainerRequestFilter {

    private final TokenConfig tokenConfig;

    private JwtAuthFilter<ServiceUser> jwtAuthFilter;

    private final Client httpClient;

    public RobustJwtAuthFilter(final TokenConfig tokenConfig) {
        this.tokenConfig = tokenConfig;
        this.httpClient = ClientBuilder.newClient(new ClientConfig().register(ClientResponse.class));
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (null == jwtAuthFilter) {
            init();
        }

        jwtAuthFilter.filter(requestContext);
    }

    private void init() {
        final Response response = httpClient
                .target(this.tokenConfig.getPublicKeyUrl())
                .request()
                .header("accept", MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .get();

        final String pkJson = response.readEntity(String.class);

        PublicJsonWebKey jwk;
        try {
            jwk = RsaJsonWebKey.Factory.newPublicJwk(pkJson);
        } catch (JoseException e) {
            throw new RuntimeException("Could not decode public key: " + e.getLocalizedMessage());
        }

        final JwtConsumerBuilder builder = new JwtConsumerBuilder()
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setVerificationKey(jwk.getPublicKey()) // verify the signature with the public key
                .setJwsAlgorithmConstraints( // only allow the expected signature algorithm(s) in the given context
                        new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, // which is only RS256 here
                                tokenConfig.getAlgorithm()))
                .setRelaxVerificationKeyValidation() // relaxes key length requirement
                .setExpectedIssuer(this.tokenConfig.getJwsIssuer());

        final JwtConsumer jwtConsumer = builder.build();

        this.jwtAuthFilter = new JwtAuthFilter.Builder<ServiceUser>()
                .setJwtConsumer(jwtConsumer)
                .setRealm("realm")
                .setPrefix("Bearer")
                .setAuthenticator(new UserAuthenticator())
                .buildAuthFilter();
    }
}
