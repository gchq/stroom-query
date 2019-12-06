package stroom.query.api.v2;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class TableSettingsBuilderTest {
    @Test
    public void doesBuild() {
        final Boolean extractValues = true;
        final Boolean showDetail = false;
        final String queryId = "someQueryId";
        
        // Extraction Pipeline
        final String extractPipelineName = "pipelineName";
        final String extractPipelineType = "pipelineType";
        final String extractPipelineUuid = UUID.randomUUID().toString();
        
        // Field 1
        final String field1Name = "field1";
        final Integer field1Group = 57;
        final Integer field1SortOrder = 2;
        final Sort.SortDirection field1SortDirection = Sort.SortDirection.DESCENDING;
        final String field1Expression = "someExpression";
        final String field1FilterExcludes = "stuff to exclude **";
        final String field1FilterIncludes = "stuff to include &&";
        final Integer field1NumberFormatDecimalPlaces = 5;
        final Boolean field1NumberFormatUseSeperator = true;

        // Field 2
        final String field2Name = "field2";
        final Integer field2Group = 57;
        final Integer field2SortOrder = 3;
        final Sort.SortDirection field2SortDirection = Sort.SortDirection.ASCENDING;
        final String field2Expression = "someExpression";
        final String field2FilterExcludes = "stuff to exclude ** field 1";
        final String field2FilterIncludes = "stuff to include field 2&&";
        final Integer field2NumberFormatDecimalPlaces = 6;
        final Boolean field2NumberFormatUseSeperator = false;
        
        final TableSettings tableSettings = new TableSettings.Builder()
                .extractValues(extractValues)
                .showDetail(showDetail)
                .queryId(queryId)
                .addFields(new Field.Builder()
                        .id(field1Name)
                        .name(field1Name)
                        .group(field1Group)
                        .sort(new Sort.Builder()
                                .order(field1SortOrder)
                                .direction(field1SortDirection)
                                .build())
                        .expression(field1Expression)
                        .filter(new Filter.Builder()
                                .includes(field1FilterIncludes)
                                .excludes(field1FilterExcludes)
                                .build())
                        .format(new Format.Builder().number(
                                new NumberFormat.Builder()
                                        .decimalPlaces(field1NumberFormatDecimalPlaces)
                                        .useSeparator(field1NumberFormatUseSeperator)
                                        .build())
                                .build())
                        .build())
                .addFields(new Field.Builder()
                        .id(field2Name)
                        .name(field2Name)
                        .group(field2Group)
                        .sort(new Sort.Builder()
                                .order(field2SortOrder)
                                .direction(field2SortDirection)
                                .build())
                        .expression(field2Expression)
                        .filter(new Filter.Builder()
                                .includes(field2FilterIncludes)
                                .excludes(field2FilterExcludes)
                                .build())
                        .format(new Format.Builder().number(
                                new NumberFormat.Builder()
                                        .decimalPlaces(field2NumberFormatDecimalPlaces)
                                        .useSeparator(field2NumberFormatUseSeperator)
                                        .build())
                                .build())
                        .build())
                .extractionPipeline(extractPipelineType, extractPipelineUuid, extractPipelineName)
                .build();
        
        assertEquals(extractValues, tableSettings.getExtractValues());
        assertEquals(showDetail, tableSettings.getShowDetail());
        assertEquals(queryId, tableSettings.getQueryId());
        
        assertNotNull(tableSettings.getExtractionPipeline());
        assertEquals(extractPipelineName, tableSettings.getExtractionPipeline().getName());
        assertEquals(extractPipelineUuid, tableSettings.getExtractionPipeline().getUuid());
        assertEquals(extractPipelineType, tableSettings.getExtractionPipeline().getType());
        
        assertEquals(2, tableSettings.getFields().size());
        final Field field1 = tableSettings.getFields().get(0);
        assertEquals(field1Name, field1.getName());
        assertEquals(field1Expression, field1.getExpression());
        assertEquals(field1Group, field1.getGroup());

        assertNotNull(field1.getSort());
        assertEquals(field1SortOrder, field1.getSort().getOrder());
        assertEquals(field1SortDirection, field1.getSort().getDirection());

        assertNotNull(field1.getFilter());
        assertEquals(field1FilterExcludes, field1.getFilter().getExcludes());
        assertEquals(field1FilterIncludes, field1.getFilter().getIncludes());

        assertNotNull(field1.getFormat());
        assertEquals(Format.Type.NUMBER, field1.getFormat().getType());
        assertNotNull(field1.getFormat().getNumberFormat());
        assertNull(field1.getFormat().getDateTimeFormat());
        assertEquals(field1NumberFormatDecimalPlaces, field1.getFormat().getNumberFormat().getDecimalPlaces());
        assertEquals(field1NumberFormatUseSeperator, field1.getFormat().getNumberFormat().getUseSeparator());
        
        final Field field2 = tableSettings.getFields().get(1);
        assertEquals(field2Name, field2.getName());
        assertEquals(field2Expression, field2.getExpression());
        assertEquals(field2Group, field2.getGroup());

        assertNotNull(field2.getSort());
        assertEquals(field2SortOrder, field2.getSort().getOrder());
        assertEquals(field2SortDirection, field2.getSort().getDirection());

        assertNotNull(field2.getFilter());
        assertEquals(field2FilterExcludes, field2.getFilter().getExcludes());
        assertEquals(field2FilterIncludes, field2.getFilter().getIncludes());

        assertNotNull(field2.getFormat());
        assertEquals(Format.Type.NUMBER, field2.getFormat().getType());
        assertNotNull(field2.getFormat().getNumberFormat());
        assertNull(field2.getFormat().getDateTimeFormat());
        assertEquals(field2NumberFormatDecimalPlaces, field2.getFormat().getNumberFormat().getDecimalPlaces());
        assertEquals(field2NumberFormatUseSeperator, field2.getFormat().getNumberFormat().getUseSeparator());
        
    }
}
