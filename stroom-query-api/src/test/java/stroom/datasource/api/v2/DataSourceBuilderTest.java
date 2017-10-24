package stroom.datasource.api.v2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataSourceBuilderTest {
    @Test
    public void doeBuild() {
        // Given
        final String field0 = "field0";
        final String field1 = "field1";
        final String field2 = "field2";

        // When
        final DataSource dataSource = new DataSource.Builder<>()
                .addField()
                    .name(field0)
                    .end()
                .addField()
                    .name(field1)
                    .end()
                .addField()
                    .name(field2)
                    .end()
                .build();

        // Then
        assertEquals(3, dataSource.getFields().size());
        assertEquals(field0, dataSource.getFields().get(0).getName());
        assertEquals(field1, dataSource.getFields().get(1).getName());
        assertEquals(field2, dataSource.getFields().get(2).getName());
    }
}
