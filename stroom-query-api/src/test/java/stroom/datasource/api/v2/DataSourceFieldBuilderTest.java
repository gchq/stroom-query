package stroom.datasource.api.v2;

import org.junit.jupiter.api.Test;
import stroom.query.api.v2.ExpressionTerm;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceFieldBuilderTest {
    @Test
    void doesBuild() {
        // Given
        final String name = "someField";
        final ExpressionTerm.Condition condition = ExpressionTerm.Condition.BETWEEN;

        // When
        final TextField field = new TextField(name, true, List.of(condition));

        // Then
        assertThat(field.getConditions()).hasSize(1);
        assertThat(field.getConditions().get(0)).isEqualTo(condition);
        assertThat(field.getName()).isEqualTo(name);
    }
}
