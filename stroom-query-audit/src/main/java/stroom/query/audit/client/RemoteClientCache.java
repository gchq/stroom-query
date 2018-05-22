package stroom.query.audit.client;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RemoteClientCache<T> implements Function<String, Optional<T>> {
    private final BiFunction<String, String, T> supplier;
    private final Function<String, String> getUrlsByType;
    private final ConcurrentHashMap<String, T> cache =
            new ConcurrentHashMap<>();

    public RemoteClientCache(final Function<String, String> getUrlsByType,
                             final BiFunction<String, String, T> supplier) {
        this.getUrlsByType = getUrlsByType;
        this.supplier = supplier;
    }

    @Override
    public Optional<T> apply(final String type) {
        return Optional.ofNullable(getUrlsByType.apply(type))
                .map(url -> cache.computeIfAbsent(url, (u) -> supplier.apply(type, u)));
    }
}
