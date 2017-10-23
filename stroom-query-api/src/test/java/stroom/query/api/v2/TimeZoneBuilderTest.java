package stroom.query.api.v2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeZoneBuilderTest {
    @Test
    public void doesBuild() {
        final String id = "someId";
        final TimeZone.Use use = TimeZone.Use.LOCAL;
        final Integer offsetHours = 3;
        final Integer offsetMinutes = 5;

        final TimeZone timeZone = new TimeZone.Builder()
                .id(id)
                .use(use)
                .offsetHours(offsetHours)
                .offsetMinutes(offsetMinutes)
                .build();

        assertEquals(id, timeZone.getId());
        assertEquals(use, timeZone.getUse());
        assertEquals(offsetHours, timeZone.getOffsetHours());
        assertEquals(offsetMinutes, timeZone.getOffsetMinutes());
    }
}
