/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.common.v2;

import stroom.dashboard.expression.v1.FieldIndexMap;
import stroom.dashboard.expression.v1.Generator;
import stroom.dashboard.expression.v1.Val;
import stroom.query.api.v2.Field;
import stroom.query.api.v2.FlatResult;
import stroom.query.api.v2.Format.Type;
import stroom.query.api.v2.OffsetRange;
import stroom.query.api.v2.Result;
import stroom.query.api.v2.ResultRequest;
import stroom.query.api.v2.TableSettings;
import stroom.query.common.v2.format.FieldFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlatResultCreator implements ResultCreator {
    private final FieldFormatter fieldFormatter;
    private final List<Mapper> mappers;
    private final List<Field> fields;
    private final List<Integer> defaultMaxResultsSizes;

    private String error;

    //TODO add defaultMaxResults to args
    public FlatResultCreator(final ResultRequest resultRequest,
                             final Map<String, String> paramMap,
                             final FieldFormatter fieldFormatter,
                             final List<Integer> defaultMaxResultsSizes,
                             final StoreSize storeSize) {

        this.fieldFormatter = fieldFormatter;
        this.defaultMaxResultsSizes = defaultMaxResultsSizes;

        final List<TableSettings> tableSettings = resultRequest.getMappings();


        if (tableSettings.size() > 1) {
            mappers = new ArrayList<>(tableSettings.size() - 1);
            for (int i = 0; i < tableSettings.size() - 1; i++) {
                final TableSettings parent = tableSettings.get(i);
                final TableSettings child = tableSettings.get(i + 1);
                mappers.add(new Mapper(parent, child, paramMap, storeSize));
            }
        } else {
            mappers = Collections.emptyList();
        }

        final TableSettings child = tableSettings.get(tableSettings.size() - 1);

        fields = child.getFields();
    }

    private List<Object> toNodeKey(final Map<Integer, List<Field>> groupFields, final GroupKey key) {
        if (key == null || key.getValues() == null) {
            return null;
        }

        final LinkedList<Object> result = new LinkedList<>();
        GroupKey k = key;
        while (k != null && k.getValues() != null) {
            final List<Field> fields = groupFields.get(k.getDepth());
            final List<Val> values = k.getValues();

            if (values.size() == 0) {
                result.addFirst(null);
            } else if (values.size() == 1) {
                final Val val = values.get(0);
                if (val == null) {
                    result.addFirst(null);
                } else {
                    Field field = null;
                    if (fields != null) {
                        field = fields.get(0);
                    }

                    result.addFirst(convert(field, val));
                }

            } else {
                final StringBuilder sb = new StringBuilder();
                for (Val val : values) {
                    if (val != null) {
                        sb.append(val);
                    }
                    sb.append("|");
                }
                sb.setLength(sb.length() - 1);
                result.addFirst(sb.toString());
            }

            k = k.getParent();
        }

        return result;
    }

    @Override
    public Result create(final Data data, final ResultRequest resultRequest) {
        if (error == null) {
            try {
                // Map data.
                Data mappedData = data;
                for (final Mapper mapper : mappers) {
                    mappedData = mapper.map(mappedData);
                }

                long totalResults = 0;

                // Get top level items.
                final Items<Item> items = mappedData.getChildMap().get(null);

                final List<List<Object>> results = new ArrayList<>();

                if (items != null) {
                    final RangeChecker rangeChecker = RangeCheckerFactory.create(resultRequest.getRequestedRange());
                    final OpenGroups openGroups = OpenGroupsFactory.create(resultRequest.getOpenGroups());

                    //extract the maxResults settings from the last TableSettings object in the chain
                    List<TableSettings> tableSettings = resultRequest.getMappings();
                    final MaxResults maxResults = new MaxResults(
                            tableSettings.get(tableSettings.size() - 1).getMaxResults(),
                            defaultMaxResultsSizes);

                    totalResults = addResults(mappedData, rangeChecker, openGroups, items, results, 0,
                            0, maxResults);
                }

                final FlatResult.Builder resultBuilder = new FlatResult.Builder()
                        .componentId(resultRequest.getComponentId())
                        .size(totalResults)
                        .error(error)
                        .addField(new Field.Builder()
                                .name(":ParentKey")
                                .build())
                        .addField(new Field.Builder()
                                .name(":Key")
                                .build())
                        .addField(new Field.Builder()
                                .name(":Depth")
                                .build());
                this.fields.forEach(resultBuilder::addField);

                results.forEach(resultBuilder::addValues);

                return resultBuilder.build();

            } catch (final RuntimeException e) {
                error = e.getMessage();
            }
        }

        return new FlatResult(resultRequest.getComponentId(), null, null, 0L, error);
    }

    private int addResults(final Data data, final RangeChecker rangeChecker,
                           final OpenGroups openGroups, final Items<Item> items, final List<List<Object>> results,
                           final int depth, final int parentCount, final MaxResults maxResults) {
        int count = parentCount;
        int maxResultsAtThisDepth = maxResults.size(depth);
        int resultCountAtThisLevel = 0;

        final Map<Integer, List<Field>> groupFields = new HashMap<>();
        for (final Field field : fields) {
            if (field.getGroup() != null) {
                groupFields.computeIfAbsent(field.getGroup(), k -> new ArrayList<>()).add(field);
            }
        }

        for (final Item item : items) {
            if (rangeChecker.check(count)) {

                final List<Object> values = new ArrayList<>(fields.size() + 3);

                if (item.getKey() != null) {
                    values.add(toNodeKey(groupFields, item.getKey().getParent()));
                    values.add(toNodeKey(groupFields, item.getKey()));
                } else {
                    values.add(null);
                    values.add(null);
                }
                values.add(depth);

                // Convert all list into fully resolved objects evaluating
                // functions where necessary.
                int i = 0;
                for (final Field field : fields) {
                    final Generator generator = item.getGenerators()[i];
                    Object value = null;
                    if (generator != null) {
                        // Convert all list into fully resolved
                        // objects evaluating functions where necessary.
                        final Val val = generator.eval();
                        if (val != null) {
                            if (fieldFormatter != null) {
                                value = fieldFormatter.format(field, val);
                            } else {
                                value = convert(field, val);
                            }
                        }
                    }

                    values.add(value);
                    i++;
                }

                // Add the values.
                results.add(values);
                resultCountAtThisLevel++;

                // Add child results if a node is open.
                if (item.getKey() != null && openGroups.isOpen(item.getKey())) {
                    final Items<Item> childItems = data.getChildMap().get(item.getKey());
                    if (childItems != null) {
                        count = addResults(data, rangeChecker, openGroups,
                                childItems, results, depth + 1, count, maxResults);
                    }
                }
            }

            // Increment the position.
            count++;

            if (resultCountAtThisLevel >= maxResultsAtThisDepth) {
                break;
            }
        }

        return count;
    }

    // TODO : Replace this with conversion at the item level.
    private Object convert(final Field field, final Val val) {
        if (field != null && field.getFormat() != null && field.getFormat().getType() != null) {
            final Type type = field.getFormat().getType();
            if (Type.NUMBER.equals(type) || Type.DATE_TIME.equals(type)) {
                return val.toDouble();
            }
        }

        return val.toString();
    }

    private String[] getTypes(final Field[] fields) {
        String[] types = null;
        if (fields != null) {
            types = new String[fields.length];
            for (int i = 0; i < types.length; i++) {
                final Field field = fields[i];
                final Type type = getType(field);
                types[i] = type.name();
            }
        }
        return types;
    }

    private Type getType(final Field field) {
        Type type = Type.GENERAL;
        if (field != null && field.getFormat() != null && field.getFormat().getType() != null) {
            type = field.getFormat().getType();
        }

        return type;
    }

    @FunctionalInterface
    private interface RangeChecker {
        boolean check(long count);
    }

    @FunctionalInterface
    private interface OpenGroups {
        boolean isOpen(GroupKey group);
    }

    private static class Mapper {
        private final String[] parentFields;
        private final FieldIndexMap fieldIndexMap;
        private final TableCoprocessor tableCoprocessor;
        private final TablePayloadHandler tablePayloadHandler;

        Mapper(final TableSettings parent,
               final TableSettings child,
               final Map<String, String> paramMap,
               final StoreSize storeSize) {

            parentFields = new String[parent.getFields().size()];
            int i = 0;
            for (final Field field : parent.getFields()) {
                parentFields[i++] = field.getName();
            }

            fieldIndexMap = new FieldIndexMap(true);

            final TableCoprocessorSettings tableCoprocessorSettings = new TableCoprocessorSettings(child);
            tableCoprocessor = new TableCoprocessor(tableCoprocessorSettings,
                    fieldIndexMap,
                    paramMap);

            final MaxResults maxResults = new MaxResults(child.getMaxResults(), Collections.singletonList(Integer.MAX_VALUE));
            tablePayloadHandler = new TablePayloadHandler(child.getFields(), true, maxResults, storeSize);
        }

        public Data map(final Data data) {
            // Get top level items.

            // TODO : Add an option to get detail level items rather than root level items.
            final Items<Item> items = data.getChildMap().get(null);

            for (final Item item : items) {
                final Generator[] generators = item.getGenerators();
                final Val[] values = new Val[fieldIndexMap.size()];
                for (int i = 0; i < generators.length; i++) {
                    final Generator generator = generators[i];
                    if (generator != null) {
                        final int index = fieldIndexMap.get(parentFields[i]);
                        if (index >= 0) {
                            values[index] = generator.eval();
                        }
                    }
                }

                // TODO : Receive Object[] for lazy + nested evaluation.
                tableCoprocessor.receive(values);
            }

            final TablePayload payload = (TablePayload) tableCoprocessor.createPayload();

            tablePayloadHandler.clear();
            tablePayloadHandler.addQueue(payload.getQueue());
            return tablePayloadHandler.getData();
        }
    }

    private static class RangeCheckerFactory {
        public static RangeChecker create(final OffsetRange range) {
            if (range == null) {
                return count -> true;
            }

            final long start = range.getOffset();
            final long end = range.getOffset() + range.getLength();
            return count -> count >= start && count < end;
        }
    }

    private static class OpenGroupsFactory {
        public static OpenGroups create(final List<String> openGroups) {
            if (openGroups == null || openGroups.size() == 0) {
                return group -> true;
            }

            final Set<String> set = new HashSet<>(openGroups);
            return key -> key != null && set.contains(key.toString());
        }
    }
}