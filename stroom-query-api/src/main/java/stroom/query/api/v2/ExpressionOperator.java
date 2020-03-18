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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import stroom.datasource.api.v2.BooleanField;
import stroom.datasource.api.v2.DateField;
import stroom.datasource.api.v2.DocRefField;
import stroom.datasource.api.v2.DoubleField;
import stroom.datasource.api.v2.FloatField;
import stroom.datasource.api.v2.IdField;
import stroom.datasource.api.v2.IntegerField;
import stroom.datasource.api.v2.LongField;
import stroom.datasource.api.v2.TextField;
import stroom.docref.DocRef;
import stroom.docref.HasDisplayValue;
import stroom.query.api.v2.ExpressionTerm.Condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonPropertyOrder({"op", "children"})
@JsonInclude(Include.NON_DEFAULT)
@XmlType(name = "ExpressionOperator", propOrder = {"op", "children"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(
        value = "ExpressionOperator",
        description = "A logical addOperator term in a query expression tree",
        parent = ExpressionItem.class)
public final class ExpressionOperator extends ExpressionItem {
    private static final long serialVersionUID = 6602004424564268512L;

    @XmlElement(name = "op")
    @ApiModelProperty(
            value = "The logical addOperator type",
            required = true)
    @JsonProperty
    private Op op;

    @XmlElementWrapper(name = "children")
    @XmlElements({
            @XmlElement(name = "operator", type = ExpressionOperator.class),
            @XmlElement(name = "term", type = ExpressionTerm.class)
    })
    @JsonProperty
    private List<ExpressionItem> children;

    public ExpressionOperator() {
        op = Op.AND;
    }

    @JsonCreator
    public ExpressionOperator(@JsonProperty("enabled") final Boolean enabled,
                              @JsonProperty("op") final Op op,
                              @JsonProperty("children") final List<ExpressionItem> children) {
        super(enabled);
        if (op != null) {
            this.op = op;
        } else {
            this.op = Op.AND;
        }
        this.children = children;
    }

    public ExpressionOperator(final Boolean enabled, final Op op, final ExpressionItem... children) {
        super(enabled);
        this.op = op;
        this.children = Arrays.asList(children);
    }

    public Op getOp() {
        return op;
    }

    public void setOp(final Op op) {
        this.op = op;
    }

    public List<ExpressionItem> getChildren() {
        return children;
    }

    public void setChildren(final List<ExpressionItem> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExpressionOperator that = (ExpressionOperator) o;
        return op == that.op &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), op, children);
    }

    @Override
    void append(final StringBuilder sb, final String pad, final boolean singleLine) {
        if (isEnabled()) {
            if (!singleLine && sb.length() > 0) {
                sb.append("\n");
                sb.append(pad);
            }

            sb.append(op);
            if (singleLine) {
                sb.append(" {");
            }

            if (children != null) {
                final String padding = pad + "  ";
                boolean firstItem = true;
                for (final ExpressionItem expressionItem : children) {
                    if (expressionItem.isEnabled()) {
                        if (singleLine && !firstItem) {
                            sb.append(", ");
                        }

                        expressionItem.append(sb, padding, singleLine);
                        firstItem = false;
                    }
                }
            }

            if (singleLine) {
                sb.append("}");
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // START QUICK EQUALS CONSTRUCTORS
    ////////////////////////////////////////////////////////////////////////
    public static ExpressionOperator equals(final String field, final String value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final BooleanField field, final boolean value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final DateField field, final String value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final DocRefField field, final DocRef value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final IdField field, final long value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final IntegerField field, final int value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final LongField field, final long value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final FloatField field, final float value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final DoubleField field, final double value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final TextField field, final String value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final String field, final DocRef value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }
    ////////////////////////////////////////////////////////////////////////
    // END QUICK EQUALS CONSTRUCTORS
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // START UTILITY METHODS
    ////////////////////////////////////////////////////////////////////////
    public int termCount() {
        return terms(null).size();
    }

    public List<String> fields() {
        return terms(null).stream().map(ExpressionTerm::getField).collect(Collectors.toList());
    }

    public List<String> fields(final String field) {
        return terms(field).stream().map(ExpressionTerm::getField).collect(Collectors.toList());
    }

    public List<String> values() {
        return terms(null).stream().map(ExpressionTerm::getValue).collect(Collectors.toList());
    }

    public List<String> values(final String field) {
        return terms(field).stream().map(ExpressionTerm::getValue).collect(Collectors.toList());
    }

    public List<ExpressionTerm> terms(final String field) {
        final List<ExpressionTerm> terms = new ArrayList<>();
        addTerms(this, field, terms);
        return terms;
    }

    private void addTerms(final ExpressionOperator expressionOperator, final String field, final List<ExpressionTerm> terms) {
        if (expressionOperator != null && expressionOperator.isEnabled() && !Op.NOT.equals(expressionOperator.getOp())) {
            for (final ExpressionItem item : expressionOperator.getChildren()) {
                if (item.isEnabled()) {
                    if (item instanceof ExpressionTerm) {
                        final ExpressionTerm expressionTerm = (ExpressionTerm) item;
                        if ((field == null || field.equals(expressionTerm.getField())) && expressionTerm.getValue() != null && expressionTerm.getValue().length() > 0) {
                            terms.add(expressionTerm);
                        }
                    } else if (item instanceof ExpressionOperator) {
                        addTerms((ExpressionOperator) item, field, terms);
                    }
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////
    // END UTILITY METHODS
    ////////////////////////////////////////////////////////////////////////

    public enum Op implements HasDisplayValue {
        AND("AND"), OR("OR"), NOT("NOT");

        private final String displayValue;

        Op(final String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }
    }

    /**
     * Builder for constructing a {@link ExpressionOperator}
     */
    public static class Builder
            extends ExpressionItem.Builder<ExpressionOperator, Builder> {
        private Op op;

        private List<ExpressionItem> children = new ArrayList<>();

        /**
         * No args constructor, defaults to using AND as the operator.
         */
        public Builder() {
            this(Op.AND);
        }

        @Override
        protected ExpressionOperator.Builder self() {
            return this;
        }

        @Override
        public ExpressionOperator build() {
            return new ExpressionOperator(isEnabled(), op, children);
        }

        /**
         * @param op Set the logical operator to apply to all the children items
         */
        public Builder(final Op op) {
            op(op);
        }

        /**
         * Construct a builder setting enabled and op
         *
         * @param enabled Is this Expression Operator enabled
         * @param op      The op
         */
        public Builder(final boolean enabled, final Op op) {
            super(enabled);
            op(op);
        }

        /**
         * Changes the operator of this builder
         *
         * @param op The operator to set for this builder
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder op(final Op op) {
            Objects.requireNonNull(op, "Attempt to set null operation");
            this.op = op;
            return this;
        }

        /**
         * Adds an {@link ExpressionOperator} to this builder
         *
         * @param item The expression item to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder addOperator(ExpressionOperator item) {
            return addOperators(item);
        }

        /**
         * Adds an {@link ExpressionOperator} to this builder
         *
         * @param items The expression items to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder addOperators(ExpressionOperator... items) {
            return addOperators(Arrays.asList(items));
        }

        /**
         * Adds an {@link ExpressionOperator} to this builder
         *
         * @param items The expression items to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder addOperators(Collection<ExpressionOperator> items) {
            this.children.addAll(items);
            return this;
        }

        /**
         * Adds an {@link ExpressionTerm} to this builder
         *
         * @param item The expression item to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder addTerm(ExpressionTerm item) {
            return addTerms(item);
        }

        /**
         * Adds an {@link ExpressionTerm} to this builder
         *
         * @param items The expression items to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder addTerms(ExpressionTerm... items) {
            return addTerms(Arrays.asList(items));
        }

        /**
         * Adds an {@link ExpressionTerm} to this builder
         *
         * @param items The expression items to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder addTerms(Collection<ExpressionTerm> items) {
            this.children.addAll(items);
            return this;
        }

        /**
         * A convenience function for adding terms in one go, the parameters should read fairly clearly
         *
         * @param field     The field name
         * @param condition The condition to apply to the value
         * @param value     The value
         * @return this builder, with the completed term added.
         */
        public Builder addTerm(final String field,
                               final ExpressionTerm.Condition condition,
                               final String value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field)
                            .condition(condition)
                            .value(value)
                            .build());
        }

        /**
         * A convenience function for adding terms in one go, the parameters should read fairly clearly
         *
         * @param field     The field name
         * @param condition The condition to apply to the value
         * @param value     The value
         * @return this builder, with the completed term added.
         */
        public Builder addTerm(final BooleanField field,
                               final ExpressionTerm.Condition condition,
                               final boolean value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(String.valueOf(value))
                            .build());
        }

        public Builder addTerm(final DateField field,
                               final ExpressionTerm.Condition condition,
                               final String value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(value)
                            .build());
        }

        public Builder addTerm(final DocRefField field,
                               final ExpressionTerm.Condition condition,
                               final DocRef docRef) {
            return addTerm(new ExpressionTerm.Builder()
                    .field(field.getName())
                    .condition(condition)
                    .docRef(docRef)
                    .build());
        }

        public Builder addTerm(final IdField field,
                               final ExpressionTerm.Condition condition,
                               final long value) {
            return addTerm(new ExpressionTerm.Builder()
                    .field(field.getName())
                    .condition(condition)
                    .value(String.valueOf(value))
                    .build());
        }

        public Builder addTerm(final IntegerField field,
                               final ExpressionTerm.Condition condition,
                               final int value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(String.valueOf(value))
                            .build());
        }

        public Builder addTerm(final LongField field,
                               final ExpressionTerm.Condition condition,
                               final long value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(String.valueOf(value))
                            .build());
        }

        public Builder addTerm(final FloatField field,
                               final ExpressionTerm.Condition condition,
                               final float value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(String.valueOf(value))
                            .build());
        }

        public Builder addTerm(final DoubleField field,
                               final ExpressionTerm.Condition condition,
                               final double value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(String.valueOf(value))
                            .build());
        }

        public Builder addTerm(final TextField field,
                               final ExpressionTerm.Condition condition,
                               final String value) {
            return addTerm(
                    new ExpressionTerm.Builder()
                            .field(field.getName())
                            .condition(condition)
                            .value(value)
                            .build());
        }

        public Builder addTerm(final String field,
                               final ExpressionTerm.Condition condition,
                               final DocRef docRef) {
            return addTerm(new ExpressionTerm.Builder()
                    .field(field)
                    .condition(condition)
                    .docRef(docRef)
                    .build());
        }
    }
}
