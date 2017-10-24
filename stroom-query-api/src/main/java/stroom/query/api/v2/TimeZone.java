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
import stroom.util.shared.HasDisplayValue;
import stroom.util.shared.OwnedBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@JsonPropertyOrder({"use", "id", "offsetHours", "offsetMinutes"})
@XmlType(name = "TimeZone", propOrder = {"use", "id", "offsetHours", "offsetMinutes"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "The timezone to apply to a date time value")
public final class TimeZone implements Serializable {
    private static final long serialVersionUID = 1200175661441813029L;

    @XmlElement
    //TODO needs better description
    @ApiModelProperty(
            value = "The required type of time zone",
            required = true)
    private Use use;

    @XmlElement
    @ApiModelProperty(
            value = "The id of the time zone, conforming to java.time.ZoneId",
            example = "GMT",
            required = false)
    private String id;

    @XmlElement
    @ApiModelProperty(
            value = "The number of hours this timezone is offset from UTC",
            example = "-1",
            required = false)
    private Integer offsetHours;

    @XmlElement
    @ApiModelProperty(
            value = "The number of minutes this timezone is offset from UTC",
            example = "-30",
            required = false)
    private Integer offsetMinutes;

    private TimeZone() {
    }

    public TimeZone(final Use use, final String id, final Integer offsetHours, final Integer offsetMinutes) {
        this.use = use;
        this.id = id;
        this.offsetHours = offsetHours;
        this.offsetMinutes = offsetMinutes;
    }

    public static TimeZone local() {
        final TimeZone timeZone = new TimeZone();
        timeZone.use = Use.LOCAL;
        return timeZone;
    }

    public static TimeZone utc() {
        final TimeZone timeZone = new TimeZone();
        timeZone.use = Use.UTC;
        return timeZone;
    }

    public static TimeZone fromId(final String id) {
        final TimeZone timeZone = new TimeZone();
        timeZone.use = Use.ID;
        timeZone.id = id;
        return timeZone;
    }

    public static TimeZone fromOffset(final int offsetHours, final int offsetMinutes) {
        final TimeZone timeZone = new TimeZone();
        timeZone.use = Use.OFFSET;
        timeZone.offsetHours = offsetHours;
        timeZone.offsetMinutes = offsetMinutes;
        return timeZone;
    }

    public Use getUse() {
        return use;
    }

    public String getId() {
        return id;
    }

    public Integer getOffsetHours() {
        return offsetHours;
    }

    public Integer getOffsetMinutes() {
        return offsetMinutes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeZone)) return false;

        final TimeZone timeZone = (TimeZone) o;

        if (use != timeZone.use) return false;
        if (id != null ? !id.equals(timeZone.id) : timeZone.id != null) return false;
        if (offsetHours != null ? !offsetHours.equals(timeZone.offsetHours) : timeZone.offsetHours != null)
            return false;
        return offsetMinutes != null ? offsetMinutes.equals(timeZone.offsetMinutes) : timeZone.offsetMinutes == null;
    }

    @Override
    public int hashCode() {
        int result = use != null ? use.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (offsetHours != null ? offsetHours.hashCode() : 0);
        result = 31 * result + (offsetMinutes != null ? offsetMinutes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeZone{" +
                "use=" + use +
                ", id='" + id + '\'' +
                ", offsetHours=" + offsetHours +
                ", offsetMinutes=" + offsetMinutes +
                '}';
    }

    public enum Use implements HasDisplayValue {
        LOCAL("Local"),
        UTC("UTC"),
        ID("Id"),
        OFFSET("Offset");

        private final String displayValue;

        Use(final String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }

        @Override
        public String toString() {
            return getDisplayValue();
        }
    }


    /**
     * Builder for constructing a {@link TimeZone timeZone}
     *
     * @param <OwningBuilder> The class of the popToWhenComplete builder, allows nested building
     */
    public static abstract class ABuilder<OwningBuilder extends OwnedBuilder, CHILD_CLASS extends ABuilder<OwningBuilder, ?>>
            extends OwnedBuilder<OwningBuilder, TimeZone, CHILD_CLASS> {
        private Use use;

        private String id;

        private Integer offsetHours;

        private Integer offsetMinutes;

        /**
         * @param value The required type of time zone
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS use(final Use value) {
            this.use = value;
            return self();
        }

        /**
         * @param value The id of the time zone, conforming to java.time.ZoneId
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS id(final String value) {
            this.id = value;
            return self();
        }

        /**
         * @param value The number of hours this timezone is offset from UTC
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS offsetHours(final Integer value) {
            this.offsetHours = value;
            return self();
        }

        /**
         * @param value The number of minutes this timezone is offset from UTC
         *
         * @return The {@link Builder}, enabling method chaining
         */
        public CHILD_CLASS offsetMinutes(final Integer value) {
            this.offsetMinutes = value;
            return self();
        }

        protected TimeZone pojoBuild() {
            return new TimeZone(use, id, offsetHours, offsetMinutes);
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