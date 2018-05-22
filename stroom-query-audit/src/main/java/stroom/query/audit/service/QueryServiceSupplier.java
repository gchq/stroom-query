package stroom.query.audit.service;

import java.util.Optional;
import java.util.function.Function;

/**
 * Interface that supplies instances of query service given the 'type'
 */
public interface QueryServiceSupplier extends Function<String, Optional<QueryService>>  {
}
