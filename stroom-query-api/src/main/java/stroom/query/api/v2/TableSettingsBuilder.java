/*
 * Copyright 2016 Crown Copyright
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

import java.util.List;

/**
 * A builder for constructing a {@link TableSettings} object
 */
public class TableSettingsBuilder {
    private String queryId;
    private List<Field> fields;
    private Boolean extractValues;
    private DocRef extractionPipeline;
    private List<Integer> maxResults;
    private Boolean showDetail;

    /**
     * @param value XXXXXXXXXXXXXXXX
     *
     * @return The {@link Builder}, enabling method chaining
     */
    public TableSettingsBuilder queryId(final String value) {
        this.queryId = value;
        return this;
    }

    /**
     * @param values XXXXXXXXXXXXXXXX
     *
     * @return The {@link Builder}, enabling method chaining
     */
    public TableSettingsBuilder fields(final List<Field> values) {
        this.fields = values;
        return this;
    }

    /**
     * @param value XXXXXXXXXXXXXXXX
     *
     * @return The {@link Builder}, enabling method chaining
     */
    public TableSettingsBuilder extractValues(final Boolean value) {
        this.extractValues = value;
        return this;
    }

    /**
     * @param value XXXXXXXXXXXXXXXX
     *
     * @return The {@link Builder}, enabling method chaining
     */
    public TableSettingsBuilder extractionPipeline(final DocRef value) {
        this.extractionPipeline = value;
        return this;
    }

    /**
     * @param values XXXXXXXXXXXXXXXX
     *
     * @return The {@link Builder}, enabling method chaining
     */
    public TableSettingsBuilder maxResults(final List<Integer> values) {
        this.maxResults = values;
        return this;
    }

    /**
     * @param value XXXXXXXXXXXXXXXX
     *
     * @return The {@link Builder}, enabling method chaining
     */
    public TableSettingsBuilder showDetail(final Boolean value) {
        this.showDetail = value;
        return this;
    }

    public TableSettings build() {
        return new TableSettings(queryId, fields, extractValues, extractionPipeline, maxResults, showDetail);
    }
}