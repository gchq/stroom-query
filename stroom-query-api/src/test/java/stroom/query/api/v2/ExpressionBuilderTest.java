package stroom.query.api.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionBuilderTest {
    @Test
    public void doesBuild() {
        ExpressionOperator root = new ExpressionOperator.Builder(ExpressionOperator.Op.AND)
                    .addTerm("fieldX", ExpressionTerm.Condition.EQUALS, "abc")
                    .addOperator(new ExpressionOperator.Builder(ExpressionOperator.Op.OR)
                        .addTerm("fieldA", ExpressionTerm.Condition.EQUALS, "Fred")
                        .addTerm("fieldA", ExpressionTerm.Condition.EQUALS, "Fred")
                        .build())
                    .addTerm("fieldY", ExpressionTerm.Condition.BETWEEN, "10,20")
                .build();

        Assertions.assertEquals(3, root.getChildren().size());

        ExpressionItem rootChild1 = root.getChildren().get(0);
        ExpressionItem rootChild2 = root.getChildren().get(1);
        ExpressionItem rootChild3 = root.getChildren().get(2);

        Assertions.assertTrue(rootChild1 instanceof ExpressionTerm);
        Assertions.assertEquals("fieldX", ((ExpressionTerm) rootChild1).getField());

        Assertions.assertTrue(rootChild2 instanceof ExpressionOperator);
        ExpressionOperator child2Op = (ExpressionOperator) rootChild2;
        Assertions.assertEquals(ExpressionOperator.Op.OR, child2Op.getOp());
        Assertions.assertEquals(2, child2Op.getChildren().size());

        Assertions.assertTrue(rootChild3 instanceof ExpressionTerm);
        Assertions.assertEquals("fieldY", ((ExpressionTerm) rootChild3).getField());
    }
}
