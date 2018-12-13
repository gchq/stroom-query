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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class CurrentServiceUserTest {
    @Test
    void testMultipleThreadsGivenDistinctUsers() {
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

                    assertThat(cUser).isEqualTo(user);
                    assertThat(pUser).isEqualTo(user);
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

        assertThat(users).hasSize(3);
    }
}
