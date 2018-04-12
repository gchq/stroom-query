package stroom.query.audit.security;

import java.util.ArrayDeque;
import java.util.Deque;

public class SecurityContext {
    private static final ThreadLocal<Deque<ServiceUser>> THREAD_LOCAL = ThreadLocal.withInitial(ArrayDeque::new);

    public static void pushServiceUser(final ServiceUser userRef) {
        THREAD_LOCAL.get().push(userRef);
    }

    public static ServiceUser popServiceUser() {
        return THREAD_LOCAL.get().pop();
    }

    public static ServiceUser currentServiceUser() {
        return THREAD_LOCAL.get().peek();
    }
}
