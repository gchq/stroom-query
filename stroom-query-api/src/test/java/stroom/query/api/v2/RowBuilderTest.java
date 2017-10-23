package stroom.query.api.v2;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RowBuilderTest {
    @Test
    public void doesBuild() {
        final Integer depth = 3;
        final List<String> values = Arrays.asList("qwerty", "asdfg");
        final String groupKey = "someGroup";

        final Row row = new Row.Builder()
                .depth(depth)
                .addValues(values.toArray(new String[2]))
                .groupKey(groupKey)
                .build();

        assertEquals(depth, row.getDepth());
        assertEquals(groupKey, row.getGroupKey());
        assertEquals(values, row.getValues());
    }
}
