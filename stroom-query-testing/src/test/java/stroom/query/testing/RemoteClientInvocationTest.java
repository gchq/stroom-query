package stroom.query.testing;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.service.QueryApiException;
import stroom.query.audit.service.QueryService;
import stroom.query.audit.service.QueryServiceSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class RemoteClientInvocationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteClientInvocationTest.class);


    @Test
    void test() {
        final Map<String, String> urlsByType = new HashMap<>();
        urlsByType.put(UUID.randomUUID().toString(), "http://localhost:28080/");
        urlsByType.put(UUID.randomUUID().toString(), "http://localhost:18080/");
        Injector injector = Guice.createInjector(new RemoteClientTestingModule(urlsByType));

        QueryServiceSupplier remoteClientCache = injector.getInstance(QueryServiceSupplier.class);

        urlsByType.forEach((k, v) -> {
            QueryService q = remoteClientCache.apply(k)
                    .orElseThrow(() -> new RuntimeException("Nope"));
            LOGGER.info("Calling Service {}", q);
            try {
                q.search(null, null);
            } catch (Exception e) {
                LOGGER.info("probably expected exception {}", e.getLocalizedMessage());
            }
        });

        urlsByType.forEach((k, v) -> {
            QueryService q = remoteClientCache.apply(k)
                    .orElseThrow(() -> new RuntimeException("Nope"));
            LOGGER.info("Verifying Service {}", q);
            try {
                verify(q).search(any(), any());
            } catch (QueryApiException e) {
                LOGGER.info("probably expected exception {}", e.getLocalizedMessage());
            }
        });
    }
}
