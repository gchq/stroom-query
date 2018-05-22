package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberFormatBuilderTest {
    @Test
    public void doesBuild() {
        final Integer decimalPlaces = 5;
        final Boolean useSeperator = true;

        final NumberFormat numberFormat = new NumberFormat.Builder()
                .decimalPlaces(decimalPlaces)
                .useSeparator(useSeperator)
                .build();

        assertEquals(decimalPlaces, numberFormat.getDecimalPlaces());
        assertEquals(useSeperator, numberFormat.getUseSeparator());
    }
}
