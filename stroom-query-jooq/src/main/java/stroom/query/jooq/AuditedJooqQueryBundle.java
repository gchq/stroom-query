package stroom.query.jooq;

import com.bendb.dropwizard.jooq.JooqBundle;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.query.audit.AuditedQueryBundle;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.service.DocRefService;

public class AuditedJooqQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        QUERY_POJO extends QueryableJooqEntity,
        DOC_REF_POJO extends DocRefJooqEntity,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>>
        extends AuditedQueryBundle<CONFIG,
        DOC_REF_POJO,
        QueryServiceJooqImpl,
        DOC_REF_SERVICE> {
    private final Class<QUERY_POJO> queryableEntityClass;

    private final JooqBundle<CONFIG> jooqBundle;

    public AuditedJooqQueryBundle(final Class<QUERY_POJO> queryableEntityClass,
                                  final JooqBundle<CONFIG> jooqBundle,
                                  final Class<DOC_REF_POJO> docRefEntityClass,
                                  final Class<DOC_REF_SERVICE> docRefServiceClass) {
        super(docRefEntityClass,
                QueryServiceJooqImpl.class,
                docRefServiceClass);

        this.queryableEntityClass = queryableEntityClass;
        this.jooqBundle = jooqBundle;
    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
        super.run(configuration, environment);

        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new QueryableEntity.ClassProvider<>(queryableEntityClass)).to(QueryableEntity.ClassProvider.class);
            }
        });
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        super.initialize(bootstrap);

        final Bootstrap<CONFIG> castBootstrap = (Bootstrap<CONFIG>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(jooqBundle);
    }
}
