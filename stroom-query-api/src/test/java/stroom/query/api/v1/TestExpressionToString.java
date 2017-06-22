/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.api.v1;

import org.junit.Assert;
import org.junit.Test;
import stroom.query.api.v1.ExpressionOperator.Op;
import stroom.query.api.v1.ExpressionTerm.Condition;

public class TestExpressionToString {
    @Test
    public void TestSingleLine() {
        ExpressionBuilder builder = new ExpressionBuilder(false, Op.AND);
        single("", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        single("AND {}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm("field", Condition.EQUALS, "value");
        single("AND {field = value}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm(null, Condition.EQUALS, null);
        single("AND { = }", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm("field1", Condition.EQUALS, "value1");
        builder.addTerm("field2", Condition.EQUALS, "value2");
        single("AND {field1 = value1, field2 = value2}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(Op.AND);
        single("AND {AND {}}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(false, Op.AND);
        single("AND {}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm("field", Condition.EQUALS, "value");
        builder.addOperator(Op.AND);
        single("AND {field = value, AND {}}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(Op.AND);
        builder.addTerm("field", Condition.EQUALS, "value");
        single("AND {AND {}, field = value}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(Op.AND).addTerm("nestedField", Condition.EQUALS, "nestedValue");
        builder.addTerm("field", Condition.EQUALS, "value");
        single("AND {AND {nestedField = nestedValue}, field = value}", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        ExpressionBuilder nested = builder.addOperator(Op.AND);
        nested.addTerm("nestedField1", Condition.EQUALS, "nestedValue1");
        nested.addTerm("nestedField2", Condition.EQUALS, "nestedValue2");
        builder.addTerm("field", Condition.EQUALS, "value");
        single("AND {AND {nestedField1 = nestedValue1, nestedField2 = nestedValue2}, field = value}", builder);
    }

    @Test
    public void TestMultiLine() {
        ExpressionBuilder builder = new ExpressionBuilder(false, Op.AND);
        multi("", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        multi("AND", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm("field", Condition.EQUALS, "value");
        multi("AND\n  field = value", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm(null, Condition.EQUALS, null);
        multi("AND\n   = ", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm("field1", Condition.EQUALS, "value1");
        builder.addTerm("field2", Condition.EQUALS, "value2");
        multi("AND\n  field1 = value1\n  field2 = value2", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(Op.AND);
        multi("AND\n  AND", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(false, Op.AND);
        multi("AND", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addTerm("field", Condition.EQUALS, "value");
        builder.addOperator(Op.AND);
        multi("AND\n  field = value\n  AND", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(Op.AND);
        builder.addTerm("field", Condition.EQUALS, "value");
        multi("AND\n  AND\n  field = value", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        builder.addOperator(Op.AND).addTerm("nestedField", Condition.EQUALS, "nestedValue");
        builder.addTerm("field", Condition.EQUALS, "value");
        multi("AND\n  AND\n    nestedField = nestedValue\n  field = value", builder);

        builder = new ExpressionBuilder(true, Op.AND);
        ExpressionBuilder nested = builder.addOperator(Op.AND);
        nested.addTerm("nestedField1", Condition.EQUALS, "nestedValue1");
        nested.addTerm("nestedField2", Condition.EQUALS, "nestedValue2");
        builder.addTerm("field", Condition.EQUALS, "value");
        multi("AND\n  AND\n    nestedField1 = nestedValue1\n    nestedField2 = nestedValue2\n  field = value", builder);
    }

    private void single(final String expected, final ExpressionBuilder builder) {
        final String actual = builder.build().toString();
        System.out.println(actual);
        Assert.assertEquals(expected, actual);
    }

    private void multi(final String expected, final ExpressionBuilder builder) {
        final String actual = builder.build().toMultiLineString();
        System.out.println(actual);
        Assert.assertEquals(expected, actual);
    }
}
