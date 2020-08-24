package stroom.query.common.v2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CompletionStateImpl implements CompletionState {
    private final AtomicBoolean complete = new AtomicBoolean();
    private final CountDownLatch completeLatch = new CountDownLatch(1);

    @Override
    public boolean isComplete() {
        return complete.get();
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return completeLatch.await(timeout, unit);
    }

    @Override
    public void complete() {
        if (complete.compareAndSet(false, true)) {
            completeLatch.countDown();
        }
    }
}
