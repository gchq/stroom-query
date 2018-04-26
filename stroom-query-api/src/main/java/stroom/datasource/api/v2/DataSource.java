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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({"fields"})
@XmlType(name = "DataSource", propOrder = "fields")
@XmlRootElement(name = "dataSource")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "The definition of a data source, describing the fields available")
public final class DataSource implements Serializable {
    private static final long serialVersionUID = 1272545271946712570L;

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    @ApiModelProperty(required = true)
    private List<DataSourceField> fields;

    private DataSource() {
    }

    public DataSource(final List<DataSourceField> fields) {
        this.fields = fields;
    }

    public List<DataSourceField> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSource that = (DataSource) o;
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    @Override
    public String toString() {
        return "DataSource{" +
                "fields=" + fields +
                '}';
    }

    public static class Builder {

        private final List<DataSourceField> fields = new ArrayList<>();

        public Builder addFields(final DataSourceField...values) {
            this.fields.addAll(Arrays.asList(values));
            return this;
        }

        public DataSource build() {
            return new DataSource(fields);
        }
    }
}