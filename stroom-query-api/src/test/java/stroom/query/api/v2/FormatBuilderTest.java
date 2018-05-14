package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FormatBuilderTest {
    @Test
    public void doesBuildNumber() {
        final Integer decimalPlaces = 5;
        final Boolean useSeperator = true;

        final Format format = new Format.Builder()
                .number(new NumberFormat.Builder()
                    .decimalPlaces(decimalPlaces)
                    .useSeparator(useSeperator)
                    .build())
                .build();

        assertEquals(Format.Type.NUMBER, format.getType());
        assertNotNull(format.getNumberFormat());
        assertNull(format.getDateTimeFormat());
        assertEquals(decimalPlaces, format.getNumberFormat().getDecimalPlaces());
        assertEquals(useSeperator, format.getNumberFormat().getUseSeparator());
    }

    @Test
    public void doesBuildDateTime() {
        final String pattern = "DAY MONTH YEAR";

        final String timeZoneId = "someId";
        final TimeZone.Use use = TimeZone.Use.LOCAL;
        final Integer offsetHours = 3;
        final Integer offsetMinutes = 5;

        final Format format = new Format.Builder()
                    .dateTime(new DateTimeFormat.Builder()
                        .pattern(pattern)
                        .timeZone(new TimeZone.Builder()
                                .id(timeZoneId)
                                .use(use)
                                .offsetHours(offsetHours)
                                .offsetMinutes(offsetMinutes)
                                .build())
                            .build())
                .build();

        assertEquals(Format.Type.DATE_TIME, format.getType());
        assertNotNull(format.getDateTimeFormat());
        assertNull(format.getNumberFormat());
        assertEquals(pattern, format.getDateTimeFormat().getPattern());

        assertEquals(timeZoneId, format.getDateTimeFormat().getTimeZone().getId());
        assertEquals(use, format.getDateTimeFormat().getTimeZone().getUse());
        assertEquals(offsetHours, format.getDateTimeFormat().getTimeZone().getOffsetHours());
        assertEquals(offsetMinutes, format.getDateTimeFormat().getTimeZone().getOffsetMinutes());
    }
}
