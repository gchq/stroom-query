package stroom.query.api.v2;

import org.junit.Assert;
import org.junit.Test;

public class ExpressionBuilderTest {
    @Test
    public void doesBuild() throws Exception {
        ExpressionOperator root = new ExpressionOperator.Builder<>()
                .op(ExpressionOperator.Op.AND)
                    .addTerm()
                        .field("fieldX")
                        .condition(ExpressionTerm.Condition.EQUALS)
                        .value("abc")
                        .end()
                    .addOperator()
                        .op(ExpressionOperator.Op.OR)
                            .addTerm()
                                .field("fieldA")
                                .condition(ExpressionTerm.Condition.EQUALS)
                                .value("Fred")
                            .end()
                            .addTerm()
                                .field("fieldA")
                                .condition(ExpressionTerm.Condition.EQUALS)
                                .value("Fred")
                            .end()
                        .end()
                    .addTerm()
                        .field("fieldY")
                        .condition(ExpressionTerm.Condition.BETWEEN)
                        .value("10,20")
                    .end()
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
