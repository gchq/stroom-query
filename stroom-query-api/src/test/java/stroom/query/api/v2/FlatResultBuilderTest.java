package stroom.query.api.v2;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlatResultBuilderTest {
    @Test
    public void doesBuild() {
        // Given
        final String componentId = "someComponentId";
        final String error = "something went wrong";

        final int numberFields = 3;
        final int numberResultSets = 10;

        // When
        final FlatResult.Builder flatResultBuilder = new FlatResult.Builder()
                .componentId(componentId)
                .error(error);
        IntStream.range(0, numberFields).forEach(x ->
                flatResultBuilder
                        .addField(String.format("field%d", x), "expression")
        );
        IntStream.range(0, numberResultSets).forEach(x -> {
            final ListBuilder<?, Object> valuesBuilder = flatResultBuilder.addValues();
            IntStream.range(0, numberFields).forEach(y ->
                    valuesBuilder
                            .value(String.format("field%d_value%d", y, x)));
        });
        final FlatResult flatResult = flatResultBuilder.build();

        // Then
        assertEquals(componentId, flatResult.getComponentId());
        assertEquals(error, flatResult.getError());
        assertEquals(Long.valueOf(numberResultSets), flatResult.getSize());

        final long fieldsCount = flatResult.getStructure().stream()
                .peek(field -> assertTrue(field.getName().startsWith("field")))
                .count();
        assertEquals(numberFields, fieldsCount);

        final long valuesCount = flatResult.getValues().stream().peek(values -> {
            final long vCount = values.stream()
                    .filter(o -> o instanceof String)
                    .map(o -> (String) o)
                    .peek(o -> assertTrue(o.startsWith("field")))
                    .count();
            assertEquals(numberFields, vCount);
        }).count();

        assertEquals(numberResultSets, valuesCount);
    }
}
