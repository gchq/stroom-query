package stroom.query.hibernate;

import event.logging.EventLoggingService;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hibernate.SessionFactory;
import stroom.query.audit.AuditedQueryResourceImpl;
import stroom.query.audit.QueryEventLoggingService;
import stroom.query.audit.QueryResource;

/**
 * This Dropwizard bundle can be used to build the entire Query Resource implementation stack when the data source is
 * an SQL database, storing one annotated data type. The annotated data type will need to also annotate it's fields with
 * {@link IsDataSourceField}
 * @param <C> The configuration class
 * @param <Q> The hibernate annotated class.
 */
public class AuditedCriteriaQueryBundle<C extends Configuration, Q extends QueryableEntity> implements ConfiguredBundle<C> {

    private final Class<Q> queryableEntityClass;

    private final HibernateBundle<C> hibernateBundle;

    public AuditedCriteriaQueryBundle(final Class<Q> queryableEntityClass, final HibernateBundle<C> hibernateBundle) {
        this.queryableEntityClass = queryableEntityClass;

        this.hibernateBundle = hibernateBundle;
    }

    @Override
    public void run(C configuration, Environment environment) throws Exception {

        environment.jersey().register(AuditedQueryResourceImpl.class);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(QueryEventLoggingService.class).to(EventLoggingService.class);

                final QueryResource queryResource =
                        new QueryResourceCriteriaImpl<>(AuditedCriteriaQueryBundle.this.queryableEntityClass, hibernateBundle.getSessionFactory());

                bind(queryResource).to(QueryResource.class);
                bind(hibernateBundle.getSessionFactory()).to(SessionFactory.class);
            }
        });

    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        final Bootstrap<C> castBootstrap = (Bootstrap<C>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(hibernateBundle);
    }
}
