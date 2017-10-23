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
import stroom.util.shared.OwnedBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    public TableSettings(final String queryId,
                         final List<Field> fields,
                         final Boolean extractValues,
                         final DocRef extractionPipeline,
                         final List<Integer> maxResults,
                         final Boolean showDetail) {
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
     *
     * @param <OwningBuilder> The class of the popToWhenComplete builder, allows nested building
     */
    public static abstract class ABuilder<OwningBuilder extends OwnedBuilder, CHILD_CLASS extends ABuilder<OwningBuilder, ?>>
            extends OwnedBuilder<OwningBuilder, TableSettings, CHILD_CLASS> {
        private String queryId;
        private final List<Field> fields = new ArrayList<>();
        private Boolean extractValues;
        private DocRef extractionPipeline;
        private Boolean showDetail;

        private final List<Integer> maxResults = new ArrayList<>();

        /**
         * @param value The ID for the query that wants these results
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS queryId(final String value) {
            this.queryId = value;
            return self();
        }

        /**
         * @param values Add expected fields to the output table
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS addFields(final Field...values) {
            return addFields(Arrays.asList(values));
        }

        /**
         * Convenience function for adding multiple fields that are already in a collection.
         * @param values The fields to add
         * @return This builder, with the fields added.
         */
        public CHILD_CLASS addFields(final Collection<Field> values) {
            this.fields.addAll(values);
            return self();
        }

        /**
         * Start building a field to add to the expected list
         * @return The Field.Builder, configured to pop back to this one when complete
         */
        public Field.OBuilder<CHILD_CLASS> addField() {
            return new Field.OBuilder<CHILD_CLASS>()
                    .popToWhenComplete(self(), this::addFields);
        }

        /**
         * Start building a field to add to the expected list
         * @param name  The field name
         * @param expression The expression to use to generate the field value
         * @return The Field.Builder, configured to pop back to this one when complete
         */
        public Field.OBuilder<CHILD_CLASS> addField(final String name, final String expression) {
            return new Field.OBuilder<CHILD_CLASS>(name, expression)
                    .popToWhenComplete(self(), this::addFields);
        }

        /**
         * @param values The max result value
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS addMaxResults(final Integer...values) {
            return addMaxResults(Arrays.asList(values));
        }

        /**
         * Add a collection of max result values
         * @param values The list of max result values
         * @return This builder
         */
        public CHILD_CLASS addMaxResults(final Collection<Integer> values) {
            this.maxResults.addAll(values);
            return self();
        }

        /**
         * @param value TODO - unknown purpose
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS extractValues(final Boolean value) {
            this.extractValues = value;
            return self();
        }

        /**
         * @param value The reference to the extraction pipeline that will be used on the results
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS extractionPipeline(final DocRef value) {
            this.extractionPipeline = value;
            return self();
        }

        /**
         * Start building the DocRef which points to the extraction pipeline
         * @return The DocRef.Builder, configured to pop back to this one when complete
         */
        public DocRef.OBuilder<CHILD_CLASS> extractionPipeline() {
            return new DocRef.OBuilder<CHILD_CLASS>()
                    .popToWhenComplete(self(), this::extractionPipeline);
        }

        /**
         * Shortcut function for creating the extractionPipeline {@link DocRef} in one go
         * @param type The type of the extractionPipeline
         * @param uuid The UUID of the extractionPipeline
         * @param name The name of the extractionPipeline
         * @return This builder, with the completed extractionPipeline added.
         */
        public CHILD_CLASS extractionPipeline(final String type,
                                                         final String uuid,
                                                         final String name) {
            return this.extractionPipeline().type(type).uuid(uuid).name(name).end();
        }

        /**
         * @param value When grouping is used a value of true indicates that the results will include
         *              the full detail of any results aggregated into a group as well as their aggregates.
         *              A value of false will only include the aggregated values for each group. Defaults to false.
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS showDetail(final Boolean value) {
            this.showDetail = value;
            return self();
        }

        protected TableSettings pojoBuild() {
            return new TableSettings(queryId, fields, extractValues, extractionPipeline, maxResults, showDetail);
        }

    }

    /**
     * A builder that is owned by another builder, used for popping back up a stack
     *
     * @param <OwningBuilder> The class of the parent builder
     */
    public static final class OBuilder<OwningBuilder extends OwnedBuilder>
            extends ABuilder<OwningBuilder, OBuilder<OwningBuilder>> {

        @Override
        public OBuilder<OwningBuilder> self() {
            return this;
        }
    }

    /**
     * A builder that is created independently of any parent builder
     */
    public static final class Builder extends ABuilder<Builder, Builder> {

        @Override
        public Builder self() {
            return this;
        }
    }
}