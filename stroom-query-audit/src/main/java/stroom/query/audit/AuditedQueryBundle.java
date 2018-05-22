package stroom.query.audit;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import stroom.query.authorisation.HasAuthorisationConfig;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;
import stroom.query.security.HasTokenConfig;

import java.util.function.Function;

/**
 * This Dropwizard bundle can be used to register an implementation of Query Resource implementation
 * This bundle will wrap that implementation in an audited layer, any requests made to your Query Resource
 * will be passed to the {@link QueryEventLoggingService}
 *
 * It will also register an audited version of the external DocRef resource. External DataSources will need to provide
 * implementations of DocRef resource to allow stroom to manage the documents that live outside of it.
 *
 * @param <CONFIG> The configuration class
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <QUERY_SERVICE> Implementation class for the Query Service
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>,
        DOC_REF_POJO extends DocRefEntity,
        QUERY_SERVICE extends QueryService> implements ConfiguredBundle<CONFIG> {


    private final AuditedDocRefBundle<CONFIG,
            DOC_REF_SERVICE,
            DOC_REF_POJO> auditedDocRefBundle;

    private Function<CONFIG, Injector> injectorSupplier;
    protected final Class<DOC_REF_SERVICE> docRefServiceClass;
    protected final Class<DOC_REF_POJO> docRefEntityClass;
    private final Class<QUERY_SERVICE> queryServiceClass;

    public AuditedQueryBundle(final Function<CONFIG, Injector> injectorSupplier,
                              final Class<DOC_REF_SERVICE> docRefServiceClass,
                              final Class<DOC_REF_POJO> docRefEntityClass,
                              final Class<QUERY_SERVICE> queryServiceClass) {
        this.injectorSupplier = injectorSupplier;
        this.docRefServiceClass = docRefServiceClass;
        this.docRefEntityClass = docRefEntityClass;
        this.queryServiceClass = queryServiceClass;
        this.auditedDocRefBundle = new AuditedDocRefBundle<>(injectorSupplier, docRefServiceClass, docRefEntityClass);
    }

    /**
     * This function will be overridden by child classes that have further specific modules to register.
     * @param configuration The dropwizard application configuration
     * @return A guice module which combines the immediate dependencies and any from underlying bundles
     */
    public Module getGuiceModule(final CONFIG configuration) {
        return Modules.combine(new AbstractModule() {
            @Override
            @SuppressWarnings("unchecked")
            protected void configure() {
                bind(QueryService.class).to(queryServiceClass);
            }
        }, auditedDocRefBundle.getGuiceModule(configuration));
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        final Bootstrap<CONFIG> castBootstrap = (Bootstrap<CONFIG>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(auditedDocRefBundle);
    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
        final Injector injector = injectorSupplier.apply(configuration);

        environment.jersey().register(injector.getInstance(AuditedQueryResourceImpl.class));
    }
}
