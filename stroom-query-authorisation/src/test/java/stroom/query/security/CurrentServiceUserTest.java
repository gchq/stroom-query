/*
 * Copyright 2018 Crown Copyright
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

package stroom.query.security;

import org.junit.Test;
import stroom.query.security.CurrentServiceUser;
import stroom.query.security.ServiceUser;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CurrentServiceUserTest {
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
                    CurrentServiceUser.pushServiceUser(user);

                    try {
                        Thread.sleep(500);
                        cUser = CurrentServiceUser.currentServiceUser();
                        Thread.sleep(500);
                    } catch (final InterruptedException e) {
                        fail(e.getLocalizedMessage());
                    }

                    final ServiceUser pUser = CurrentServiceUser.popServiceUser();

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
