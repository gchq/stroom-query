package stroom.query.jooq.search;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import stroom.dashboard.expression.v1.FieldIndexMap;
import stroom.dashboard.expression.v1.Val;
import stroom.dashboard.expression.v1.ValString;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.ExpressionItem;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.ExpressionTerm;
import stroom.query.api.v2.Param;
import stroom.query.api.v2.SearchRequest;
import stroom.query.audit.CriteriaStore;
import stroom.query.audit.model.QueryableEntity;
import stroom.query.security.CurrentServiceUser;
import stroom.query.security.ServiceUser;
import stroom.query.audit.service.DocRefService;
import stroom.query.audit.service.QueryApiException;
import stroom.query.common.v2.Coprocessor;
import stroom.query.common.v2.CoprocessorSettings;
import stroom.query.common.v2.CoprocessorSettingsMap;
import stroom.query.common.v2.Payload;
import stroom.query.common.v2.Sizes;
import stroom.query.common.v2.Store;
import stroom.query.common.v2.StoreFactory;
import stroom.query.common.v2.TableCoprocessor;
import stroom.query.common.v2.TableCoprocessorSettings;
import stroom.query.jooq.JooqEntity;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.not;
import static org.jooq.impl.DSL.or;

public class JooqStoreFactory implements StoreFactory {
    private final JooqDataSourceProvider dataSourceProvider;
    private final DSLContext database;
    private final DocRefService<?> docRefService;
    private final Table<Record> table;

    @Inject
    public JooqStoreFactory(final QueryableEntity.ClassProvider dtoClassProvider,
                            final DocRefService docRefService,
                            final DSLContext database,
                            final JooqDataSourceProvider dataSourceProvider) {
        this.database = database;
        this.docRefService = docRefService;
        this.dataSourceProvider = dataSourceProvider;

        final Class<?> dtoClass = dtoClassProvider.get();

        this.table = Optional.ofNullable(dtoClass.getAnnotation(JooqEntity.class))
                .map(JooqEntity::tableName)
                .map(DSL::table)
                .orElseThrow(() -> new IllegalArgumentException("The Document Entity Class must be annotated with JooqEntity"));
    }

    @Override
    public Store create(final SearchRequest searchRequest) {
        final String dataSourceUuid = searchRequest.getQuery().getDataSource().getUuid();

        try {
            final ServiceUser user = CurrentServiceUser.currentServiceUser();
            docRefService.get(user, dataSourceUuid)
                    .orElseThrow(() -> new RuntimeException(String.format("Could not find doc ref entity for data source %s", dataSourceUuid)));
        } catch (QueryApiException e) {
            throw new RuntimeException(String.format("Could not find doc ref entity for data source %s", dataSourceUuid));
        }

        return database.transactionResult(configuration -> {

            final Result<Record> results = DSL.using(configuration)
                    .select(dataSourceProvider.getFields().stream()
                            .map(f -> field(f.getName()))
                            .collect(Collectors.toList()))
                    .from(table)
                    .where(and(getCondition(searchRequest.getQuery().getExpression())))
                    .fetch();

            return projectResults(searchRequest, results);
        });
    }

    private Condition getCondition(final ExpressionItem item) {
        if (!item.getEnabled()) {
            return null;
        }

        if (item instanceof ExpressionTerm) {
            final ExpressionTerm term = (ExpressionTerm) item;

            switch (term.getCondition()) {
                case EQUALS: {
                    return field(term.getField()).equal(term.getValue());
                }
                case CONTAINS: {
                    return field(term.getField()).like("%" + term.getValue() + "%");
                }
                case BETWEEN: {
                    final String[] parts = term.getValue().split(",");
                    if (parts.length == 2) {
                        return field(term.getField()).between(parts[0], parts[1]);
                    }
                    break;
                }
                case GREATER_THAN: {
                    return field(term.getField()).greaterThan(term.getValue());
                }
                case GREATER_THAN_OR_EQUAL_TO: {
                    return field(term.getField()).greaterOrEqual(term.getValue());
                }
                case LESS_THAN: {
                    return field(term.getField()).lessThan(term.getValue());
                }
                case LESS_THAN_OR_EQUAL_TO: {
                    return field(term.getField()).lessOrEqual(term.getValue());
                }
                case IN: {
                    final String[] parts = term.getValue().split(",");
                    return field(term.getField()).in((Object[]) parts);
                }
                case IN_DICTIONARY: {
                    // Not sure how to handle this yet
                }

            }

        } else if (item instanceof ExpressionOperator) {
            final ExpressionOperator operator = (ExpressionOperator) item;

            final Condition[] children = operator.getChildren().stream()
                    .map(this::getCondition)
                    .filter(Objects::nonNull)
                    .toArray(Condition[]::new);

            switch (operator.getOp()) {
                case AND:
                    return and(children);
                case OR:
                    return or(children);
                case NOT:

                    if (children.length == 1) {
                        // A single child, just apply the 'not' to that first item
                        return not(children[0]);
                    } else if (children.length > 1) {
                        // If there are multiple children, apply an and around them all
                        return and(Arrays.stream(children)
                                .map(DSL::not)
                                .toArray(Condition[]::new));
                    }
                default:
                    // Fall through to null if there aren't any children
                    break;
            }
        }

        return null;
    }

    // TODO I copied this from 'stats', but can't make head or tail of it to try and move it into somewhere more sensible
    private Store projectResults(final SearchRequest searchRequest,
                                 final List<Record> tuples) {

        // TODO: possibly the mapping from the componentId to the coprocessorsettings map is a bit odd.
        final CoprocessorSettingsMap coprocessorSettingsMap = CoprocessorSettingsMap.create(searchRequest);

        final Map<CoprocessorSettingsMap.CoprocessorKey, Coprocessor> coprocessorMap = new HashMap<>();

        // TODO: Mapping to this is complicated! it'd be nice not to have to do this.
        final FieldIndexMap fieldIndexMap = new FieldIndexMap(true);

        // Compile all of the result component options to optimise pattern matching etc.
        if (coprocessorSettingsMap.getMap() != null) {
            for (final Map.Entry<CoprocessorSettingsMap.CoprocessorKey, CoprocessorSettings> entry : coprocessorSettingsMap.getMap().entrySet()) {
                final CoprocessorSettingsMap.CoprocessorKey coprocessorId = entry.getKey();
                final CoprocessorSettings coprocessorSettings = entry.getValue();

                // Create a parameter map.
                final Map<String, String> paramMap;
                if (searchRequest.getQuery().getParams() != null) {
                    paramMap = searchRequest.getQuery().getParams().stream()
                            .collect(Collectors.toMap(Param::getKey, Param::getValue));
                } else {
                    paramMap = Collections.emptyMap();
                }

                if (coprocessorSettings instanceof TableCoprocessorSettings) {
                    final TableCoprocessorSettings tableCoprocessorSettings = (TableCoprocessorSettings) coprocessorSettings;
                    final Coprocessor coprocessor = new TableCoprocessor(tableCoprocessorSettings,
                            fieldIndexMap,
                            paramMap);

                    coprocessorMap.put(coprocessorId, coprocessor);
                }
            }
        }

        //TODO TableCoprocessor is doing a lot of work to pre-process and aggregate the datas

        for (Record criteriaDataPoint : tuples) {
            Val[] dataArray = new Val[fieldIndexMap.size()];

            //TODO should probably drive this off a new fieldIndexMap.getEntries() method or similar
            //then we only loop round fields we car about
            final List<DataSourceField> fields = dataSourceProvider.getFields();
            for (int x = 0; x < fields.size(); x++) {
                final Object value = criteriaDataPoint.get(x);
                final String fieldName = fields.get(x).getName();

                int posInDataArray = fieldIndexMap.get(fieldName);
                //if the fieldIndexMap returns -1 the field has not been requested
                if (posInDataArray != -1) {
                    dataArray[posInDataArray] = ValString.create(value.toString());
                }
            }

            coprocessorMap.forEach((key, value) -> value.receive(dataArray));
        }

        // TODO putting things into a payload and taking them out again is a waste of time in this case. We could use a queue instead and that'd be fine.
        //TODO: 'Payload' is a cluster specific name - what lucene ships back from a node.
        // Produce payloads for each coprocessor.
        Map<CoprocessorSettingsMap.CoprocessorKey, Payload> payloadMap = null;
        if (coprocessorMap.size() > 0) {
            for (final Map.Entry<CoprocessorSettingsMap.CoprocessorKey, Coprocessor> entry : coprocessorMap.entrySet()) {
                final Payload payload = entry.getValue().createPayload();
                if (payload != null) {
                    if (payloadMap == null) {
                        payloadMap = new HashMap<>();
                    }
                    payloadMap.put(entry.getKey(), payload);
                }
            }
        }

        // Construct the store
        final Sizes storeSize = Sizes.create(tuples.size());
        return new CriteriaStore(
                storeSize,
                storeSize,
                coprocessorSettingsMap,
                payloadMap);
    }
}
