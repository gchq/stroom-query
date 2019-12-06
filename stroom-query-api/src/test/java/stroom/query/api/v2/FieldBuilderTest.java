package stroom.query.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FieldBuilderTest {
    @Test
    public void doesBuild() {
        final String name = "someName";
        final Integer group = 57;
        final Integer sortOrder = 2;
        final Sort.SortDirection sortDirection = Sort.SortDirection.DESCENDING;
        final String expression = "someExpression";
        final String filterExcludes = "stuff to exclude **";
        final String filterIncludes = "stuff to include &&";
        final Integer numberFormatDecimalPlaces = 5;
        final Boolean numberFormatUseSeperator = true;

        final Field field = new Field.Builder()
                .id(name)
                .name(name)
                .expression(expression)
                .sort(new Sort.Builder()
                    .order(sortOrder)
                    .direction(sortDirection)
                    .build())
                .filter(new Filter.Builder()
                    .includes(filterIncludes)
                    .excludes(filterExcludes)
                    .build())
                .format(new Format.Builder()
                        .number(new NumberFormat.Builder()
                                .decimalPlaces(numberFormatDecimalPlaces)
                                .useSeparator(numberFormatUseSeperator)
                                .build())
                        .build())
                .group(group)
                .build();

        assertEquals(name, field.getName());
        assertEquals(expression, field.getExpression());
        assertEquals(group, field.getGroup());

        assertNotNull(field.getSort());
        assertEquals(sortOrder, field.getSort().getOrder());
        assertEquals(sortDirection, field.getSort().getDirection());

        assertNotNull(field.getFilter());
        assertEquals(filterExcludes, field.getFilter().getExcludes());
        assertEquals(filterIncludes, field.getFilter().getIncludes());

        assertNotNull(field.getFormat());
        assertEquals(Format.Type.NUMBER, field.getFormat().getType());
        assertNotNull(field.getFormat().getNumberFormat());
        assertNull(field.getFormat().getDateTimeFormat());
        assertEquals(numberFormatDecimalPlaces, field.getFormat().getNumberFormat().getDecimalPlaces());
        assertEquals(numberFormatUseSeperator, field.getFormat().getNumberFormat().getUseSeparator());
    }
}
