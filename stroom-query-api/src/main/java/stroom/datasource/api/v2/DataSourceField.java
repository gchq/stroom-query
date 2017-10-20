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

package stroom.datasource.api.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import stroom.query.api.v2.ExpressionTerm.Condition;
import stroom.util.shared.HasDisplayValue;
import stroom.util.shared.OwnedBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonPropertyOrder({"type", "name", "queryable", "conditions"})
@XmlType(name = "DataSourceField", propOrder = {"type", "name", "queryable", "conditions"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "The definition of a field within a data source")
public final class DataSourceField implements Serializable, HasDisplayValue {
    private static final long serialVersionUID = 1272545271946712570L;

    @XmlElement
    @ApiModelProperty(
            value = "The data type for the field",
            required = true)
    private DataSourceFieldType type;

    @XmlElement
    @ApiModelProperty(
            value = "The name of the field",
            example = "field1",
            required = true)
    private String name;

    @XmlElement
    @ApiModelProperty(
            value = "Whether the field can be used in predicate in a query",
            example = "true",
            required = true)
    private Boolean queryable;

    /**
     * Defines a list of the {@link Condition} values supported by this field,
     * can be null in which case a default set will be returned. Not persisted
     * in the XML
     */
    @XmlElementWrapper(name = "conditions")
    @XmlElement(name = "condition")
    @ApiModelProperty(
            value = "The supported predicate conditions for this field",
            required = true)
    private List<Condition> conditions;

    private DataSourceField() {
    }

    public DataSourceField(final DataSourceFieldType type, final String name, final Boolean queryable, final List<Condition> conditions) {
        this.type = type;
        this.name = name;
        this.queryable = queryable;
        this.conditions = conditions;
    }

    public DataSourceFieldType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Boolean getQueryable() {
        return queryable;
    }

    public boolean queryable() {
        return queryable != null && queryable;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public String getDisplayValue() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DataSourceField that = (DataSourceField) o;

        if (type != that.type) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (queryable != null ? !queryable.equals(that.queryable) : that.queryable != null) return false;
        return conditions != null ? conditions.equals(that.conditions) : that.conditions == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (queryable != null ? queryable.hashCode() : 0);
        result = 31 * result + (conditions != null ? conditions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataSourceField{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", queryable=" + queryable +
                ", conditions=" + conditions +
                '}';
    }

    public enum DataSourceFieldType implements HasDisplayValue {
        FIELD("Text", false),
        NUMERIC_FIELD("Number", true),
        DATE_FIELD("Date", false),
        ID("Id", true);

        private final String displayValue;
        private final boolean numeric;

        DataSourceFieldType(final String displayValue, final boolean numeric) {
            this.displayValue = displayValue;
            this.numeric = numeric;
        }

        public boolean isNumeric() {
            return numeric;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }
    }

    public static class Builder<OwningBuilder extends OwnedBuilder>
        extends OwnedBuilder<OwningBuilder, DataSourceField, Builder<OwningBuilder>> {

        private DataSourceFieldType type;
        private String name;
        private Boolean queryable;
        private final List<Condition> conditions = new ArrayList<>();

        public Builder<OwningBuilder> type(final DataSourceFieldType value) {
            this.type = value;
            return self();
        }

        public Builder<OwningBuilder> name(final String value) {
            this.name = value;
            return self();
        }

        public Builder<OwningBuilder> queryable(final Boolean value) {
            this.queryable = value;
            return self();
        }

        public Builder<OwningBuilder> addConditions(final Condition...values) {
            this.conditions.addAll(Arrays.asList(values));
            return self();
        }

        @Override
        protected DataSourceField pojoBuild() {
            return new DataSourceField(type, name, queryable, conditions);
        }

        @Override
        public Builder<OwningBuilder> self() {
            return this;
        }
    }
}