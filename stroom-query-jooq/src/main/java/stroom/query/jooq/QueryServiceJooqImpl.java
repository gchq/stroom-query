package stroom.query.jooq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.DocRef;
import stroom.query.api.v2.QueryKey;
import stroom.query.api.v2.SearchRequest;
import stroom.query.api.v2.SearchResponse;
import stroom.query.audit.model.IsDataSourceField;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryService;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QueryServiceJooqImpl<
        DOC_REF_ENTITY extends DocRefJooqEntity,
        QUERYABLE_ENTITY extends QueryableJooqEntity> implements QueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryServiceJooqImpl.class);

    private final Class<QUERYABLE_ENTITY> dtoClass;

    private final List<DataSourceField> fields;

    private final DocRefService<DOC_REF_ENTITY> docRefService;

    @Inject
    public QueryServiceJooqImpl(final QueryableEntity.ClassProvider<QUERYABLE_ENTITY> dtoClassProvider,
                                final DocRefService<DOC_REF_ENTITY> docRefService) {
        this.docRefService = docRefService;
        this.dtoClass = dtoClassProvider.get();

        this.fields = QueryableEntity.getFields(dtoClass)
                .map(IsDataSourceField::fieldSupplier)
                .map(aClass -> {
                    try {
                        return aClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        LOGGER.warn("Could not create instance of DataSourceField supplier with " + aClass.getName());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(Supplier::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DataSource> getDataSource(final ServiceUser user,
                                              final DocRef docRef) throws Exception {
        final Optional<DOC_REF_ENTITY> docRefEntity = docRefService.get(user, docRef.getUuid());

        if (!docRefEntity.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new DataSource(this.fields));
    }

    @Override
    public Optional<SearchResponse> search(final ServiceUser user,
                                           final SearchRequest request) throws Exception {
        final String dataSourceUuid = request.getQuery().getDataSource().getUuid();

        final Optional<DOC_REF_ENTITY> docRefEntity = docRefService.get(user, dataSourceUuid);

        if (!docRefEntity.isPresent()) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    @Override
    public Boolean destroy(final ServiceUser user,
                           final QueryKey queryKey) throws Exception {
        return Boolean.TRUE;
    }

    @Override
    public Optional<DocRef> getDocRefForQueryKey(final ServiceUser user,
                                                 final QueryKey queryKey) throws Exception {
        return Optional.empty();
    }
}
