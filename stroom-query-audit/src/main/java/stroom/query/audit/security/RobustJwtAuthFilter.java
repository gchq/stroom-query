package stroom.query.audit.security;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import stroom.query.audit.client.SimpleJsonHttpClient;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Allows the lazy loading of the public key. This is required because the order of services being stood up
 * cannot be guaranteed.
 */
public class RobustJwtAuthFilter implements ContainerRequestFilter {

    private final String jwsIssuer;

    private final String algorithm;

    private final String publicKeyUrl;

    private JwtAuthFilter<ServiceUser> jwtAuthFilter;

    private SimpleJsonHttpClient<IOException> httpClient;

    public RobustJwtAuthFilter(final String jwsIssuer,
                               final String algorithm,
                               final String publicKeyUrl) {
        this.jwsIssuer = jwsIssuer;
        this.algorithm = algorithm;
        this.publicKeyUrl = publicKeyUrl;
        this.httpClient = new SimpleJsonHttpClient<>(IOException::new);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (null == jwtAuthFilter) {
            init();
        }

        jwtAuthFilter.filter(requestContext);
    }

    private void init() throws IOException {
        final Response response = this.httpClient.get(this.publicKeyUrl).send();

        final String pkJson = response.getEntity().toString();

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
                                AlgorithmIdentifiers.RSA_USING_SHA256))
                .setRelaxVerificationKeyValidation() // relaxes key length requirement
                .setExpectedIssuer(this.jwsIssuer);

        final JwtConsumer jwtConsumer = builder.build();

        this.jwtAuthFilter = new JwtAuthFilter.Builder<ServiceUser>()
                .setJwtConsumer(jwtConsumer)
                .setRealm("realm")
                .setPrefix("Bearer")
                .setAuthenticator(new UserAuthenticator())
                .buildAuthFilter();
    }
}
