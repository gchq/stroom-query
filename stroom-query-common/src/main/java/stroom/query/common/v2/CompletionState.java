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

package stroom.query.common.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class CompletionState {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionState.class);

    private final AtomicBoolean complete = new AtomicBoolean();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final Queue<CompletionListener> completionListeners = new ConcurrentLinkedQueue<>();

    void complete() {
        complete.set(true);
        countDownLatch.countDown();

        // Notify the listeners
        notifyCompletionListeners();
    }

    public void awaitCompletion() throws InterruptedException {
        countDownLatch.await();
    }

    public void registerCompletionListener(final CompletionListener completionListener) {
        completionListeners.add(Objects.requireNonNull(completionListener));
        if (complete.get()) {
            notifyCompletionListeners();
        }
    }

    private void notifyCompletionListeners() {
        for (CompletionListener listener; (listener = completionListeners.poll()) != null; ) {
            // When notified they will check isComplete
            LOGGER.debug("Notifying {} {} that we are complete", listener.getClass().getName(), listener);
            listener.onCompletion();
        }
    }
}
