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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonPropertyOrder({"queryId", "fields", "extractValues", "extractionPipeline", "maxResults",
        "showDetail"})
@XmlType(
        name = "TableSettings",
        propOrder = {"queryId", "fields", "extractValues", "extractionPipeline", "maxResults", "showDetail"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "An object to describe how the query results should be returned, including which fields " +
        "should be included and what sorting, grouping, filtering, limiting, etc. should be applied")
public final class TableSettings implements Serializable {
    private static final long serialVersionUID = -2530827581046882396L;

    @XmlElement
    @ApiModelProperty(
        value = "TODO",
        required = true)
    private String queryId;

    @XmlElementWrapper(name = "fields")
    @XmlElement(name = "field")
    @ApiModelProperty(required = true)
    private List<Field> fields;

    @XmlElement
    @ApiModelProperty(
            value = "TODO",
            required = false)
    private Boolean extractValues;

    @XmlElement
    @ApiModelProperty(required = false)
    private DocRef extractionPipeline;

    @XmlElementWrapper(name = "maxResults")
    @XmlElement(name = "val")
    @ApiModelProperty(
            value = "Defines the maximum number of results to return at each grouping level, e.g. '1000,10,1' means " +
                    "1000 results at group level 0, 10 at level 1 and 1 at level 2. In the absence of this field " +
                    "system defaults will apply",
            required = false,
            example = "1000,10,1")
    private List<Integer> maxResults;

    @XmlElement
    @ApiModelProperty(
            value = "When grouping is used a value of true indicates that the results will include the full detail of " +
                    "any results aggregated into a group as well as their aggregates. A value of false will only " +
                    "include the aggregated values for each group. Defaults to false.",
            required = false)
    private Boolean showDetail;

    private TableSettings() {
    }

    public TableSettings(final String queryId, final List<Field> fields, final Boolean extractValues, final DocRef extractionPipeline, final List<Integer> maxResults, final Boolean showDetail) {
        this.queryId = queryId;
        this.fields = fields;
        this.extractValues = extractValues;
        this.extractionPipeline = extractionPipeline;
        this.maxResults = maxResults;
        this.showDetail = showDetail;
    }

    public String getQueryId() {
        return queryId;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Boolean getExtractValues() {
        return extractValues;
    }

    public boolean extractValues() {
        if (extractValues == null) {
            return false;
        }
        return extractValues;
    }

    public DocRef getExtractionPipeline() {
        return extractionPipeline;
    }

    public List<Integer> getMaxResults() {
        return maxResults;
    }

    public Boolean getShowDetail() {
        return showDetail;
    }

    public boolean showDetail() {
        if (showDetail == null) {
            return false;
        }
        return showDetail;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TableSettings that = (TableSettings) o;

        if (queryId != null ? !queryId.equals(that.queryId) : that.queryId != null) return false;
        if (fields != null ? !fields.equals(that.fields) : that.fields != null) return false;
        if (extractValues != null ? !extractValues.equals(that.extractValues) : that.extractValues != null)
            return false;
        if (extractionPipeline != null ? !extractionPipeline.equals(that.extractionPipeline) : that.extractionPipeline != null)
            return false;
        if (maxResults != null ? !maxResults.equals(that.maxResults) : that.maxResults != null) return false;
        return showDetail != null ? showDetail.equals(that.showDetail) : that.showDetail == null;
    }

    @Override
    public int hashCode() {
        int result = queryId != null ? queryId.hashCode() : 0;
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        result = 31 * result + (extractValues != null ? extractValues.hashCode() : 0);
        result = 31 * result + (extractionPipeline != null ? extractionPipeline.hashCode() : 0);
        result = 31 * result + (maxResults != null ? maxResults.hashCode() : 0);
        result = 31 * result + (showDetail != null ? showDetail.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TableSettings{" +
                "queryId='" + queryId + '\'' +
                ", fields=" + fields +
                ", extractValues=" + extractValues +
                ", extractionPipeline=" + extractionPipeline +
                ", maxResults=" + maxResults +
                ", showDetail=" + showDetail +
                '}';
    }

    /**
     * Builder for constructing a {@link TableSettings tableSettings}
     */
    public static class Builder<ParentBuilder extends PojoBuilder>
            extends PojoBuilder<ParentBuilder, TableSettings, Builder<ParentBuilder>> {
        private String queryId;
        private final List<Field> fields = new ArrayList<>();
        private Boolean extractValues;
        private DocRef extractionPipeline;
        private Boolean showDetail;

        private final List<Integer> maxResults = new ArrayList<>();

        /**
         * @param value XXXXXXXXXXXXXXXX
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<ParentBuilder> queryId(final String value) {
            this.queryId = value;
            return self();
        }

        /**
         * @param values XXXXXXXXXXXXXXXX
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<ParentBuilder> addFields(final Field...values) {
            this.fields.addAll(Arrays.asList(values));
            return self();
        }

        public Field.Builder<Builder<ParentBuilder>> addField() {
            return new Field.Builder<Builder<ParentBuilder>>()
                    .parent(this, this::addFields);
        }

        /**
         * @param values XXXXXXXXXXXXXXXX
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<ParentBuilder> addMaxResults(final Integer...values) {
            this.maxResults.addAll(Arrays.asList(values));
            return self();
        }

        /**
         * @param value XXXXXXXXXXXXXXXX
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<ParentBuilder> extractValues(final Boolean value) {
            this.extractValues = value;
            return self();
        }

        /**
         * @param value XXXXXXXXXXXXXXXX
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<ParentBuilder> extractionPipeline(final DocRef value) {
            this.extractionPipeline = value;
            return self();
        }

        public DocRef.Builder<Builder<ParentBuilder>> extractionPipeline() {
            return new DocRef.Builder<Builder<ParentBuilder>>()
                    .parent(this, this::extractionPipeline);
        }

        /**
         * @param value XXXXXXXXXXXXXXXX
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder<ParentBuilder> showDetail(final Boolean value) {
            this.showDetail = value;
            return self();
        }

        protected TableSettings pojoBuild() {
            return new TableSettings(queryId, fields, extractValues, extractionPipeline, maxResults, showDetail);
        }

        @Override
        public Builder<ParentBuilder> self() {
            return this;
        }
    }
}