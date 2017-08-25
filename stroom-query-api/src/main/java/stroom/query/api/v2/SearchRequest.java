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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

/**
 * A class for describing a search request including the query to run and definition(s) of how the results
 * should be returned
 */
@JsonPropertyOrder({"key", "query", "resultRequests", "dateTimeLocale", "incremental"})
@XmlRootElement(name = "searchRequest")
@XmlType(name = "SearchRequest", propOrder = {"key", "query", "resultRequests", "dateTimeLocale", "incremental"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "A request for new search or a follow up request for more data for an existing iterative search")
public final class SearchRequest implements Serializable {
    private static final long serialVersionUID = -6668626615097471925L;

    @XmlElement
    @ApiModelProperty(
            required = true)
    private QueryKey key;

    @XmlElement
    @ApiModelProperty(
            required = true)
    private Query query;

    @XmlElementWrapper(name = "resultRequests")
    @XmlElement(name = "resultRequest")
    @ApiModelProperty(
            value = "A list of ResultRequest objects",
            required = true)
    private List<ResultRequest> resultRequests;


    @XmlElement
    @ApiModelProperty(
            value = "The locale to use when formatting date values in the search results. The " +
                    "value is the string form of a {@link java.time.ZoneId zoneId}",
            required = true)
    private String dateTimeLocale;

    @XmlElement
    @ApiModelProperty(
            value = "If true the response will contain all results found so far. Future requests " +
            "for the same query key may return more results. Intended for use on longer running searches to allow " +
            "partial result sets to be returned as soon as they are available rather than waiting for the full " +
            "result set.",
            required = true)
    private Boolean incremental;

    private SearchRequest() {
    }

    /**
     * @param key            A unique key to identify the instance of the search by. This key is used to identify multiple
     *                       requests for the same search when running in incremental mode.
     * @param query          The query terms for the search
     * @param resultRequests A list of {@link ResultRequest resultRequest} definitions. If null or the list is empty
     *                       no results will be returned. Allows the caller to request that the results of the query
     *                       are returned in multiple forms, e.g. using a number of different
     *                       filtering/aggregation/sorting approaches.
     * @param dateTimeLocale The locale to use when formatting date values in the search results. The value is the
     *                       string form of a {@link java.time.ZoneId zoneId}
     * @param incremental    If true the response will contain all results found so far. Future requests for the same
     *                       query key may return more results. Intended for use on longer running searches to allow
     *                       partial result sets to be returned as soon as they are available rather than waiting for the
     *                       full result set.
     */
    public SearchRequest(final QueryKey key,
                         final Query query,
                         final List<ResultRequest> resultRequests,
                         final String dateTimeLocale,
                         final Boolean incremental) {
        this.key = key;
        this.query = query;
        this.resultRequests = resultRequests;
        this.dateTimeLocale = dateTimeLocale;
        this.incremental = incremental;
    }

    /**
     * @return The unique {@link QueryKey queryKey} for the search request
     */
    public QueryKey getKey() {
        return key;
    }

    /**
     * @return The {@link Query query} object containing the search terms
     */
    public Query getQuery() {
        return query;
    }

    /**
     * @return The list of {@link ResultRequest resultRequest} objects
     */
    public List<ResultRequest> getResultRequests() {
        return resultRequests;
    }

    /**
     * @return The locale ID, see {@link java.time.ZoneId}, for the date values uses in the search response.
     */
    public String getDateTimeLocale() {
        return dateTimeLocale;
    }

    /**
     * @return Whether the search should return immediately with the results found so far or wait for the search
     * to finish
     */
    public Boolean getIncremental() {
        return incremental;
    }

    /**
     * @return Whether the search should return immediately with the results found so far or wait for the search
     * to finish
     */
    public boolean incremental() {
        return incremental != null && incremental;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SearchRequest that = (SearchRequest) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (query != null ? !query.equals(that.query) : that.query != null) return false;
        if (resultRequests != null ? !resultRequests.equals(that.resultRequests) : that.resultRequests != null)
            return false;
        if (dateTimeLocale != null ? !dateTimeLocale.equals(that.dateTimeLocale) : that.dateTimeLocale != null)
            return false;
        return incremental != null ? incremental.equals(that.incremental) : that.incremental == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (resultRequests != null ? resultRequests.hashCode() : 0);
        result = 31 * result + (dateTimeLocale != null ? dateTimeLocale.hashCode() : 0);
        result = 31 * result + (incremental != null ? incremental.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SearchRequest{" +
                "key=" + key +
                ", query=" + query +
                ", resultRequests=" + resultRequests +
                ", dateTimeLocale='" + dateTimeLocale + '\'' +
                ", incremental=" + incremental +
                '}';
    }
}