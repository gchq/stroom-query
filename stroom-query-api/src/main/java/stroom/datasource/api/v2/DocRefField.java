/*
 * Copyright 2019 Crown Copyright
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

package stroom.datasource.api.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import stroom.query.api.v2.ExpressionTerm.Condition;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"type", "docRefType", "name", "queryable", "conditions"})
@XmlType(name = "DataSourceField", propOrder = {"type", "docRefType", "name", "queryable", "conditions"})
public class DocRefField extends AbstractField {
    private static final long serialVersionUID = 1272545271946712570L;

    private static List<Condition> DEFAULT_CONDITIONS = new ArrayList<>();
    static {
        DEFAULT_CONDITIONS.add(Condition.IS_DOC_REF);
        DEFAULT_CONDITIONS.add(Condition.CONTAINS);
        DEFAULT_CONDITIONS.add(Condition.EQUALS);
        DEFAULT_CONDITIONS.add(Condition.IN);
        DEFAULT_CONDITIONS.add(Condition.IN_DICTIONARY);
        DEFAULT_CONDITIONS.add(Condition.IN_FOLDER);
    }

    @XmlElement
    @ApiModelProperty(
            value = "The doc ref type for the field",
            required = true)
    private String docRefType;

    public DocRefField() {
    }

    public DocRefField(final String docRefType,
                       final String name) {
        super(name, Boolean.TRUE, DEFAULT_CONDITIONS);
        this.docRefType = docRefType;
    }

    public DocRefField(final String docRefType,
                       final String name,
                       final Boolean queryable) {
        super(name, queryable, DEFAULT_CONDITIONS);
        this.docRefType = docRefType;
    }

    public DocRefField(final String docRefType,
                       final String name,
                       final Boolean queryable,
                       final List<Condition> conditions) {
        super(name, queryable, conditions);
        this.docRefType = docRefType;
    }

    public String getDocRefType() {
        return docRefType;
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public String getType() {
        return FieldTypes.DOC_REF;
    }
}