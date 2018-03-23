package stroom.query.hibernate;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.AuditedQueryBundle;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.service.DocRefService;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This Dropwizard bundle can be used to build the entire Query Resource implementation stack when the data source is
 * an SQL database, storing one annotated data type. The annotated data type will need to also annotate it's fields with
 * {@link IsDataSourceField}
 * @param <CONFIG> The configuration class
 * @param <QUERY_POJO> The hibernate annotated class.
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedCriteriaQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig & HasDataSourceFactory & HasFlywayFactory,
                DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>,
                DOC_REF_POJO extends DocRefHibernateEntity,
                QUERY_POJO extends QueryableHibernateEntity> implements ConfiguredBundle<CONFIG> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditedCriteriaQueryBundle.class);

    // Wrap the flyway bundle so that we can call migrate in the bundles 'run'.
    // This allows the flyway migration to happen before the hibernate validation
    private final ConfiguredBundle<CONFIG> flywayBundle = new ConfiguredBundle<CONFIG>() {

        private final FlywayBundle<CONFIG> wrappedBundle = new FlywayBundle<CONFIG>() {
            public DataSourceFactory getDataSourceFactory(CONFIG config) {
                return config.getDataSourceFactory();
            }

            public FlywayFactory getFlywayFactory(final CONFIG config) {
                return config.getFlywayFactory();
            }
        };

        @Override
        public void run(final CONFIG configuration, final Environment environment) throws Exception {

            // We need the database before we need most other things
            migrate(configuration, environment);
        }

        private void migrate(CONFIG config, Environment environment) {
            ManagedDataSource dataSource = config.getDataSourceFactory().build(environment.metrics(), "flywayDataSource");
            Flyway flyway = config.getFlywayFactory().build(dataSource);
            // We want to be resilient against the database not being available, so we'll keep trying to migrate if there's
            // an exception. This approach blocks the startup of th/home/gchq-11/git/stroom-query/stroom-query-testing/src/integration-test/resources/hibernate/config.ymle service until the database is available. The downside
            // of this is that the admin pages won't be available - any future dashboarding that wants to emit information
            // about the missing database won't be able to do so. The upside of this approach is that it's very simple
            // to implement from where we are now, i.e. we don't need to add service-wide code to handle a missing database
            // e.g. in JwkDao.init().
            boolean migrationComplete = false;
            int databaseRetryDelayMs = 5000;
            while(!migrationComplete){
                try {
                    flyway.migrate();
                    migrationComplete = true;
                } catch(FlywayException flywayException){
                    LOGGER.error("Unable to migrate database! Will retry in {}ms.", databaseRetryDelayMs);
                    try {
                        Thread.sleep(databaseRetryDelayMs);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {
            bootstrap.addBundle(wrappedBundle);
        }

    };

    private final Class<QUERY_POJO> queryableEntityClass;

    private final HibernateBundle<CONFIG> hibernateBundle;

    private final AuditedQueryBundle<CONFIG,
            DOC_REF_SERVICE,
            DOC_REF_POJO,
            QueryServiceCriteriaImpl> auditedQueryBundle;

    public AuditedCriteriaQueryBundle(final Function<CONFIG, Injector> injectorSupplier,
                                      final Class<DOC_REF_SERVICE> docRefServiceClass,
                                      final Class<DOC_REF_POJO> docRefEntityClass,
                                      final Class<QUERY_POJO> queryableEntityClass,
                                      final Class<?> ... additionalHibernateClasses) {
        auditedQueryBundle = new AuditedQueryBundle<>(injectorSupplier, docRefServiceClass, docRefEntityClass, QueryServiceCriteriaImpl.class);

        // Put the doc ref class and additional classes into an array
        final Class<?>[] hibernateClasses = Stream.concat(
                Stream.of(docRefEntityClass),
                Stream.of(additionalHibernateClasses)
        ).toArray(Class<?>[]::new);

        this.queryableEntityClass = queryableEntityClass;
        this.hibernateBundle = new HibernateBundle<CONFIG>(queryableEntityClass, hibernateClasses) {
            @Override
            public DataSourceFactory getDataSourceFactory(CONFIG configuration) {
                return configuration.getDataSourceFactory();
            }
        };
    }

    public Module getGuiceModule(CONFIG configuration) {
        return Modules.combine(new AbstractModule() {
            @Override
            protected void configure() {
                bind(QueryableHibernateEntity.ClassProvider.class).toInstance(new QueryableEntity.ClassProvider<>(queryableEntityClass));
                bind(SessionFactory.class).toInstance(hibernateBundle.getSessionFactory());
            }
        }, auditedQueryBundle.getGuiceModule(configuration));
    }

    @Override
    public void run(CONFIG configuration, Environment environment) throws Exception {

    }

    public void initialize(final Bootstrap<?> bootstrap) {
        final Bootstrap<CONFIG> castBootstrap = (Bootstrap<CONFIG>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(flywayBundle);
        castBootstrap.addBundle(hibernateBundle);
        castBootstrap.addBundle(auditedQueryBundle);
    }
}
