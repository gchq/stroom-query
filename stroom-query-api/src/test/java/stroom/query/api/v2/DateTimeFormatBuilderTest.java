package stroom.query.api.v2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateTimeFormatBuilderTest {
    @Test
    public void doesBuild() {
        final String pattern = "DAY MONTH YEAR";

        final String timeZoneId = "someId";
        final TimeZone.Use use = TimeZone.Use.LOCAL;
        final Integer offsetHours = 3;
        final Integer offsetMinutes = 5;

        final DateTimeFormat dateTimeFormat = new DateTimeFormat.Builder()
                .pattern(pattern)
                .timeZone(new TimeZone.Builder()
                    .id(timeZoneId)
                    .use(use)
                    .offsetHours(offsetHours)
                    .offsetMinutes(offsetMinutes)
                    .build())
                .build();

        assertEquals(pattern, dateTimeFormat.getPattern());

        assertEquals(timeZoneId, dateTimeFormat.getTimeZone().getId());
        assertEquals(use, dateTimeFormat.getTimeZone().getUse());
        assertEquals(offsetHours, dateTimeFormat.getTimeZone().getOffsetHours());
        assertEquals(offsetMinutes, dateTimeFormat.getTimeZone().getOffsetMinutes());
    }
}
