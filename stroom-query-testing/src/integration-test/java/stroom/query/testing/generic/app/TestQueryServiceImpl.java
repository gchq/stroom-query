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
import stroom.query.audit.service.QueryApiException;
import stroom.query.audit.service.QueryService;

import javax.inject.Inject;
import java.util.Optional;

public class TestQueryServiceImpl implements QueryService {

    private final DocRefService<TestDocRefEntity> docRefService;

    @Inject
    public TestQueryServiceImpl(final DocRefService docRefService) {
        this.docRefService = (DocRefService<TestDocRefEntity>) docRefService;
    }

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) throws QueryApiException {
        final Optional<TestDocRefEntity> docRefEntity = docRefService.get(user, docRef.getUuid());

        if (!docRefEntity.isPresent()) {
            return Optional.empty();
        }

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
                                           final SearchRequest request) throws QueryApiException {
        final String dataSourceUuid = request.getQuery().getDataSource().getUuid();

        final Optional<TestDocRefEntity> docRefEntity = docRefService.get(user, dataSourceUuid);

        if (!docRefEntity.isPresent()) {
            return Optional.empty();
        }

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
