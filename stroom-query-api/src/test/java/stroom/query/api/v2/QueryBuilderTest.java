package stroom.query.api.v2;

import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryBuilderTest {
    @Test
    public void doesBuild() {
        final String dataSourceName = "someDataSource";
        final String dataSourceType = "someDocRefType";
        final String dataSourceUuid = UUID.randomUUID().toString();

        final Query query = new Query.Builder()
                .dataSource(dataSourceType, dataSourceUuid, dataSourceName)
                .addParam("someKey0", "someValue0")
                .addParam("someKey1", "someValue1")
                .expression(ExpressionOperator.Op.AND)
                    .addTerm("fieldX", ExpressionTerm.Condition.EQUALS, "abc")
                    .addOperator(ExpressionOperator.Op.OR)
                        .addTerm("fieldA", ExpressionTerm.Condition.EQUALS, "Fred")
                        .addTerm("fieldA", ExpressionTerm.Condition.EQUALS, "Fred")
                        .end()
                    .addTerm("fieldY", ExpressionTerm.Condition.BETWEEN, "10,20")
                    .end()
                .build();

        // Examine the params
        final List<Param> params = query.getParams();
        assertNotNull(params);
        assertEquals(2, params.size());
        final Param param0 = params.get(0);
        assertEquals("someKey0", param0.getKey());
        assertEquals("someValue0", param0.getValue());
        final Param param1 = params.get(1);
        assertEquals("someKey1", param1.getKey());
        assertEquals("someValue1", param1.getValue());

        // Examine the datasource
        final DocRef dataSource = query.getDataSource();
        assertNotNull(dataSource);
        assertEquals(dataSourceName, dataSource.getName());
        assertEquals(dataSourceType, dataSource.getType());
        assertEquals(dataSourceUuid, dataSource.getUuid());

        // Examine the expression
        ExpressionOperator root = query.getExpression();
        assertNotNull(root);
        assertEquals(3, root.getChildren().size());

        ExpressionItem rootChild1 = root.getChildren().get(0);
        ExpressionItem rootChild2 = root.getChildren().get(1);
        ExpressionItem rootChild3 = root.getChildren().get(2);

        assertTrue(rootChild1 instanceof ExpressionTerm);
        assertEquals("fieldX", ((ExpressionTerm) rootChild1).getField());

        assertTrue(rootChild2 instanceof ExpressionOperator);
        ExpressionOperator child2Op = (ExpressionOperator) rootChild2;
        assertEquals(ExpressionOperator.Op.OR, child2Op.getOp());
        assertEquals(2, child2Op.getChildren().size());

        assertTrue(rootChild3 instanceof ExpressionTerm);
        assertEquals("fieldY", ((ExpressionTerm) rootChild3).getField());
    }
}
