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

package stroom.query.common.v2;

import stroom.dashboard.expression.v1.Expression;
import stroom.dashboard.expression.v1.ExpressionParser;
import stroom.dashboard.expression.v1.FieldIndexMap;
import stroom.dashboard.expression.v1.FunctionFactory;
import stroom.dashboard.expression.v1.ParamFactory;
import stroom.query.api.v2.Field;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CompiledFields implements Iterable<CompiledField> {
    private final List<CompiledField> compiledFields;

    public CompiledFields(final List<Field> fields,
                          final FieldIndexMap fieldIndexMap,
                          final Map<String, String> paramMap) {
        if (null != fields) {
            compiledFields = new ArrayList<>(fields.size());

            final ExpressionParser expressionParser = new ExpressionParser(new FunctionFactory(), new ParamFactory());
            for (final Field field : fields) {
                // Create compiled field.
                int groupDepth = -1;
                if (field.getGroup() != null) {
                    groupDepth = field.getGroup();
                }
                Expression expression = null;
                if (fieldIndexMap != null && field.getExpression() != null && field.getExpression().trim().length() > 0) {
                    try {
                        expression = expressionParser.parse(fieldIndexMap, field.getExpression());
                        expression.setStaticMappedValues(paramMap);
                    } catch (final ParseException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }

                CompiledFilter filter = null;
                if (field.getFilter() != null) {
                    filter = new CompiledFilter(field.getFilter(), paramMap);
                }

                final CompiledField compiledField = new CompiledField(field, groupDepth, expression, filter);

                // Only include this field if it is used for display, grouping,
                // sorting.
                compiledFields.add(compiledField);
            }
        } else {
            compiledFields = Collections.emptyList();
        }
    }

    @Override
    public Iterator<CompiledField> iterator() {
        return compiledFields.iterator();
    }

    public int size() {
        return compiledFields.size();
    }

    public CompiledField getField(final int i) {
        return compiledFields.get(i);
    }

    @Override
    public String toString() {
        return "CompiledFields{" +
                "compiledFields=" + compiledFields +
                '}';
    }
}
