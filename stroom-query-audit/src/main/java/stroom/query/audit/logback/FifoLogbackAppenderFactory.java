package stroom.query.audit.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

@JsonTypeName("fifo")
public class FifoLogbackAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {
    public static final String APPENDER_NAME = "fifoAudit";

    @Override
    public Appender<E> build(final LoggerContext context,
                             final String applicationName,
                             final LayoutFactory<E> layoutFactory,
                             final LevelFilterFactory<E> levelFilterFactory,
                             final AsyncAppenderFactory<E> asyncAppenderFactory) {
        final Appender<E> appender = new FifoLogbackAppender<>();

        appender.setContext(context);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();
        appender.setName(APPENDER_NAME);

        return appender;
    }
}
