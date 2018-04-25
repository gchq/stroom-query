package stroom.query.audit.rest;

import java.util.Optional;
import java.util.function.Function;

public interface DocRefResourceSupplier extends Function<String, Optional<DocRefResource>> {
}
