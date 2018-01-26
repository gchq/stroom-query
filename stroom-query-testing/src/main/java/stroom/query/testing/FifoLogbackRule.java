package stroom.query.testing;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.query.audit.logback.FifoLogbackAppender;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FifoLogbackRule extends ExternalResource {
    protected static final Logger LOGGER = LoggerFactory.getLogger(FifoLogbackRule.class);

    @Override
    protected void before() throws Throwable {
        FifoLogbackAppender.popLogs();
    }

    @Override
    protected void after() {
    }

    public void checkAuditLogs(final int expected) {
        final List<Object> records = FifoLogbackAppender.popLogs();

        LOGGER.info(String.format("Expected %d records, received %d", expected, records.size()));

        assertEquals(expected, records.size());
    }
}
