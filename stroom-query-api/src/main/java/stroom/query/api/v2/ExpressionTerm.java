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
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.List;

@JsonPropertyOrder({"field", "condition", "value", "dictionary"})
@XmlType(name = "ExpressionTerm", propOrder = {"field", "condition", "value", "dictionary"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(
        value = "ExpressionTerm",
        description = "A predicate term in a query expression tree",
        parent = ExpressionItem.class)
public final class ExpressionTerm extends ExpressionItem {
    private static final long serialVersionUID = 9035311895540457146L;

    @XmlElement
    @ApiModelProperty(
            value = "The name of the field that is being evaluated in this predicate term",
            required = true)
    private String field;

    @XmlElement
    @ApiModelProperty(
            value = "The condition of the predicate term",
            required = true)
    private Condition condition;

    @XmlElement
    @ApiModelProperty(
            value = "The value that the field value is being evaluated against. Not required if a dictionary is supplied",
            required = false)
    private String value;

    @XmlElement
    @ApiModelProperty(
            value = "The DocRef for the dictionary that this predicate is using for its evaluation",
            required = true)
    private DocRef dictionary;

    private ExpressionTerm() {
    }

    public ExpressionTerm(final String field, final Condition condition, final String value) {
        this(null, field, condition, value, null);
    }

    public ExpressionTerm(final String field, final Condition condition, final DocRef dictionary) {
        this(null, field, condition, null, dictionary);
    }

    public ExpressionTerm(final Boolean enabled,
                          final String field,
                          final Condition condition,
                          final String value,
                          final DocRef dictionary) {
        super(enabled);
        this.field = field;
        this.condition = condition;
        this.value = value;
        this.dictionary = dictionary;
    }

    public String getField() {
        return field;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getValue() {
        return value;
    }

    public DocRef getDictionary() {
        return dictionary;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final ExpressionTerm that = (ExpressionTerm) o;

        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (condition != that.condition) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return dictionary != null ? dictionary.equals(that.dictionary) : that.dictionary == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (field != null ? field.hashCode() : 0);
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (dictionary != null ? dictionary.hashCode() : 0);
        return result;
    }

    @Override
    void append(final StringBuilder sb, final String pad, final boolean singleLine) {
        if (enabled()) {
            if (!singleLine && sb.length() > 0) {
                sb.append("\n");
                sb.append(pad);
            }

            if (field != null) {
                sb.append(field);
            }
            sb.append(" ");
            if (condition != null) {
                sb.append(condition.getDisplayValue());
            }
            sb.append(" ");
            if (Condition.IN_DICTIONARY.equals(condition)) {
                if (dictionary != null) {
                    sb.append(dictionary.getUuid());
                }
            } else if (value != null) {
                sb.append(value);
            }
        }
    }

    public enum Condition implements HasDisplayValue {
        CONTAINS("contains"),
        EQUALS("="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL_TO(">="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL_TO("<="),
        BETWEEN("between"),
        IN("in"),
        IN_DICTIONARY("in dictionary");

        public static final List<Condition> SIMPLE_CONDITIONS = Arrays.asList(
                EQUALS,
                GREATER_THAN,
                GREATER_THAN_OR_EQUAL_TO,
                LESS_THAN,
                LESS_THAN_OR_EQUAL_TO,
                BETWEEN);

        public static final String IN_CONDITION_DELIMITER = ",";

        private final String displayValue;

        Condition(final String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }
    }

    /**
     * Builder for constructing a {@link ExpressionTerm}
     *
     * @param <OwningBuilder> The class of the popToWhenComplete builder, allows nested building
     */
    public static class Builder<OwningBuilder extends OwnedBuilder>
            extends ExpressionItem.Builder<OwningBuilder, ExpressionTerm, Builder<OwningBuilder>> {
        private String field;

        private Condition condition;

        private String value;

        private DocRef dictionary;

        /**
         * @param value The name of the field that is being evaluated in this predicate term"
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<OwningBuilder> field(final String value) {
            this.field = value;
            return self();
        }

        /**
         * @param value The condition of the predicate term
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<OwningBuilder> condition(final Condition value) {
            this.condition = value;
            return self();
        }

        /**
         * @param value The value that the field value is being evaluated against. Not required if a dictionary is supplied
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<OwningBuilder> value(final String value) {
            this.value = value;
            return self();
        }

        /**
         * Add a dictionary term to the builder, e.g fieldX|IN_DICTIONARY|docRefToDictionaryY
         * Term is enabled by default. Not all data sources support dictionary terms and only certain
         * conditions are supported for a dictionary term.
         *
         * @param value The DocRef for the dictionary that this predicate is using for its evaluation
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<OwningBuilder> dictionary(final DocRef value) {
            this.dictionary = value;
            return self();
        }

        /**
         * Begin construction of the {@link DocRef} for the dictionary that this predicate is using for evaluation
         * @return The DocRef.Builder, configured to pop back to this builder when complete
         */
        public DocRef.Builder<Builder<OwningBuilder>> dictionary() {
            return new DocRef.Builder<Builder<OwningBuilder>>()
                    .popToWhenComplete(this, this::dictionary);
        }

        @Override
        protected ExpressionTerm pojoBuild() {
            return new ExpressionTerm(getEnabled(), field, condition, value, dictionary);
        }

        @Override
        public Builder<OwningBuilder> self() {
            return this;
        }
    }
}