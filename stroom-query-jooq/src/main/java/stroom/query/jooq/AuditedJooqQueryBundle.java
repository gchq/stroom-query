package stroom.query.jooq;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.service.DocRefService;

import java.util.function.Function;

/**
 * This Dropwizard bundle can be used to build the entire Query Resource implementation stack when the data source is
 * an SQL database, storing one annotated data type. The annotated data type will need to also annotate it's fields with
 * {@link IsDataSourceField}
 * @param <CONFIG> The configuration class
 * @param <QUERY_POJO> The annotated class.
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedJooqQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig & HasDataSourceFactory & HasFlywayFactory & HasJooqFactory,
                DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>,
                DOC_REF_POJO extends DocRefJooqEntity,
                QUERY_POJO extends QueryableJooqEntity> implements ConfiguredBundle<CONFIG> {
    private final Class<QUERY_POJO> queryableEntityClass;

    private final AuditedJooqDocRefBundle<CONFIG,
            DOC_REF_SERVICE,
            DOC_REF_POJO,
            QueryServiceJooqImpl> auditedJooqDocRefBundle;

    public AuditedJooqQueryBundle(final Function<CONFIG, Injector> injectorSupplier,
                                  final Class<DOC_REF_SERVICE> docRefServiceClass,
                                  final Class<DOC_REF_POJO> docRefEntityClass,
                                  final Class<QUERY_POJO> queryableEntityClass) {
        this.queryableEntityClass = queryableEntityClass;
        this.auditedJooqDocRefBundle = new AuditedJooqDocRefBundle<>(injectorSupplier, docRefServiceClass, docRefEntityClass, QueryServiceJooqImpl.class);
    }

    public Module getGuiceModule(CONFIG configuration) {
        return Modules.combine(new AbstractModule() {
            @Override
            protected void configure() {
                bind(QueryableEntity.ClassProvider.class).toInstance(new QueryableEntity.ClassProvider<>(queryableEntityClass));
            }
        }, auditedJooqDocRefBundle.getGuiceModule(configuration));
    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        final Bootstrap<CONFIG> castBootstrap = (Bootstrap<CONFIG>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(auditedJooqDocRefBundle);
    }
}
