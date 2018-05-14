package stroom.query.testing.hibernate;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.ClassRule;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.*;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.security.UrlTokenReplacer;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.QueryRemoteServiceIT;
import stroom.query.testing.StroomAuthenticationRule;
import stroom.query.testing.hibernate.app.HibernateApp;
import stroom.query.testing.hibernate.app.HibernateConfig;
import stroom.query.testing.hibernate.app.TestDocRefHibernateEntity;
import stroom.query.testing.hibernate.app.TestQueryableHibernateEntity;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHibernateQueryRemoteServiceIT extends QueryRemoteServiceIT<TestDocRefHibernateEntity, HibernateConfig> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<HibernateConfig> appRule =
            new DropwizardAppWithClientsRule<>(HibernateApp.class, resourceFilePath("hibernate/config.yml"));

    @ClassRule
    public static StroomAuthenticationRule authRule =
            new StroomAuthenticationRule(WireMockConfiguration.options().dynamicPort());

    public TestHibernateQueryRemoteServiceIT() {
        super(TestDocRefHibernateEntity.TYPE,
                TestDocRefHibernateEntity.class,
                appRule,
                authRule);
        UrlTokenReplacer.setPort(authRule.port());
    }

    @Override
    protected SearchRequest getValidSearchRequest(final DocRef docRef,
                                                  final ExpressionOperator expressionOperator,
                                                  final OffsetRange offsetRange) {
        final String queryKey = UUID.randomUUID().toString();
        return new SearchRequest.Builder()
                .query(new Query.Builder()
                        .dataSource(docRef)
                        .expression(expressionOperator)
                        .build())
                .key(queryKey)
                .dateTimeLocale("en-gb")
                .incremental(true)
                .addResultRequests(new ResultRequest.Builder()
                        .fetch(ResultRequest.Fetch.ALL)
                        .resultStyle(ResultRequest.ResultStyle.FLAT)
                        .componentId("componentId")
                        .requestedRange(offsetRange)
                        .addMappings(new TableSettings.Builder()
                                .queryId(queryKey)
                                .extractValues(false)
                                .showDetail(false)
                                .addFields(new Field.Builder()
                                        .name(TestQueryableHibernateEntity.FLAVOUR)
                                        .expression("${" + TestQueryableHibernateEntity.FLAVOUR + "}")
                                        .build())
                                .addMaxResults(10)
                                .build())
                        .build())
                .build();
    }

    @Override
    protected void assertValidDataSource(final DataSource dataSource) {
        final Set<String> resultFieldNames = dataSource.getFields().stream()
                .map(DataSourceField::getName)
                .collect(Collectors.toSet());

        assertTrue(resultFieldNames.contains(DocRefEntity.CREATE_TIME));
        assertTrue(resultFieldNames.contains(DocRefEntity.CREATE_USER));
        assertTrue(resultFieldNames.contains(DocRefEntity.UPDATE_TIME));
        assertTrue(resultFieldNames.contains(DocRefEntity.UPDATE_USER));
        assertTrue(resultFieldNames.contains(TestQueryableHibernateEntity.ID));
        assertTrue(resultFieldNames.contains(TestQueryableHibernateEntity.FLAVOUR));
    }

    @Override
    protected TestDocRefHibernateEntity getValidEntity(final DocRef docRef) {
        return new TestDocRefHibernateEntity.Builder()
                .docRef(docRef)
                .clanName("TestClan")
                .build();
    }
}
