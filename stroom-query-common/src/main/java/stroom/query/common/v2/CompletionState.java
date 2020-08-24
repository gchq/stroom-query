package stroom.query.common.v2;

import java.util.concurrent.TimeUnit;

public interface CompletionState {
    boolean isComplete();

    boolean await(long timeout, TimeUnit unit) throws InterruptedException;

    void complete();
}
