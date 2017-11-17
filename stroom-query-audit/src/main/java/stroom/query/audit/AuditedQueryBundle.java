package stroom.query.audit;

import event.logging.EventLoggingService;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * This Dropwizard bundle can be used to register an implementation of Query Resource implemenation
 * This bundle will wrap that implementation in an audited layer, any requests made to your Query Resource
 * will be passed to the {@link QueryEventLoggingService}
 *
 * @param <Q> Implementation class for the Query Resource
 */
public class AuditedQueryBundle<Q extends QueryResource> implements Bundle {

    private final Class<Q> queryResourceClass;

    public AuditedQueryBundle(final Class<Q> queryResourceClass) {
        this.queryResourceClass = queryResourceClass;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(final Environment environment) {
        environment.jersey().register(AuditedQueryResourceImpl.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(QueryEventLoggingService.class).to(EventLoggingService.class);
                bind(queryResourceClass).to(QueryResource.class);
            }
        });
    }
}
