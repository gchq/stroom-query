package stroom.query.api.v2;

import org.junit.Assert;
import org.junit.Test;

public class TestExpressionBuilder {

    @Test
    public void testEnd() throws Exception {
        ExpressionOperator root = new ExpressionBuilder(ExpressionOperator.Op.AND)
                .addTerm("fieldX", ExpressionTerm.Condition.EQUALS, "abc")
                .addOperator(ExpressionOperator.Op.OR)
                    .addTerm("fieldA", ExpressionTerm.Condition.EQUALS, "Fred")
                    .addTerm("fieldA", ExpressionTerm.Condition.EQUALS, "Fred")
                    .end()
                .addTerm("fieldY", ExpressionTerm.Condition.BETWEEN, "10,20")
                .end() //not needed here but testing to make sure it doesn't break it
                .build();

        Assert.assertEquals(3, root.getChildren().size());

        ExpressionItem rootChild1 = root.getChildren().get(0);
        ExpressionItem rootChild2 = root.getChildren().get(1);
        ExpressionItem rootChild3 = root.getChildren().get(2);

        Assert.assertTrue(rootChild1 instanceof ExpressionTerm);
        Assert.assertEquals("fieldX", ((ExpressionTerm) rootChild1).getField());

        Assert.assertTrue(rootChild2 instanceof ExpressionOperator);
        ExpressionOperator child2Op = (ExpressionOperator) rootChild2;
        Assert.assertEquals(ExpressionOperator.Op.OR, child2Op.getOp());
        Assert.assertEquals(2, child2Op.getChildren().size());

        Assert.assertTrue(rootChild3 instanceof ExpressionTerm);
        Assert.assertEquals("fieldY", ((ExpressionTerm) rootChild3).getField());
    }
}