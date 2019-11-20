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

import org.junit.Assert;
import org.junit.Test;
import stroom.dashboard.expression.v1.Val;
import stroom.dashboard.expression.v1.ValString;
import stroom.mapreduce.v2.UnsafePairQueue;
import stroom.query.api.v2.Field;
import stroom.query.api.v2.Format;
import stroom.query.api.v2.OffsetRange;
import stroom.query.api.v2.ResultRequest;
import stroom.query.api.v2.Row;
import stroom.query.api.v2.Sort;
import stroom.query.api.v2.Sort.SortDirection;
import stroom.query.api.v2.TableResult;
import stroom.query.api.v2.TableSettings;
import stroom.query.common.v2.format.FieldFormatter;
import stroom.query.common.v2.format.FormatterFactory;
import stroom.query.shared.v2.ParamUtil;
import stroom.util.shared.HasTerminateImpl;

import java.util.Collections;

public class TestTablePayloadHandler {
    private final Sizes defaultMaxResultsSizes = Sizes.create(50);
    private final Sizes maxResults = Sizes.min(Sizes.create(50), defaultMaxResultsSizes);
    private final Sizes storeSize = Sizes.create(100);

    @Test
    public void basicTest() {
        final FormatterFactory formatterFactory = new FormatterFactory(null);
        final FieldFormatter fieldFormatter = new FieldFormatter(formatterFactory);

//        final DataSourceFieldsMap dataSourceFieldsMap = new DataSourceFieldsMap();

//        final DataSourceField indexField = new DataSourceField(DataSourceFieldType.FIELD, "Text");
//        dataSourceFieldsMap.put(indexField);

        final TableSettings tableSettings = new TableSettings.Builder()
                .addFields(new Field.Builder()
                        .id("Text")
                        .name("Text")
                        .expression(ParamUtil.makeParam("Text"))
                        .format(Format.Type.TEXT)
                        .build())
                .build();

        final CompiledDepths compiledDepths = new CompiledDepths(tableSettings.getFields(), tableSettings.showDetail());
        final CompiledFields compiledFields = new CompiledFields(tableSettings.getFields(), null, Collections.emptyMap());

        final UnsafePairQueue<GroupKey, Item> queue = new UnsafePairQueue<>();
        final ItemMapper itemMapper = new ItemMapper(queue, compiledFields, compiledDepths.getMaxDepth(),
                compiledDepths.getMaxGroupDepth());

        for (int i = 0; i < 3000; i++) {
            final String text = "Text " + i;
            final Val[] values = new Val[1];
            values[0] = ValString.create(text);

            itemMapper.collect(null, values);
        }

        final TablePayloadHandler payloadHandler = new TablePayloadHandler(
                tableSettings.getFields(),
                tableSettings.showDetail(),
                maxResults,
                storeSize);
        payloadHandler.addQueue(queue, new HasTerminateImpl());
        final Data data = payloadHandler.getData();

        // Make sure we only get 50 results.
        final ResultRequest tableResultRequest = new ResultRequest(
                "componentX",
                tableSettings, new OffsetRange(0, 3000));
        final TableResultCreator tableComponentResultCreator = new TableResultCreator(
                fieldFormatter,
                defaultMaxResultsSizes);
        final TableResult searchResult = (TableResult) tableComponentResultCreator.create(
                data,
                tableResultRequest);
        Assert.assertEquals(50, searchResult.getTotalResults().intValue());
    }

    @Test
    public void sortedTextTest() {
        final Sort sort = new Sort(0, SortDirection.ASCENDING);

//        final DataSourceFieldsMap dataSourceFieldsMap = new DataSourceFieldsMap();
//
//        final IndexField indexField = new IndexField();
//        indexField.setFieldName("Text");
//        indexField.setFieldType(IndexFieldType.FIELD);
//        dataSourceFieldsMap.put(indexField);

        final TableSettings tableSettings = new TableSettings.Builder()
                .addFields(new Field.Builder()
                        .id("Text")
                        .name("Text")
                        .expression(ParamUtil.makeParam("Text"))
                        .sort(sort)
                        .build())
                .build();

        final CompiledDepths compiledDepths = new CompiledDepths(tableSettings.getFields(), tableSettings.showDetail());
        final CompiledFields compiledFields = new CompiledFields(tableSettings.getFields(), null, Collections.emptyMap());

        final UnsafePairQueue<GroupKey, Item> queue = new UnsafePairQueue<>();
        final ItemMapper itemMapper = new ItemMapper(queue, compiledFields, compiledDepths.getMaxDepth(),
                compiledDepths.getMaxGroupDepth());

        for (int i = 0; i < 3000; i++) {
            final String text = "Text " + (int) (Math.random() * 100);
            final Val[] values = new Val[1];
            values[0] = ValString.create(text);

            itemMapper.collect(null, values);
        }

        final TablePayloadHandler payloadHandler = new TablePayloadHandler(tableSettings.getFields(),
                tableSettings.showDetail(), maxResults, storeSize);
        payloadHandler.addQueue(queue, new HasTerminateImpl());
        final Data data = payloadHandler.getData();

        final ResultRequest tableResultRequest = new ResultRequest("componentX", tableSettings, new OffsetRange(0, 3000));
        checkResults(data, tableResultRequest, 0);
    }

    @Test
    public void sortedNumberTest() {
        final Sort sort = new Sort(0, SortDirection.ASCENDING);

//        final DataSourceFieldsMap dataSourceFieldsMap = new DataSourceFieldsMap();
//
//        final IndexField indexField = new IndexField();
//        indexField.setFieldName("Number");
//        indexField.setFieldType(IndexFieldType.ID);
//        dataSourceFieldsMap.put(indexField);


        final TableSettings tableSettings = new TableSettings.Builder()
                .addFields(new Field.Builder()
                        .id("Number")
                        .name("Number")
                        .expression(ParamUtil.makeParam("Number"))
                        .sort(sort)
                        .build())
                .build();

        final CompiledDepths compiledDepths = new CompiledDepths(tableSettings.getFields(), tableSettings.showDetail());
        final CompiledFields compiledFields = new CompiledFields(tableSettings.getFields(), null, Collections.emptyMap());

        final UnsafePairQueue<GroupKey, Item> queue = new UnsafePairQueue<>();
        final ItemMapper itemMapper = new ItemMapper(queue, compiledFields, compiledDepths.getMaxDepth(),
                compiledDepths.getMaxGroupDepth());

        for (int i = 0; i < 3000; i++) {
            final String text = String.valueOf((int) (Math.random() * 100));
            final Val[] values = new Val[1];
            values[0] = ValString.create(text);

            itemMapper.collect(null, values);
        }

        final TablePayloadHandler payloadHandler = new TablePayloadHandler(tableSettings.getFields(),
                tableSettings.showDetail(), maxResults, storeSize);
        payloadHandler.addQueue(queue, new HasTerminateImpl());
        final Data data = payloadHandler.getData();

        final ResultRequest tableResultRequest = new ResultRequest("componentX", tableSettings, new OffsetRange(0, 3000));
        checkResults(data, tableResultRequest, 0);
    }

    @Test
    public void sortedCountedTextTest1() {
        final Sort sort = new Sort(0, SortDirection.ASCENDING);

//        final DataSourceFieldsMap dataSourceFieldsMap = new DataSourceFieldsMap();

        final Field count = new Field.Builder()
                .id("Count")
                .name("Count")
                .expression("count()")
                .sort(sort)
                .build();

//        final IndexField indexField = new IndexField();
//        indexField.setFieldName("Text");
//        indexField.setFieldType(IndexFieldType.FIELD);
//        dataSourceFieldsMap.put(indexField);

        final TableSettings tableSettings = new TableSettings.Builder()
                .addFields(new Field.Builder()
                        .id("Text")
                        .name("Text")
                        .expression(ParamUtil.makeParam("Text"))
                        .group(0)
                        .build())
                .build();

        final CompiledDepths compiledDepths = new CompiledDepths(tableSettings.getFields(), tableSettings.showDetail());
        final CompiledFields compiledFields = new CompiledFields(tableSettings.getFields(), null, Collections.emptyMap());

        final UnsafePairQueue<GroupKey, Item> queue = new UnsafePairQueue<>();
        final ItemMapper itemMapper = new ItemMapper(queue, compiledFields, compiledDepths.getMaxDepth(),
                compiledDepths.getMaxGroupDepth());

        for (int i = 0; i < 3000; i++) {
            final String text = "Text " + (int) (Math.random() * 100);
            final Val[] values = new Val[2];
            values[1] = ValString.create(text);

            itemMapper.collect(null, values);
        }

        final TablePayloadHandler payloadHandler = new TablePayloadHandler(tableSettings.getFields(),
                tableSettings.showDetail(), maxResults, storeSize);
        payloadHandler.addQueue(queue, new HasTerminateImpl());
        final Data data = payloadHandler.getData();

        final ResultRequest tableResultRequest = new ResultRequest("componentX", tableSettings, new OffsetRange(0, 3000));
        checkResults(data, tableResultRequest, 0);
    }

    @Test
    public void sortedCountedTextTest2() {
        final Sort sort = new Sort(0, SortDirection.ASCENDING);

//        final DataSourceFieldsMap dataSourceFieldsMap = new DataSourceFieldsMap();


//        final IndexField indexField = new IndexField();
//        indexField.setFieldName("Text");
//        indexField.setFieldType(IndexFieldType.FIELD);
//        dataSourceFieldsMap.put(indexField);

        final TableSettings tableSettings = new TableSettings.Builder()
                .addFields(new Field.Builder()
                        .id("Count")
                        .name("Count")
                        .expression("count()")
                        .build())
                .addFields(new Field.Builder()
                        .id("Text")
                        .name("Text")
                        .expression(ParamUtil.makeParam("Text"))
                        .sort(sort)
                        .group(0)
                        .build())
                .build();

        final CompiledDepths compiledDepths = new CompiledDepths(tableSettings.getFields(), tableSettings.showDetail());
        final CompiledFields compiledFields = new CompiledFields(tableSettings.getFields(), null, Collections.emptyMap());

        final UnsafePairQueue<GroupKey, Item> queue = new UnsafePairQueue<>();
        final ItemMapper itemMapper = new ItemMapper(queue, compiledFields, compiledDepths.getMaxDepth(),
                compiledDepths.getMaxGroupDepth());

        for (int i = 0; i < 3000; i++) {
            final String text = "Text " + (int) (Math.random() * 100);
            final Val[] values = new Val[2];
            values[1] = ValString.create(text);

            itemMapper.collect(null, values);
        }

        final TablePayloadHandler payloadHandler = new TablePayloadHandler(tableSettings.getFields(),
                tableSettings.showDetail(), maxResults, storeSize);
        payloadHandler.addQueue(queue, new HasTerminateImpl());
        final Data data = payloadHandler.getData();

        final ResultRequest tableResultRequest = new ResultRequest("componentX", tableSettings, new OffsetRange(0, 3000));
        checkResults(data, tableResultRequest, 1);
    }

    @Test
    public void sortedCountedTextTest3() {
        final Sort sort = new Sort(0, SortDirection.ASCENDING);

//        final DataSourceFieldsMap dataSourceFieldsMap = new DataSourceFieldsMap();

//        final IndexField indexField = new IndexField();
//        indexField.setFieldName("Text");
//        indexField.setFieldType(IndexFieldType.FIELD);
//        dataSourceFieldsMap.put(indexField);

        final TableSettings tableSettings = new TableSettings.Builder()
                .addFields(new Field.Builder()
                        .id("Count")
                        .name("Count")
                        .expression("count()")
                        .build())
                .addFields(new Field.Builder()
                        .id("Text")
                        .name("Text")
                        .expression(ParamUtil.makeParam("Text"))
                        .sort(sort)
                        .group(1)
                        .build())
                .build();

        final CompiledDepths compiledDepths = new CompiledDepths(tableSettings.getFields(), tableSettings.showDetail());
        final CompiledFields compiledFields = new CompiledFields(tableSettings.getFields(), null, Collections.emptyMap());

        final UnsafePairQueue<GroupKey, Item> queue = new UnsafePairQueue<>();
        final ItemMapper itemMapper = new ItemMapper(queue, compiledFields, compiledDepths.getMaxDepth(),
                compiledDepths.getMaxGroupDepth());

        for (int i = 0; i < 3000; i++) {
            final String text = "Text " + (int) (Math.random() * 100);
            final Val[] values = new Val[2];
            values[1] = ValString.create(text);

            itemMapper.collect(null, values);
        }

        final TablePayloadHandler payloadHandler = new TablePayloadHandler(tableSettings.getFields(),
                tableSettings.showDetail(), maxResults, storeSize);
        payloadHandler.addQueue(queue, new HasTerminateImpl());
        final Data data = payloadHandler.getData();

        final ResultRequest tableResultRequest = new ResultRequest(
                "componentX",
                tableSettings,
                new OffsetRange(0, 3000));
        checkResults(data, tableResultRequest, 1);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void checkResults(final Data data, final ResultRequest tableResultRequest,
                              final int sortCol) {
        final FormatterFactory formatterFactory = new FormatterFactory(null);
        final FieldFormatter fieldFormatter = new FieldFormatter(formatterFactory);

        // Make sure we only get 2000 results.
        final TableResultCreator tableComponentResultCreator = new TableResultCreator(
                fieldFormatter,
                defaultMaxResultsSizes);
        final TableResult searchResult = (TableResult) tableComponentResultCreator.create(data,
                tableResultRequest);

        Assert.assertTrue(searchResult.getTotalResults() <= 50);

        String lastValue = null;
        for (final Row result : searchResult.getRows()) {
            final String value = result.getValues().get(sortCol);

            if (lastValue != null) {
                final int diff = lastValue.compareTo(value);
                Assert.assertTrue(diff <= 0);
            }
            lastValue = value;
        }
    }
}
