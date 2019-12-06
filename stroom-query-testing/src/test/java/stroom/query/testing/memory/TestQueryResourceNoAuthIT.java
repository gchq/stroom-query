package stroom.query.testing.memory;


import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.extension.ExtendWith;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.AbstractField;
import stroom.docref.DocRef;
import stroom.query.api.v2.*;
import stroom.query.testing.DropwizardAppExtensionWithClients;
import stroom.query.testing.QueryResourceNoAuthIT;
import stroom.query.testing.memory.app.App;
import stroom.query.testing.memory.app.Config;
import stroom.query.testing.memory.app.TestDocRefEntity;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TestQueryResourceNoAuthIT extends QueryResourceNoAuthIT<TestDocRefEntity, Config> {
    private static final DropwizardAppExtensionWithClients<Config> appRule =
            new DropwizardAppExtensionWithClients<>(App.class, resourceFilePath("generic_noauth/config.yml"));

    TestQueryResourceNoAuthIT() {
        super(TestDocRefEntity.TYPE,
                appRule);
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
                                        .id(TestDocRefEntity.INDEX_NAME)
                                        .name(TestDocRefEntity.INDEX_NAME)
                                        .expression("${" + TestDocRefEntity.INDEX_NAME + "}")
                                        .build())
                                .addMaxResults(10)
                                .build())
                        .build())
                .build();
    }

    @Override
    protected void assertValidDataSource(final DataSource dataSource) {
        final Set<String> resultFieldNames = dataSource.getFields().stream()
                .map(AbstractField::getName)
                .collect(Collectors.toSet());

        assertThat(resultFieldNames.contains(TestDocRefEntity.INDEX_NAME)).isTrue();
    }

    @Override
    protected TestDocRefEntity getValidEntity(final DocRef docRef) {
        return new TestDocRefEntity.Builder()
                .docRef(docRef)
                .indexName("TestIndex")
                .build();
    }
}
