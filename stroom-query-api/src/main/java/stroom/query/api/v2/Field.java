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

package stroom.query.api.v2;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@JsonPropertyOrder({"name", "expression", "sort", "filter", "format", "group"})
@XmlType(name = "Field", propOrder = {"name", "expression", "sort", "filter", "format", "group"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "Describes a field in a result set. The field can have various expressions applied to it, " +
        "e.g. SUM(), along with sorting, filtering, formatting and grouping")
public final class Field implements Serializable {
    private static final long serialVersionUID = 7327802315955158337L;

    @XmlElement
    @ApiModelProperty(
            value = "The name of the field for display purposes",
            required = false)
    private String name;

    @XmlElement
    @ApiModelProperty(
            value = "The expression to use to generate the value for this field",
            required = true,
            example = "SUM(${count})")
    private String expression;

    @XmlElement
    @ApiModelProperty(required = false)
    private Sort sort;

    @XmlElement
    @ApiModelProperty(required = false)
    private Filter filter;

    @XmlElement
    @ApiModelProperty(required = false)
    private Format format;

    @XmlElement
    @ApiModelProperty(
            value = "If this field is to be grouped then this defines the level of grouping, with 0 being the top " +
                    "level of grouping, 1 being the next level down, etc.",
            required = false)
    private Integer group;

    public Field() {
    }

    public Field(final String name,
                 final String expression,
                 final Sort sort,
                 final Filter filter,
                 final Format format,
                 final Integer group) {
        this.name = name;
        this.expression = expression;
        this.sort = sort;
        this.filter = filter;
        this.format = format;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public Sort getSort() {
        return sort;
    }

    public Filter getFilter() {
        return filter;
    }

    public Format getFormat() {
        return format;
    }

    public Integer getGroup() {
        return group;
    }

    public Field copy() {
        final Field field = new Field();
        field.name = name;
        field.expression = expression;
        field.sort = sort;
        field.filter = filter;
        field.format = format;
        field.group = group;

        return field;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Field)) return false;

        final Field field = (Field) o;

        if (name != null ? !name.equals(field.name) : field.name != null) return false;
        if (expression != null ? !expression.equals(field.expression) : field.expression != null) return false;
        if (sort != null ? !sort.equals(field.sort) : field.sort != null) return false;
        if (filter != null ? !filter.equals(field.filter) : field.filter != null) return false;
        if (format != null ? !format.equals(field.format) : field.format != null) return false;
        return group != null ? group.equals(field.group) : field.group == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", expression='" + expression + '\'' +
                ", sort=" + sort +
                ", filter=" + filter +
                ", format=" + format +
                ", group=" + group +
                '}';
    }

    /**
     * Builder for constructing a {@link Field}
     */
    public static class Builder {

        private String name;
        private String expression;
        private Sort sort;
        private Filter filter;
        private Format format;
        private Integer group;

        /**
         * @param name The name of the field for display purposes
         * @param expression The expression to use to generate the value for this field
         */
        public Builder(final String name,
                       final String expression) {
            this.name = name;
            this.expression = expression;
        }

        /**
         * No args constructor, allow all building using chained methods
         */
        public Builder() {

        }

        /**
         * @param value The name of the field for display purposes
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        /**
         * @param value The expression to use to generate the value for this field
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder expression(final String value) {
            this.expression = value;
            return this;
        }

        /**
         * @param value The sorting configuration to use
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder sort(final Sort value) {
            this.sort = value;
            return this;
        }

        /**
         * @param value Any regex filtering to apply to the values
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder filter(final Filter value) {
            this.filter = value;
            return this;
        }

        /**
         * @param value Formatting to apply to the value
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder format(final Format value) {
            this.format = value;
            return this;
        }

        /**
         * @param value Formatting type to apply to the value
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder format(final Format.Type value) {
            this.format = new Format(value);
            return this;
        }

        /**
         * Set the group level
         * @param group The group level to apply to this field
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder group(final Integer group) {
            this.group = group;
            return this;
        }

        public Field build() {
            return new Field(name, expression, sort, filter, format, group);
        }
    }
}