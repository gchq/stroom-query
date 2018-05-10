package stroom.query.jooq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.datasource.api.v2.DataSource;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.security.CurrentServiceUser;
import stroom.query.security.ServiceUser;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryApiException;
import stroom.query.audit.service.QueryService;
import stroom.query.common.v2.SearchResponseCreator;
import stroom.query.common.v2.SearchResponseCreatorCache;
import stroom.query.common.v2.SearchResponseCreatorManager;
import stroom.query.jooq.search.JooqDataSourceProvider;
import stroom.query.jooq.search.JooqSearchResponseCreatorManager;

import javax.inject.Inject;
import java.util.Optional;

public class QueryServiceJooqImpl implements QueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryServiceJooqImpl.class);

    private final JooqDataSourceProvider dataSourceProvider;

    private final DocRefService<?> docRefService;

    private final SearchResponseCreatorManager searchResponseCreatorManager;

    @Inject
    public QueryServiceJooqImpl(final JooqSearchResponseCreatorManager searchResponseCreatorManager,
                                final JooqDataSourceProvider dataSourceProvider,
                                final DocRefService docRefService) {
        this.docRefService = docRefService;
        this.dataSourceProvider = dataSourceProvider;
        this.searchResponseCreatorManager = searchResponseCreatorManager;
    }

    @Override
    public String getType() {
        return docRefService.getType();
    }

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) throws QueryApiException {
        final Optional<? extends DocRefEntity> docRefEntity = docRefService.get(user, docRef.getUuid());

        if (!docRefEntity.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new DataSource(dataSourceProvider.getFields()));
    }

    @Override
    public Optional<SearchResponse> search(final ServiceUser user,
                                           final SearchRequest request) throws QueryApiException {

        CurrentServiceUser.pushServiceUser(user);

        //if this is the first call for this query key then it will create a searchResponseCreator (& store) that have
        //a lifespan beyond the scope of this request and then begin the search for the data
        //If it is not the first call for this query key then it will return the existing searchResponseCreator with
        //access to whatever data has been found so far
        final SearchResponseCreator searchResponseCreator = searchResponseCreatorManager.get(new SearchResponseCreatorCache.Key(request));

        //create a response from the data found so far, this could be complete/incomplete
        final SearchResponse response = searchResponseCreator.create(request);

        CurrentServiceUser.popServiceUser();

        return Optional.of(response);
    }

    @Override
    public Boolean destroy(final ServiceUser user,
                           final QueryKey queryKey) {
        searchResponseCreatorManager.remove(new SearchResponseCreatorCache.Key(queryKey));
        return Boolean.TRUE;
    }

    @Override
    public Optional<DocRef> getDocRefForQueryKey(final ServiceUser user,
                                                 final QueryKey queryKey) {
        return Optional.empty();
    }
}
