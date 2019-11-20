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

import stroom.dashboard.expression.v1.Generator;
import stroom.dashboard.expression.v1.Val;
import stroom.query.api.v2.Field;
import stroom.query.api.v2.OffsetRange;
import stroom.query.api.v2.Result;
import stroom.query.api.v2.ResultRequest;
import stroom.query.api.v2.Row;
import stroom.query.api.v2.TableResult;
import stroom.query.api.v2.TableSettings;
import stroom.query.common.v2.format.FieldFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TableResultCreator implements ResultCreator {
    private final FieldFormatter fieldFormatter;
    private volatile List<Field> latestFields;
    private final Sizes defaultMaxResultsSizes;

    public TableResultCreator(final FieldFormatter fieldFormatter,
                              final Sizes defaultMaxResultsSizes) {

        this.fieldFormatter = fieldFormatter;
        this.defaultMaxResultsSizes = defaultMaxResultsSizes;
    }

    @Override
    public Result create(final Data data, final ResultRequest resultRequest) {
        final List<Row> resultList = new ArrayList<>();
        int offset = 0;
        int length = Integer.MAX_VALUE;
        int totalResults = 0;
        String error = null;

        try {
            final OffsetRange range = resultRequest.getRequestedRange();
            if (range != null) {
                offset = range.getOffset().intValue();
                length = range.getLength().intValue();
            }

            //What is the interaction between the paging and the maxResults? The assumption is that
            //maxResults defines the max number of records to come back and the paging can happen up to
            //that maxResults threshold

            Set<String> openGroups = Collections.emptySet();
            if (resultRequest.getOpenGroups() != null) {
                openGroups = new HashSet<>(resultRequest.getOpenGroups());
            }

            TableSettings tableSettings = resultRequest.getMappings().get(0);
            latestFields = tableSettings.getFields();
            // Create a set of sizes that are the minimum values for the combination of user provided sizes for the table and the default maximum sizes.
            final Sizes maxResults = Sizes.min(Sizes.create(tableSettings.getMaxResults()), defaultMaxResultsSizes);

            totalResults = addTableResults(data,
                    latestFields,
                    maxResults,
                    offset,
                    length,
                    openGroups,
                    resultList,
                    null,
                    0,
                    0);
        } catch (final Exception e) {
            error = e.getMessage();
        }

        return new TableResult(resultRequest.getComponentId(), latestFields, resultList, new OffsetRange(offset, resultList.size()), totalResults, error);
    }

    private int addTableResults(final Data data,
                                final List<Field> fields,
                                final Sizes maxResults,
                                final int offset,
                                final int length,
                                final Set<String> openGroups,
                                final List<Row> resultList,
                                final GroupKey parentKey,
                                final int depth,
                                final int position) {
        int maxResultsAtThisDepth = maxResults.size(depth);
        int pos = position;
        int resultCountAtThisLevel = 0;
        // Get top level items.
        final Items<Item> items = data.getChildMap().get(parentKey);
        if (items != null) {
            for (final Item item : items) {
                // If the result is within the requested window (offset + length) then add it.
                if (pos >= offset &&
                        resultList.size() < length) {
                    // Convert all list into fully resolved objects evaluating functions where necessary.
                    final List<String> values = new ArrayList<>(item.getGenerators().length);
                    int i = 0;

                    for (final Field field : fields) {
                        String string = null;

                        if (item.getGenerators().length > i) {
                            final Generator generator = item.getGenerators()[i];
                            if (generator != null) {
                                // Convert all list into fully resolved objects evaluating functions where necessary.
                                final Val val = generator.eval();
                                string = fieldFormatter.format(field, val);
                            }
                        }

                        values.add(string);
                        i++;
                    }

                    if (item.getKey() != null) {
                        resultList.add(new Row(item.getKey().toString(), values, item.getDepth()));
                    } else {
                        resultList.add(new Row(null, values, item.getDepth()));
                    }
                }

                // Increment the overall position.
                pos++;

                // Add child results if a node is open.
                if (item.getKey() != null && openGroups != null && openGroups.contains(item.getKey().toString())) {
                    pos = addTableResults(data, fields, maxResults, offset, length, openGroups, resultList,
                            item.getKey(), depth + 1, pos);
                }

                // Increment the total results at this depth.
                resultCountAtThisLevel++;
                // Stop adding results if we have reached the maximum for this level.
                if (resultCountAtThisLevel >= maxResultsAtThisDepth) {
                    break;
                }
            }
        }
        return pos;
    }

    public List<Field> getFields() {
        return latestFields;
    }
}
