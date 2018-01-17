package stroom.query.hibernate;

import event.logging.EventLoggingService;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.hibernate.SessionFactory;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.AuthorisationServiceConfig;
import stroom.query.audit.authorisation.AuthorisationServiceImpl;
import stroom.query.audit.authorisation.HasAuthorisationConfig;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.audit.rest.AuditedQueryResourceImpl;
import stroom.query.audit.QueryEventLoggingService;
import stroom.query.audit.security.HasTokenConfig;
import stroom.query.audit.security.TokenConfig;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

/**
 * This Dropwizard bundle can be used to build the entire Query Resource implementation stack when the data source is
 * an SQL database, storing one annotated data type. The annotated data type will need to also annotate it's fields with
 * {@link IsDataSourceField}
 * @param <CONFIG> The configuration class
 * @param <QUERY_POJO> The hibernate annotated class.
 * @param <DOC_REF_POJO> POJO class for the Document
 * @param <AUDITED_DOC_REF_RESOURCE> Implementation class for the Audited DocRef Resource
 * @param <DOC_REF_SERVICE> Implementation class for the DocRef Service
 */
public class AuditedCriteriaQueryBundle<CONFIG extends Configuration & HasTokenConfig & HasAuthorisationConfig,
        QUERY_POJO extends QueryableEntity,
        DOC_REF_POJO extends DocRefHibernateEntity,
        AUDITED_DOC_REF_RESOURCE extends AuditedDocRefResourceImpl<DOC_REF_POJO>,
        DOC_REF_SERVICE extends DocRefService<DOC_REF_POJO>> implements ConfiguredBundle<CONFIG> {

    private final Class<QUERY_POJO> queryableEntityClass;

    private final HibernateBundle<CONFIG> hibernateBundle;

    private final Class<DOC_REF_POJO> docRefClass;
    private final Class<AUDITED_DOC_REF_RESOURCE> auditedDocRefResourceClass;
    private final Class<DOC_REF_SERVICE> docRefServiceClass;

    public AuditedCriteriaQueryBundle(final Class<QUERY_POJO> queryableEntityClass,
                                      final HibernateBundle<CONFIG> hibernateBundle,
                                      final Class<DOC_REF_POJO> docRefClass,
                                      final Class<AUDITED_DOC_REF_RESOURCE> auditedDocRefResourceClass,
                                      final Class<DOC_REF_SERVICE> docRefServiceClass) {
        this.queryableEntityClass = queryableEntityClass;

        this.hibernateBundle = hibernateBundle;

        this.docRefClass = docRefClass;
        this.auditedDocRefResourceClass = auditedDocRefResourceClass;
        this.docRefServiceClass = docRefServiceClass;
    }

    @Override
    public void run(final CONFIG configuration,
                    final Environment environment) throws Exception {

        environment.jersey().register(AuditedQueryResourceImpl.class);
        environment.jersey().register(auditedDocRefResourceClass);
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(QueryEventLoggingService.class).to(EventLoggingService.class);

                final QueryService queryService =
                        new QueryServiceCriteriaImpl<>(AuditedCriteriaQueryBundle.this.queryableEntityClass, hibernateBundle.getSessionFactory());

                bind(queryService).to(QueryService.class);
                bind(hibernateBundle.getSessionFactory()).to(SessionFactory.class);
                bind(docRefServiceClass).to(new ParameterizedTypeImpl(DocRefService.class, docRefClass));
                bind(AuthorisationServiceImpl.class).to(AuthorisationService.class);
                bind(configuration.getAuthorisationServiceConfig()).to(AuthorisationServiceConfig.class);
                bind(configuration.getTokenConfig()).to(TokenConfig.class);
            }
        });

    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        final Bootstrap<CONFIG> castBootstrap = (Bootstrap<CONFIG>) bootstrap; // this initialize function should have used the templated config type
        castBootstrap.addBundle(hibernateBundle);
    }
}
