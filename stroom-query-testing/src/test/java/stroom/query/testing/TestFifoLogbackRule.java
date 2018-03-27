package stroom.query.testing;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.logback.FifoLogbackAppender;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static stroom.query.testing.FifoLogbackRule.containsAllOf;

public class TestFifoLogbackRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestFifoLogbackRule.class);

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    @Test
    public void testSimpleSequence() {
        final FifoLogbackAppender<String> logbackAppender = new FifoLogbackAppender<>();

        logbackAppender.doAppend("my special log one");
        logbackAppender.doAppend("my special log two");
        logbackAppender.doAppend("my special log three");

        auditLogRule.check().thereAreAtLeast(3)
                .containsOrdered(containsAllOf("special", "one"))
                .containsOrdered(containsAllOf("special", "two"))
                .containsOrdered(containsAllOf("special", "three"));
    }

    @Test
    public void testSimpleSequenceFailure() {
        final FifoLogbackAppender<String> logbackAppender = new FifoLogbackAppender<>();

        logbackAppender.doAppend("my special log one");
        logbackAppender.doAppend("my special log two");
        logbackAppender.doAppend("my special log three");

        final FifoLogbackRule.LogChecker checker = auditLogRule.check().thereAreAtLeast(3)
                .containsOrdered(containsAllOf("special", "one"))
                .containsOrdered(containsAllOf("special", "three"));
        try {
            checker.containsOrdered(containsAllOf("special", "two")); // should fail here
            fail("Should have failed to find 'two' after looking for 'three'");
        } catch (final AssertionError e) {
            // good!
        }
    }

    @Test
    public void testContainsAnywhere() {
        final FifoLogbackAppender<String> logbackAppender = new FifoLogbackAppender<>();

        logbackAppender.doAppend("my special log one");
        logbackAppender.doAppend("my special log two");
        logbackAppender.doAppend("my special log three");

        final FifoLogbackRule.LogChecker checker = auditLogRule.check().thereAreAtLeast(3)
                .containsAnywhere(containsAllOf("special", "three"))
                .containsAnywhere(containsAllOf("special", "two"))
                .containsAnywhere(containsAllOf("special", "one"));

        try {
            checker.containsOrdered(containsAllOf("special", "four")); // should fail here
            fail("Should have failed to find 'four'");
        } catch (final AssertionError e) {
            // good!
        }
    }

    @Test
    public void testUncheckedLots() {
        final FifoLogbackAppender<String> logbackAppender = new FifoLogbackAppender<>();

        logbackAppender.doAppend("my special log one");
        logbackAppender.doAppend("my special log two");
        logbackAppender.doAppend("my special log three");

        final FifoLogbackRule.LogChecker logChecker = auditLogRule.check().thereAreAtLeast(3)
                .containsOrdered(containsAllOf("special", "one"))
                // miss out two
                .containsOrdered(containsAllOf("special", "three"));

        try {
            auditLogRule.after();
            fail("Expected an error for failing to check on a log");
        } catch (final AssertionError e) {
            // Good!
            LOGGER.info("Correctly thrown error for failing to check on a log");

            // Now add a check for the second log to allow the rule.after to pass
            logChecker.containsAnywhere(containsAllOf("special", "two"));
        }
    }

    @Test
    public void testPredicate() {
        final String logToCheck = "special one";
        final Predicate<String> pass = FifoLogbackRule.containsAllOf("special", "one");
        final Predicate<String> fail = FifoLogbackRule.containsAllOf("special", "two");

        assertTrue(pass.test(logToCheck));
        assertFalse(fail.test(logToCheck));
    }
}
