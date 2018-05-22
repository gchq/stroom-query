package stroom.query.audit.service;

import java.util.Optional;
import java.util.function.Function;

/**
 * Interface that supplies instances of doc ref service given the 'type'
 */
public interface DocRefServiceSupplier extends Function<String, Optional<DocRefService>> {
}
