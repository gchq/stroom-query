package stroom.datasource.api.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataSourceBuilderTest {
    @Test
    public void doeBuild() {
        // Given
        final String field0 = "field0";
        final String field1 = "field1";
        final String field2 = "field2";

        // When
        final DataSource dataSource = new DataSource.Builder()
                .addFields(new DataSourceField.Builder()
                        .name(field0)
                        .build())
                .addFields(new DataSourceField.Builder()
                        .name(field1)
                        .build())
                .addFields(new DataSourceField.Builder()
                        .name(field2)
                        .build())
                .build();

        // Then
        assertEquals(3, dataSource.getFields().size());
        assertEquals(field0, dataSource.getFields().get(0).getName());
        assertEquals(field1, dataSource.getFields().get(1).getName());
        assertEquals(field2, dataSource.getFields().get(2).getName());
    }
}
