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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import stroom.docref.HasDisplayValue;
import stroom.query.api.v2.ExpressionTerm.Condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({"type", "docRefType", "name", "queryable", "conditions"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @Type(value = TextField.class, name = "Text"),
        @Type(value = BooleanField.class, name = "Boolean"),
        @Type(value = NumberField.class, name = "Number"),
        @Type(value = LongField.class, name = "Long"),
        @Type(value = IntegerField.class, name = "Integer"),
        @Type(value = DateField.class, name = "Date"),
        @Type(value = IdField.class, name = "Id"),
        @Type(value = DocRefField.class, name = "DocRef")
})

@XmlType(name = "DataSourceField", propOrder = {"type", "docRefType", "name", "queryable", "conditions"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "The definition of a field within a data source")
public abstract class AbstractField implements Serializable, HasDisplayValue {
    private static final long serialVersionUID = 1272545271946712570L;

//    @XmlElement
//    @ApiModelProperty(
//            value = "The data type for the field",
//            required = true)
//    private DataSourceFieldType type;


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

    public AbstractField(final String name,
                         final Boolean queryable,
                         final List<Condition> conditions) {
//        this.type = type;
        this.name = name;
        this.queryable = queryable;
        this.conditions = conditions;
    }

//    public DataSourceFieldType getType() {
//        return type;
//    }


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
    public boolean isNumeric() {
        return false;
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
        if (!(o instanceof AbstractField)) return false;
        final AbstractField that = (AbstractField) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "DataSourceField{" +
                ", name='" + name + '\'' +
                ", queryable=" + queryable +
                ", conditions=" + conditions +
                '}';
    }

    //
//    @Override
//    public String toString() {
//        return "DataSourceField{" +
//                ", name='" + name + '\'' +
//                ", queryable=" + queryable +
//                ", conditions=" + conditions +
//                '}';
//    }

//    public enum DataSourceFieldType implements HasDisplayValue {
//        FIELD("Text", false),
//        BOOLEAN_FIELD("Boolean", false),
//        NUMERIC_FIELD("Number", true),
//        DATE_FIELD("Date", false),
//        ID("Id", true),
//        DOC_REF("DocRef", false);
//
//        private final String displayValue;
//        private final boolean numeric;
//
//        DataSourceFieldType(final String displayValue, final boolean numeric) {
//            this.displayValue = displayValue;
//            this.numeric = numeric;
//        }
//
//        public boolean isNumeric() {
//            return numeric;
//        }
//
//        @Override
//        public String getDisplayValue() {
//            return displayValue;
//        }
//    }

//    public static class Builder {
//        private DataSourceFieldType type;
//        private String docRefType;
//        private String name;
//        private Boolean queryable;
//        private final List<Condition> conditions = new ArrayList<>();
//
//        public Builder type(final DataSourceFieldType value) {
//            this.type = value;
//            return this;
//        }
//
//        public Builder docRefType(final String docRefType) {
//            this.docRefType = docRefType;
//            return type(DataSourceFieldType.DOC_REF);
//        }
//
//        public Builder name(final String value) {
//            this.name = value;
//            return this;
//        }
//
//        public Builder queryable(final Boolean value) {
//            this.queryable = value;
//            return this;
//        }
//
//        public Builder addConditions(final Condition... values) {
//            this.conditions.addAll(Arrays.asList(values));
//            return this;
//        }
//
//        public DataSourceField build() {
//            return new DataSourceField(type, docRefType, name, queryable, conditions);
//        }
//    }
}