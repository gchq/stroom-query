package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OffsetRangeTest {
    @Test
    public void doesBuild() {
        final Long offset = 30L;
        final Long length = 1000L;

        final OffsetRange offsetRange = new OffsetRange.Builder()
                .offset(offset)
                .length(length)
                .build();

        assertEquals(offset, offsetRange.getOffset());
        assertEquals(length, offsetRange.getLength());
    }
}
