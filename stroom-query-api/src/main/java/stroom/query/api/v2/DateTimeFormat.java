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
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;

/**
 * Class for describing the format to use for formatting a date time value
 */
@JsonPropertyOrder({"pattern", "timeZone"})
@XmlType(name = "DateTimeFormat", propOrder = {"pattern", "timeZone"})
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel(description = "The string formatting to apply to a date value")
public final class DateTimeFormat implements Serializable {
    private static final long serialVersionUID = 9145624653060319801L;

    @XmlElement
    @ApiModelProperty(
            value = "A date time formatting pattern string conforming to the specification of " +
                    "java.time.format.DateTimeFormatter",
            required = true)
    private String pattern;

    @XmlElement
    @ApiModelProperty(
            required = true)
    private TimeZone timeZone;

    /**
     * Default constructor for deserialisation
     */
    private DateTimeFormat() {
    }

    /**
     * @param pattern A date time formatting pattern string conforming to the specification of
     * {@link java.time.format.DateTimeFormatter}
     * @param timeZone The time zone to use when formatting the date time value
     */
    public DateTimeFormat(final String pattern, final TimeZone timeZone) {
        this.pattern = pattern;
        this.timeZone = timeZone;
    }

    /**
     * @return The format pattern string, conforming to {@link java.time.format.DateTimeFormatter}
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @return The the {@link TimeZone timeZone} to use when formatting the date
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DateTimeFormat)) return false;

        final DateTimeFormat that = (DateTimeFormat) o;

        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;
        return timeZone != null ? timeZone.equals(that.timeZone) : that.timeZone == null;
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateTimeFormat{" +
                "pattern='" + pattern + '\'' +
                ", timeZone=" + timeZone +
                '}';
    }

    /**
     * Builder for constructing a {@link DateTimeFormat dateTimeFormat}
     *
     * @param <OwningBuilder> The class of the popToWhenComplete builder, allows nested building
     */
    public static abstract class ABuilder<OwningBuilder extends OwnedBuilder, CHILD_CLASS extends ABuilder<OwningBuilder, ?>>
            extends OwnedBuilder<OwningBuilder, DateTimeFormat, CHILD_CLASS> {
        private String pattern;

        private TimeZone timeZone;

        /**
         * @param value The format pattern string, conforming to {@link java.time.format.DateTimeFormatter}
         *
         * @return The {@link CHILD_CLASS}, enabling method chaining
         */
        public CHILD_CLASS pattern(final String value) {
            this.pattern = value;
            return self();
        }
        /**
         * @param value Set the {@link TimeZone timeZone} to use when formatting the date
         *
         * @return The {@link CHILD_CLASS}, enabling method chaining
         */
        public CHILD_CLASS timeZone(final TimeZone value) {
            this.timeZone = value;
            return self();
        }

        /**
         * Construct a new timeZone
         *
         * @return The {@link TimeZone.Builder} for method chaining the child construction
         */
        public TimeZone.OBuilder<CHILD_CLASS> timeZone() {
            return new TimeZone.OBuilder<CHILD_CLASS>()
                    .popToWhenComplete(self(), this::timeZone);
        }

        protected DateTimeFormat pojoBuild() {
            return new DateTimeFormat(pattern, timeZone);
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