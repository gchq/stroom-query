package stroom.query.api.v2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SortBuilderTest {
    @Test
    public void doesBuild() {
        final Sort.SortDirection direction = Sort.SortDirection.DESCENDING;
        final Integer order = 3;

        final Sort sort = new Sort.Builder<>()
                .direction(direction)
                .order(order)
                .build();

        assertEquals(direction, sort.getDirection());
        assertEquals(order, sort.getOrder());
    }
}
