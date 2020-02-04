/*
 * Copyright 2018 Crown Copyright
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

package stroom.docref;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

/**
 * {@value #CLASS_DESC}
 */
@JsonPropertyOrder({"type", "uuid", "name"})
@XmlType(name = "DocRef", propOrder = {"type", "uuid", "name"})
@XmlRootElement(name = "doc")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = DocRef.CLASS_DESC)
public class DocRef implements Comparable<DocRef>, HasDisplayValue, SharedObject, Serializable {
    public static final String CLASS_DESC = "A class for describing a unique reference to a 'document' in stroom.  " +
            "A 'document' is an entity in stroom such as a data source dictionary or pipeline.";

    private static final long serialVersionUID = -2121399789820829359L;

    @XmlElement
    @ApiModelProperty(
            value = "The type of the 'document' that this DocRef refers to",
            example = "StroomStatsStore",
            required = true)
    private String type;

    @XmlElement
    @ApiModelProperty(
            value = "The unique identifier for this 'document'",
            example = "9f6184b4-bd78-48bc-b0cd-6e51a357f6a6",
            required = true)
    private String uuid;

    @XmlElement
    @ApiModelProperty(
            value = "The name for the data source",
            example = "MyStatistic",
            required = true)
    private String name;

    private transient int hashCode = -1;

    /**
     * JAXB requires a no-arg constructor.
     */
    public DocRef() {
    }

    /**
     * @param type The type of the 'document' that this docRef points to an instance of. Supported types are defined
     *             outside of this documentation.
     * @param uuid A UUID as generated by {@link java.util.UUID#randomUUID()}
     */
    public DocRef(final String type, String uuid) {
        this.type = type;
        this.uuid = uuid;
    }

    /**
     * @param type The type of the 'document' that this docRef points to an instance of. Supported types are defined
     *             outside of this documentation.
     * @param uuid A UUID as generated by {@link java.util.UUID#randomUUID()}
     * @param name The name of the 'document' being referenced
     */
    public DocRef(final String type, String uuid, final String name) {
        this.type = type;
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * @return The type of the 'document' that this docRef points to an instance of. Supported types are defined
     * outside of this documentation.
     */
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return A UUID as generated by {@link java.util.UUID#randomUUID()}
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return The name of the 'document' being referenced
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @XmlTransient
    @JsonIgnore
    @Override
    public String getDisplayValue() {
        return name;
    }

    @Override
    public int compareTo(final DocRef o) {
        int diff = type.compareTo(o.type);

        if (diff == 0) {
            if (name != null && o.name != null) {
                diff = name.compareTo(o.name);
            }
        }
        if (diff == 0) {
            diff = uuid.compareTo(o.uuid);
        }
        return diff;
    }

    public String toInfoString() {
        final StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name);
        }
        if (uuid != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("{");
            sb.append(uuid);
            sb.append("}");
        }

        if (sb.length() > 0) {
            return sb.toString();
        }

        return toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DocRef)) return false;
        final DocRef docRef = (DocRef) o;
        return Objects.equals(type, docRef.type) &&
                Objects.equals(uuid, docRef.uuid);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = Objects.hash(type, uuid);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return "DocRef{" +
                "type='" + type + '\'' +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * Builder for constructing a {@link DocRef docRef}
     */
    public static class Builder {
        private String type;

        private String uuid;

        private String name;

        /**
         * @param value The type of the 'document' that this docRef points to an instance of. Supported types are defined
         *              outside of this documentation.
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder type(final String value) {
            this.type = value;
            return this;
        }

        /**
         * @param value A UUID as generated by {@link java.util.UUID#randomUUID()}
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder uuid(final String value) {
            this.uuid = value;
            return this;
        }

        /**
         * @param value The name of the 'document' being referenced
         * @return The {@link Builder}, enabling method chaining
         */
        public Builder name(final String value) {
            this.name = value;
            return this;
        }

        public DocRef build() {
            return new DocRef(type, uuid, name);
        }
    }
}
