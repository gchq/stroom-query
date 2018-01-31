package stroom.query.testing;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.logback.FifoLogbackAppender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * This rule allows a test to check that the audit logs are being received by the singleton FIFO logback appender.
 *
 * This rule insists that all audit logs are accounted for within a test using a Log Checker.
 *
 * A new instance of Log Checker is created with the check() function.
 * This will pop any logs and allow the tester to make assertions on them.
 */
public class FifoLogbackRule extends ExternalResource {
    protected static final Logger LOGGER = LoggerFactory.getLogger(FifoLogbackRule.class);

    private final List<String> logsThisTest = new ArrayList<>();
    private final List<Predicate<String>> predicates = new ArrayList<>();

    @Override
    protected void before() throws Throwable {
        FifoLogbackAppender.popLogs();
        logsThisTest.clear();
        predicates.clear();
    }

    @Override
    protected void after() {
        // All audit logs should be checked, so now we will check that all logs
        // can be matched up with predicates.
        // This is to ensure that there aren't any completely left field logs that
        // the tester is not aware of.

        logsThisTest.forEach(log -> {
            boolean match = predicates.stream().anyMatch(a -> a.test(log));

            try {
                assertTrue(match);
            } catch (final AssertionError e) {
                LOGGER.error(String.format("A Log was seen that didn't match any of the predicates given\n%s", log));
                throw e;
            }
        });
    }

    /**
     * Begin the process of detailed examination of the accumulated logs.
     * @return A log checking process.
     */
    public LogChecker check() {
        return new LogChecker();
    }

    /**
     * Checking the logs will involve several chained assertions.
     * This takes a dump of all the accumulated logs, converts them to string,
     * this then allows simple string matching.
     */
    public class LogChecker {
        private final List<String> logsThisCheck;
        private int currentIndex = 0;

        private LogChecker() {
            this.logsThisCheck = FifoLogbackAppender.popLogs().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            FifoLogbackRule.this.logsThisTest.addAll(this.logsThisCheck);
        }

        /**
         * Sometimes REST interfaces get called twice, not sure why,
         * this is why it is important to verify that the right logs are received in order.
         * @param minimum The minimum expected number of logs
         * @return The current log checker (this)
         */
        public LogChecker thereAreAtLeast(final int minimum) {
            final boolean pass = this.logsThisCheck.size() >= minimum;

            try {
                assertTrue(pass);
            } catch (final AssertionError e) {
                LOGGER.error(String.format("There were not enough audit logs, expected %d", minimum), e);
                throw e;
            }
            return this;
        }

        /**
         * When it is unclear what order the logs will appear, use this function to simply check that
         * a specific log exists
         * @param filter The filter to apply, any match will result in success
         * @return The current log checker (this)
         */
        public LogChecker containsAnywhere(final Predicate<String> filter) {
            FifoLogbackRule.this.predicates.add(filter);

            final boolean found = this.logsThisCheck.stream().anyMatch(filter);
            try {
                assertTrue(found);
            } catch (AssertionError e) {
                LOGGER.error("Could not find a log that matched the given filter", e);
                throw e;
            }
            return this;
        }

        /**
         * Check that the next log matches the given filter.
         * If any calls are repeated, it should effectively skip over them.
         * As long as it can always find a match going forward.
         * @param filter The filter to apply
         * @return The current log checker (this)
         */
        public LogChecker containsOrdered(final Predicate<String> filter) {
            FifoLogbackRule.this.predicates.add(filter);

            final int startIndex = currentIndex;
            boolean found = false;
            for (currentIndex = startIndex; currentIndex < this.logsThisCheck.size(); currentIndex++) {
                if (filter.test(this.logsThisCheck.get(currentIndex))) {
                    found = true;
                    this.currentIndex++;
                    break;
                }
            }

            try {
                assertTrue(found);
            } catch (AssertionError e) {
                LOGGER.error("Could not find an ordered log that matched the given filter", e);
                throw e;
            }
            return this;
        }
    }

    /**
     * Utility function to generate a predicate that checks for presence of all strings
     * @param patterns The patterns that must all match
     * @return The predicate
     */
    public static Predicate<String> containsAllOf(final String ... patterns) {
        return s -> Stream.of(patterns).filter(s::contains).count() == patterns.length;
    }
}
