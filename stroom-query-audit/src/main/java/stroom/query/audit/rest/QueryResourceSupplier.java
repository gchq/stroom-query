package stroom.query.audit.rest;

import java.util.Optional;
import java.util.function.Function;

public interface QueryResourceSupplier extends Function<String, Optional<QueryResource>> {
}
