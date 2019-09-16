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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TestDateExpressionParser {
    private final Instant instant = Instant.parse("2015-02-03T01:22:33.056Z");
    private final long nowEpochMilli = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).toInstant().toEpochMilli();

    @Test
    public void testSimple() {
        testSimple("2015-02-03T01:22:33.056Z");
        testSimple("2016-01-01T00:00:00.000Z");
    }

    private void testSimple(final String time) {
        Assertions.assertEquals(
                ZonedDateTime.parse(time),
                DateExpressionParser.parse(time, nowEpochMilli).get());
    }

    @Test
    public void testComplex1() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2017-02-03T01:22:33.056Z"),
                DateExpressionParser.parse("2015-02-03T01:22:33.056Z + 2y", nowEpochMilli).get());
    }

    @Test
    public void testComplex2() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2017-02-06T02:22:35.056Z"),
                DateExpressionParser.parse("2015-02-03T01:22:33.056Z + 2y+3d+1h+2s", nowEpochMilli).get());
    }

    @Test
    public void testComplex3() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2017-02-06T02:22:35.056Z"),
                DateExpressionParser.parse("2015-02-03T01:22:33.056Z + 2y3d1h2s", nowEpochMilli).get());
    }

    @Test
    public void testComplex4() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2017-01-31T00:22:31.056Z"),
                DateExpressionParser.parse("2015-02-03T01:22:33.056Z + 2y-3d1h2s", nowEpochMilli).get());
    }

    @Test
    public void testComplex5() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2017-01-31T00:22:35.056Z"),
                DateExpressionParser.parse("2015-02-03T01:22:33.056Z + 2y-3d1h+2s", nowEpochMilli).get());
    }

    @Test
    public void testComplex6() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2017-01-31T00:22:31.056Z"),
                DateExpressionParser.parse("2015-02-03T01:22:33.056Z + 2y-3d1h-2s", nowEpochMilli).get());
    }

    @Test
    public void testNow() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-03T01:22:33.056Z"),
                DateExpressionParser.parse("now()", nowEpochMilli).get());
    }

    @Test
    public void testSecond() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-03T01:22:33.000Z"),
                DateExpressionParser.parse("second()", nowEpochMilli).get());
    }

    @Test
    public void testMinute() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-03T01:22:00.000Z"),
                DateExpressionParser.parse("minute()", nowEpochMilli).get());
    }

    @Test
    public void testHour() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-03T01:00:00.000Z"),
                DateExpressionParser.parse("hour()", nowEpochMilli).get());
    }

    @Test
    public void testDay() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-03T00:00:00.000Z"),
                DateExpressionParser.parse("day()", nowEpochMilli).get());
    }

    @Test
    public void testWeek() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-02T00:00:00.000Z"),
                DateExpressionParser.parse("week()", nowEpochMilli).get());
    }

    @Test
    public void testMonth() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-01T00:00:00.000Z"),
                DateExpressionParser.parse("month()", nowEpochMilli).get());
    }

    @Test
    public void testYear() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-01-01T00:00:00.000Z"),
                DateExpressionParser.parse("year()", nowEpochMilli).get());
    }

    @Test
    public void testSecondPlus() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-07T01:22:33.000Z"),
                DateExpressionParser.parse("second()+4d", nowEpochMilli).get());
    }

    @Test
    public void testHourMinus() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-03T05:00:00.000Z"),
                DateExpressionParser.parse("hour()+5h-1h", nowEpochMilli).get());
    }

    @Test
    public void testWeekPlus() {
        Assertions.assertEquals(
                ZonedDateTime.parse("2015-02-09T00:00:00.000Z"),
                DateExpressionParser.parse("week()+1w", nowEpochMilli).get());
    }

    @Test
    public void testMissingTime() {
        testError("+1w", "You must specify a time or time constant before adding or subtracting duration '1w'.");
    }

    @Test
    public void testTwoTimes1() {
        testError("now()+now()", "Text '+' could not be parsed at index 1");
    }

    @Test
    public void testTwoTimes2() {
        testError("now() now()", "Attempt to set the date and time twice with 'now()'. You cannot have more than one declaration of date and time.");
    }


    @Test
    public void testMissingSign1() {
        testError("now() 1w", "You must specify a plus or minus operation before duration '1w'.");
    }

    @Test
    public void testMissingSign2() {
        testError("1w", "You must specify a plus or minus operation before duration '1w'.");
    }

    private void testError(final String expression, final String expectedMessage) {
        DateTimeException dateTimeException = null;
        try {
            DateExpressionParser.parse(expression, nowEpochMilli).get();
        } catch (DateTimeException e) {
            dateTimeException = e;
        }
        Assertions.assertEquals(expectedMessage, dateTimeException.getMessage());
    }
}
