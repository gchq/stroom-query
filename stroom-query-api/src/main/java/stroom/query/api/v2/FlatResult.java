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
import stroom.util.shared.PojoBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonPropertyOrder({"componentId", "structure", "values", "size", "error"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(
        description = "A result structure used primarily for visualisation data",
        parent = Result.class)
public final class FlatResult extends Result {

    private static final long serialVersionUID = 3826654996795750099L;

    @XmlElement
    private List<Field> structure;

    @XmlElement
    @ApiModelProperty(value = "The 2 dimensional array containing the result set. The positions in the inner array " +
            "correspond to the positions in the 'structure' property")
    private List<List<Object>> values;

    @XmlElement
    @ApiModelProperty(value = "The size of the result set being returned")
    private Long size;

    private FlatResult() {
    }

    public FlatResult(final String componentId,
                      final List<Field> structure,
                      final List<List<Object>> values,
                      final String error) {
        super(componentId, error);
        this.structure = structure;
        this.values = values;
        this.size = (long) values.size();
    }

    public FlatResult(final String componentId,
                      final List<Field> structure,
                      final List<List<Object>> values,
                      final Long size,
                      final String error) {
        super(componentId, error);
        this.structure = structure;
        this.values = values;
        this.size = size;
    }

    public List<Field> getStructure() {
        return structure;
    }

    public List<List<Object>> getValues() {
        return values;
    }

    public Long getSize() {
        return size;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final FlatResult that = (FlatResult) o;

        if (structure != null ? !structure.equals(that.structure) : that.structure != null) return false;
        if (values != null ? !values.equals(that.values) : that.values != null) return false;
        return (size != null ? !size.equals(that.size) : that.size != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (structure != null ? structure.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return size + " rows";
    }

    /**
     * Builder for constructing a {@link FlatResult flatResult}
     */
    public static class Builder<ParentBuilder extends PojoBuilder>
            extends Result.Builder<ParentBuilder, FlatResult, Builder<ParentBuilder>> {
        private final List<Field> structure = new ArrayList<>();

        private final List<List<Object>> values = new ArrayList<>();

        public Builder<ParentBuilder> addFields(final Field...fields) {
            structure.addAll(Arrays.asList(fields));
            return self();
        }

        public Field.Builder<Builder<ParentBuilder>> addField() {
            return new Field.Builder<Builder<ParentBuilder>>().parent(this, this::addFields);
        }

        public Builder<ParentBuilder> addValues(final List<Object>... values) {
            this.values.addAll(Arrays.asList(values));
            return self();
        }

        public ValueListBuilder<ParentBuilder> addValues() {
            return new ValueListBuilder<ParentBuilder>().parent(this, this::addValues);
        }

        protected FlatResult pojoBuild() {
            return new FlatResult(getComponentId(), structure, values, getError());
        }

        @Override
        public Builder<ParentBuilder> self() {
            return this;
        }
    }

    public static class ValueListBuilder<GrandparentBuilder extends PojoBuilder>
            extends PojoBuilder<Builder<GrandparentBuilder>, List<Object>, ValueListBuilder<GrandparentBuilder>> {
        private final List<Object> childValues = new ArrayList<>();

        public ValueListBuilder value(final Object...values) {
            this.childValues.addAll(Arrays.asList(values));
            return this;
        }

        @Override
        protected List<Object> pojoBuild() {
            return childValues;
        }

        @Override
        public ValueListBuilder self() {
            return this;
        }
    }
}