package stroom.query.api.v2;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableResultBuilderTest {
    @Test
    public void doesBuild() {
        // Given
        final String error = "Something went wrong";
        final String componentId = "someTabularComponentId";

        final Long offset = 30L;
        final Long length = 1000L;

        final Integer numberResults = 20;

        // When
        final TableResult.Builder<?> builder = new TableResult.Builder<>()
                .componentId(componentId)
                .error(error)
                .resultRange()
                    .offset(offset)
                    .length(length)
                    .end();

        IntStream.range(0, numberResults).forEach(x ->
            builder.addRow().groupKey(String.format("rowGroup%d", x)).end()
        );

        final TableResult tableResult = builder.build();

        // Then
        assertEquals(componentId, tableResult.getComponentId());
        assertEquals(error, tableResult.getError());
        assertEquals(offset, tableResult.getResultRange().getOffset());
        assertEquals(length, tableResult.getResultRange().getLength());

        final long rowCount = tableResult.getRows().stream().peek(row ->
                assertTrue(row.getGroupKey().startsWith("rowGroup"))
        ).count();
        assertEquals((long) numberResults, rowCount);
        assertEquals(numberResults, tableResult.getTotalResults());
    }
}
