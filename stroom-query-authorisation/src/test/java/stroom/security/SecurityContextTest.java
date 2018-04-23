package stroom.security;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SecurityContextTest {
    @Test
    public void testMultipleThreadsGivenDistinctUsers() {
        // Given
        final int n = 3;
        final Set<ServiceUser> users = Collections.synchronizedSet(new HashSet<>());
        final ExecutorService executorService = Executors.newFixedThreadPool(n);

        final List<Future> futures = IntStream.range(0, n)
                .mapToObj((Runnable) -> (java.lang.Runnable) () -> {
                    final ServiceUser user = new ServiceUser.Builder()
                            .name(UUID.randomUUID().toString())
                            .jwt(UUID.randomUUID().toString())
                            .build();
                    ServiceUser cUser = null;
                    SecurityContext.pushServiceUser(user);

                    try {
                        Thread.sleep(500);
                        cUser = SecurityContext.currentServiceUser();
                        Thread.sleep(500);
                    } catch (final InterruptedException e) {
                        fail(e.getLocalizedMessage());
                    }

                    final ServiceUser pUser = SecurityContext.popServiceUser();

                    assertEquals(user, cUser);
                    assertEquals(user, pUser);
                    users.add(user);
                })
                .map(executorService::submit)
                .collect(Collectors.toList());

        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                fail(e.getLocalizedMessage());
            }
        });

        assertEquals(3, users.size());
    }
}
