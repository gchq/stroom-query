package stroom.query.hibernate;

import event.logging.EventLoggingService;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.query.audit.AuditedQueryResourceImpl;
import stroom.query.audit.QueryEventLoggingService;
import stroom.query.audit.QueryResource;

/**
 * This Dropwizard bundle can be used to build the entire Query Resource implementation stack when the data source is
 * an SQL database, storing one annotated data type. The annotated data type will need to also annotate it's fields with
 * {@link IsDataSourceField}
 * @param <C> The configuration class
 * @param <T> The hibernate annotated class.
 */
public abstract class AuditedCriteriaQueryBundle<C extends Configuration, T> implements ConfiguredBundle<C> {

    protected abstract DataSourceFactory getDataSourceFactory(C configuration);

    private final Class<T> dtoClass;

    private final HibernateBundle<C> hibernateBundle;

    protected AuditedCriteriaQueryBundle(final Class<T> dtoClass) {
        this.dtoClass = dtoClass;

        hibernateBundle = new HibernateBundle<C>(dtoClass) {
            @Override
            public DataSourceFactory getDataSourceFactory(C configuration) {
                return AuditedCriteriaQueryBundle.this.getDataSourceFactory(configuration);
            }
        };
    }

    @Override
    public void run(C configuration, Environment environment) throws Exception {

        environment.jersey().register(AuditedQueryResourceImpl.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(QueryEventLoggingService.class).to(EventLoggingService.class);

                final QueryResource queryResource =
                        new QueryResourceCriteriaImpl<>(AuditedCriteriaQueryBundle.this.dtoClass, hibernateBundle.getSessionFactory());

                bind(queryResource).to(QueryResource.class);
            }
        });

    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        final Bootstrap<C> castBootstrap = (Bootstrap<C>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(hibernateBundle);
    }
}
