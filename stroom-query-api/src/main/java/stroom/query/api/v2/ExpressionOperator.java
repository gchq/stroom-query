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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import stroom.util.shared.HasDisplayValue;
import stroom.util.shared.OwnedBuilder;

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

@JsonPropertyOrder({"op", "children"})
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
    private Op op = Op.AND;

    @XmlElementWrapper(name = "children")
    @XmlElements({
            @XmlElement(name = "addOperator", type = ExpressionOperator.class),
            @XmlElement(name = "term", type = ExpressionTerm.class)
    })
    @ApiModelProperty(
            required = false)
    private List<ExpressionItem> children;

    private ExpressionOperator() {
    }

    public ExpressionOperator(final Boolean enabled, final Op op, final List<ExpressionItem> children) {
        super(enabled);
        this.op = op;
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

    public List<ExpressionItem> getChildren() {
        return children;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final ExpressionOperator that = (ExpressionOperator) o;

        if (op != that.op) return false;
        return children != null ? children.equals(that.children) : that.children == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (op != null ? op.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    void append(final StringBuilder sb, final String pad, final boolean singleLine) {
        if (enabled()) {
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
                    if (expressionItem.enabled()) {
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
     *
     * @param <OwningBuilder> The class of the popToWhenComplete builder, allows nested building
     */
    public static abstract class ABuilder<
                    OwningBuilder extends OwnedBuilder,
                    CHILD_CLASS extends ABuilder<OwningBuilder, ?>>
            extends ExpressionItem.Builder<OwningBuilder, ExpressionOperator, CHILD_CLASS> {
        private final Op op;

        private List<ExpressionItem> children = new ArrayList<>();

        /**
         * No args constructor, defaults to using AND as the operator.
         */
        public ABuilder() {
            this(Op.AND);
        }

        /**
         * @param value Set the logical operator to apply to all the children items
         */
        public ABuilder(final Op value) {
            this.op = value;
        }

        /**
         * Construct a builder setting enabled and op
         * @param enabled Is this Expression Operator enabled
         * @param op The op
         */
        public ABuilder(final Boolean enabled, final Op op) {
            super(enabled);
            this.op = op;
        }

        /**
         * Adds an {@link ExpressionTerm} to this builder
         * @param items The expression items to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS addOperators(ExpressionItem...items) {
            return addOperators(Arrays.asList(items));
        }

        /**
         * Adds an {@link ExpressionTerm} to this builder
         * @param items The expression items to add as children
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS addOperators(Collection<ExpressionItem> items) {
            this.children.addAll(items);
            return self();
        }

        /**
         * Begin construction of a new child {@link ExpressionOperator}
         * @param op The logical operator to apply
         * @return A new operator builder, configured to pop back to this builder when complete
         */
        public OBuilder<CHILD_CLASS> addOperator(final Op op) {
            return new OBuilder<CHILD_CLASS>(op)
                    .popToWhenComplete(self(), this::addOperators);
        }

        /**
         * Begin construction of a new child {@link ExpressionOperator}
         * @param enabled Indicates if the operator is enabled
         * @param op The logical operator to apply
         * @return A new operator builder, configured to pop back to this builder when complete
         */
        public OBuilder<CHILD_CLASS> addOperator(final Boolean enabled, final Op op) {
            return new OBuilder<CHILD_CLASS>(enabled, op)
                    .popToWhenComplete(self(), this::addOperators);
        }

        /**
         * Begin construction of a new child {@link ExpressionTerm}
         * @return A new term builder, configured to pop back to this builder when complete
         */
        public ExpressionTerm.OBuilder<CHILD_CLASS> addTerm() {
            return new ExpressionTerm.OBuilder<CHILD_CLASS>()
                    .popToWhenComplete(self(), this::addOperators);
        }

        /**
         * A convenience function for adding terms in one go, the parameters should read fairly clearly
         * @param field The field name
         * @param condition The condition to apply to the valud
         * @param value The value
         * @return This builder, with the completed term added.
         */
        public CHILD_CLASS addTerm(final String field,
                                              final ExpressionTerm.Condition condition,
                                              final String value) {
            return addTerm().field(field).condition(condition).value(value).end();
        }

        /**
         * A convenience function for adding dictionary terms in one go, the parameters should read fairly clearly
         * @param field The field name
         * @param condition The condition to apply to the valud
         * @param dictionary The dictionary
         * @return This builder, with the completed term added.
         */
        public CHILD_CLASS addDictionaryTerm(final String field,
                                             final ExpressionTerm.Condition condition,
                                             final DocRef dictionary) {
            return addTerm().field(field).condition(condition).dictionary(dictionary).end();
        }

        @Override
        protected ExpressionOperator pojoBuild() {
            return new ExpressionOperator(getEnabled(), op, children);
        }
    }

    /**
     * An expression operator that is owned by some parent builder.
     *
     * @param <OwningBuilder> The class of the owning builder
     */
    public static final class OBuilder<OwningBuilder extends OwnedBuilder>
            extends ABuilder<OwningBuilder, OBuilder<OwningBuilder>> {

        public OBuilder() {
            super(Op.AND);
        }
        public OBuilder(final Op value) {
            super(value);
        }
        public OBuilder(final Boolean enabled, final Op op) {
            super(enabled, op);
        }

        @Override
        public OBuilder self() {
            return this;
        }
    }

    /**
     * An expression operator that is created independently of any builder tree
     */
    public static final class Builder extends ABuilder<Builder, Builder> {

        public Builder() {
            super(Op.AND);
        }
        public Builder(final Op value) {
            super(value);
        }
        public Builder(final Boolean enabled, final Op op) {
            super(enabled, op);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}
