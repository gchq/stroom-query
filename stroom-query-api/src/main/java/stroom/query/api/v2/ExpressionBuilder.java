/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.api.v2;

import stroom.query.api.v2.ExpressionOperator.Op;
import stroom.query.api.v2.ExpressionTerm.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder class for constructing a tree of {@link ExpressionItem expressionItems}. Builders can be nested as follows:
 * <pre>
 ExpressionOperator root = new ExpressionBuilder(Op.AND)
     .addTerm("fieldX", Condition.EQUALS, "abc")
     .addOperator(Op.OR)
         .addTerm("fieldA", Condition.EQUALS, "Fred")
         .addTerm("fieldA", Condition.EQUALS, "Fred")
         .end()
     .addTerm("fieldY", Condition.BETWEEN, "10,20")
     .build();
 * </pre>
 */
public final class ExpressionBuilder {

    private final Boolean enabled;
    private final Op op;
    private final List<Object> children = new ArrayList<>();
    private final ExpressionBuilder parentBuilder;

    /**
     * Create a builder with an enabled 'AND' operator as its root item
     */
    public ExpressionBuilder() {
        this(null, Op.AND);
    }

    /**
     * Create a builder with the supplied {@link ExpressionOperator} type as its root item. The root item will be
     * enabled
     * @param op The {@link Op operator type} of the root operator
     */
    public ExpressionBuilder(final Op op) {
        this(null, op);
    }

    /**
     * Create a builder with the supplied {@link Op} as the type of its root {@link ExpressionOperator}.
     * The root item will be enabled.
     * @param enabled The enabled state of the root operator item. A value of null is take to mean enabled
     * @param op The {@link Op operator type} of the root operator
     */
    public ExpressionBuilder(final Boolean enabled, final Op op) {
        this(enabled, op, null);
    }

    private ExpressionBuilder(final Boolean enabled, final Op op, final ExpressionBuilder parentBuilder) {
        this.enabled = enabled;
        this.op = op;
        this.parentBuilder = parentBuilder;
    }

    /**
     * Add a value term to the builder, e.g fieldX|BETWEEN|10,100
     * Term is enabled by default.
     * Not all conditions are supported by all data sources
     * @param field The field in the data source that is being evaluated
     * @param condition The {@link Condition} of the predicate
     * @param value The value for the predicate
     * @return This builder instance that the term is being added to
     */
    public ExpressionBuilder addTerm(final String field,
                                     final Condition condition,
                                     final String value) {

        return addTerm(null, field, condition, value, null);
    }

    /**
     * Add a value term to the builder, e.g fieldX|BETWEEN|10,100
     * Not all conditions are supported by all data sources
     * @param enabled Sets the terms state to enabled if true or null, disabled if false
     * @param field The field in the data source that is being evaluated
     * @param condition The {@link Condition} of the predicate
     * @param value The value for the predicate
     * @return This builder instance that the term is being added to
     */
    public ExpressionBuilder addTerm(final Boolean enabled,
                                     final String field,
                                     final Condition condition,
                                     final String value) {

        return addTerm(enabled, field, condition, value, null);
    }

    /**
     * Add a dictionary term to the builder, e.g fieldX|IN_DICTIONARY|docRefToDictionaryY
     * Term is enabled by default. Not all data sources support dictionary terms and only certain
     * conditions are supported for a dictionary term.
     * @param field The field in the data source that is being evaluated against the dictionary
     * @param condition The {@link Condition} of the predicate
     * @param dictionary The {@link DocRef} of the dictionary entity in stroom
     * @return This builder instance that the term is being added to
     */
    public ExpressionBuilder addDictionaryTerm(final String field,
                                               final Condition condition,
                                               final DocRef dictionary) {

        return addTerm(null, field, condition, null, dictionary);
    }

    /**
     * Add a dictionary term to the builder, e.g fieldX|IN_DICTIONARY|docRefToDictionaryY
     * Term is enabled by default. Not all data sources support dictionary terms and only certain
     * conditions are supported for a dictionary term.
     * @param enabled Sets the terms state to enabled if true or null, disabled if false
     * @param field The field in the data source that is being evaluated against the dictionary
     * @param condition The {@link Condition} of the predicate
     * @param dictionary The {@link DocRef} of the dictionary entity in stroom
     * @return This builder instance that the term is being added to
     */
    public ExpressionBuilder addDictionaryTerm(final Boolean enabled,
                                               final String field,
                                               final Condition condition,
                                               final DocRef dictionary) {

        return addTerm(enabled, field, condition, null, dictionary);
    }

    //TODO should this be public? Better to use an addTerm for addDictotionaryTerm from above?
    public ExpressionBuilder addTerm(final Boolean enabled,
                                     final String field,
                                     final Condition condition,
                                     final String value,
                                     final DocRef dictionary) {

        children.add(new ExpressionTerm(enabled, field, condition, value, dictionary));
        return this;
    }

    /**
     * Adds an {@link ExpressionTerm} to this builder
     * @param term The {@link ExpressionTerm} to add to this builder
     * @return This builder instance that the term is being added to
     */
    public ExpressionBuilder addTerm(final ExpressionTerm term) {
        if (term != null) {
            addTerm(term.getEnabled(), term.getField(), term.getCondition(), term.getValue(), term.getDictionary());
        }

        return this;
    }

    /**
     * Adds an {@link ExpressionOperator} of type {@link Op} to this expression builder.
     * The {@link ExpressionOperator} will be enabled by default.
     * @param op The type of the {@link ExpressionOperator} to add
     * @return A new builder instance for the added {@link ExpressionOperator} to allow adding items to this
     * {@link ExpressionOperator}
     */
    public ExpressionBuilder addOperator(final Op op) {
        return addOperator(null, op);
    }

    /**
     * Adds an {@link ExpressionOperator} of type {@link Op} to this expression builder.
     * The {@link ExpressionOperator} will be enabled by default.
     * @param enabled Sets the terms state to enabled if true or null, disabled if false
     * @param op The type of the {@link ExpressionOperator} to add
     * @return A new builder instance for the added {@link ExpressionOperator} to allow adding items to this
     * {@link ExpressionOperator}
     */
    public ExpressionBuilder addOperator(final Boolean enabled, final Op op) {
        final ExpressionBuilder builder = new ExpressionBuilder(enabled, op, this);
        children.add(builder);
        return builder;
    }

    /**
     * Adds the passed {@link ExpressionOperator} tree to this builder
     * @param operator The {@link ExpressionOperator} to add
     * @return A new builder instance for the added {@link ExpressionOperator}
     */
    public ExpressionBuilder addOperator(final ExpressionOperator operator) {
        if (operator != null) {
            final ExpressionBuilder builder = addOperator(operator.getEnabled(), operator.getOp());

            if (operator.getChildren() != null) {
                for (final ExpressionItem child : operator.getChildren()) {
                    if (child instanceof ExpressionOperator) {
                        builder.addOperator((ExpressionOperator) child);
                    } else if (child instanceof ExpressionTerm) {
                        builder.addTerm((ExpressionTerm) child);
                    }
                }
            }

            return builder;
        }

        return null;
    }

    /**
     * Builds the expression tree, returning the root {@link ExpressionOperator}
     * @return The {@link ExpressionOperator} that is the root of the expression tree
     */
    public ExpressionOperator build() {
        if (children.size() == 0) {
            return new ExpressionOperator(enabled, op, Arrays.asList());
        }

        final List<ExpressionItem> list = new ArrayList<>(children.size());
        for (final Object object : children) {
            if (object instanceof ExpressionBuilder) {
                list.add(((ExpressionBuilder) object).build());
            } else if (object instanceof ExpressionTerm) {
                list.add((ExpressionTerm) object);
            }
        }

        return new ExpressionOperator(enabled, op, list);
    }

    /**
     * If this builder instance was created as a child of another builder instance then this method returns the
     * parent builder instance so you can continue to add items to the parent. See the example in
     * {@link ExpressionBuilder}
     * @return The parent builder instance if there is one, else returns this builder instance
     */
    public ExpressionBuilder end() {
        if (parentBuilder != null) {
            return parentBuilder;
        } else {
            return this;
        }
    }
}