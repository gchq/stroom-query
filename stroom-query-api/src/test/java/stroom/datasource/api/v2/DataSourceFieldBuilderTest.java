package stroom.datasource.api.v2;

import org.junit.Test;
import stroom.query.api.v2.ExpressionTerm;

import static org.junit.Assert.assertEquals;

public class DataSourceFieldBuilderTest {
    @Test
    public void doesBuild() {
        // Given
        final String name = "someField";
        final ExpressionTerm.Condition condition = ExpressionTerm.Condition.BETWEEN;
        final DataSourceField.DataSourceFieldType type = DataSourceField.DataSourceFieldType.NUMERIC_FIELD;

        // When
        final DataSourceField field = new DataSourceField.Builder()
                    .addConditions(condition)
                    .name(name)
                    .queryable(true)
                    .type(type)
                .build();

        // Then
        assertEquals(1, field.getConditions().size());
        assertEquals(condition, field.getConditions().get(0));
        assertEquals(name, field.getName());
        assertEquals(type, field.getType());
    }
}
