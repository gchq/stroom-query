package stroom.datasource.api.v2;

import org.junit.jupiter.api.Test;
import stroom.query.api.v2.ExpressionTerm;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceFieldBuilderTest {
    @Test
    void doesBuild() {
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
        assertThat(field.getConditions()).hasSize(1);
        assertThat(field.getConditions().get(0)).isEqualTo(condition);
        assertThat(field.getName()).isEqualTo(name);
        assertThat(field.getType()).isEqualTo(type);
    }
}
