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
import stroom.query.api.v2.ExpressionTerm.Condition;

import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

public class TextField extends AbstractField {
    private static final long serialVersionUID = 1272545271946712570L;

    private static List<Condition> DEFAULT_CONDITIONS = new ArrayList<>();
    static {
        DEFAULT_CONDITIONS.add(Condition.CONTAINS);
        DEFAULT_CONDITIONS.add(Condition.EQUALS);
        DEFAULT_CONDITIONS.add(Condition.IN);
        DEFAULT_CONDITIONS.add(Condition.IN_DICTIONARY);
    }

    public TextField() {
    }

    public TextField(final String name) {
        super(name, Boolean.TRUE, DEFAULT_CONDITIONS);
    }

    public TextField(final String name,
                     final Boolean queryable,
                     final List<Condition> conditions) {
        super(name, queryable, conditions);
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public String getType() {
        return FieldTypes.TEXT;
    }
}