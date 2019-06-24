package stroom.query.testing.generic.app;

import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;
import stroom.util.shared.HasTerminate;

import javax.inject.Inject;
import java.util.Optional;

public class TestQueryServiceImpl implements QueryService {
    public static final String VALID_INDEX_NAME = "TestIndex";

    private final DocRefService<TestDocRefEntity> docRefEntityDocRefService;

    @Inject
    public TestQueryServiceImpl(final DocRefService<TestDocRefEntity> docRefEntityDocRefService) {
        this.docRefEntityDocRefService = docRefEntityDocRefService;
    }

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) {
        return Optional.of(new DataSource.Builder()
                .addFields(new DataSourceField.Builder()
                        .type(DataSourceField.DataSourceFieldType.FIELD)
                        .name(TestDocRefEntity.INDEX_NAME)
                        .queryable(true)
                        .addConditions(
                                ExpressionTerm.Condition.EQUALS,
                                ExpressionTerm.Condition.IN,
                                ExpressionTerm.Condition.IN_DICTIONARY)
                        .build())
                .build());
    }

    @Override
    public Optional<SearchResponse> search(final ServiceUser user,
                                           final SearchRequest request,
                                           final HasTerminate hasTerminate) {
        return Optional.of(new SearchResponse.TableResultBuilder()
                // Yeah whatever...
                .build());
    }

    @Override
    public Boolean destroy(final ServiceUser user,
                           final QueryKey queryKey) {
        return Boolean.TRUE;
    }

    @Override
    public Optional<DocRef> getDocRefForQueryKey(final ServiceUser user,
                                                 final QueryKey queryKey) {
        return Optional.empty();
    }
}
