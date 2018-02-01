package stroom.query.hibernate;

import io.dropwizard.Configuration;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hibernate.SessionFactory;
import stroom.query.audit.AuditedQueryBundle;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.service.DocRefService;

import javax.inject.Provider;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;

/**
 * This Dropwizard bundle can be used to build the entire Query Resource implementation stack when the data source is
 * an SQL database, storing one annotated data type. The annotated data type will need to also annotate it's fields with
 * {@link IsDataSourceField}
 * @param <CONFIG> The configuration class
 * @param <QUERY_POJO> The hibernate annotated class.
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <AUDITED_QUERY_RESOURCE> Implementation class for the Audited Query Resource
 * @param <AUDITED_DOC_REF_RESOURCE> Implementation class for the Audited DocRef Resource
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedCriteriaQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        QUERY_POJO extends QueryableEntity,
        DOC_REF_POJO extends DocRefHibernateEntity,
        AUDITED_QUERY_RESOURCE extends AuditedQueryResourceImpl<DOC_REF_POJO>,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>,
        AUDITED_DOC_REF_RESOURCE extends AuditedDocRefResourceImpl<DOC_REF_POJO>>
        extends AuditedQueryBundle<CONFIG,
                DOC_REF_POJO,
                QueryServiceCriteriaImpl,
                AUDITED_QUERY_RESOURCE,
                DOC_REF_SERVICE,
                AUDITED_DOC_REF_RESOURCE> {

    private final Class<QUERY_POJO> queryableEntityClass;

    private final HibernateBundle<CONFIG> hibernateBundle;

    public AuditedCriteriaQueryBundle(final Class<QUERY_POJO> queryableEntityClass,
                                      final HibernateBundle<CONFIG> hibernateBundle,
                                      final Class<DOC_REF_POJO> docRefEntityClass,
                                      final Class<AUDITED_QUERY_RESOURCE> auditedQueryResourceClass,
                                      final Class<DOC_REF_SERVICE> docRefServiceClass,
                                      final Class<AUDITED_DOC_REF_RESOURCE> auditedDocRefResourceClass) {
        super(docRefEntityClass,
                QueryServiceCriteriaImpl.class,
                auditedQueryResourceClass,
                docRefServiceClass,
                auditedDocRefResourceClass);

        this.queryableEntityClass = queryableEntityClass;
        this.hibernateBundle = hibernateBundle;
    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) {
        super.run(configuration, environment);

        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(docRefServiceClass).to(new ParameterizedTypeImpl(DocRefService.class, docRefEntityClass));
                bind(new QueryableEntity.ClassProvider<>(queryableEntityClass)).to(QueryableEntity.ClassProvider.class);
                bind(hibernateBundle.getSessionFactory()).to(SessionFactory.class);
            }
        });
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        super.initialize(bootstrap);

        final Bootstrap<CONFIG> castBootstrap = (Bootstrap<CONFIG>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(hibernateBundle);
    }
}
