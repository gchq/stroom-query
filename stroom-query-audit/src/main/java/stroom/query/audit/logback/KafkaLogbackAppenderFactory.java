package stroom.query.audit.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

import java.util.List;
import java.util.Properties;

@JsonTypeName("kafka")
public class KafkaLogbackAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {
    public static final String APPENDER_NAME = "kafkaAudit";

    @JsonProperty
    private String topic;

    @JsonProperty
    private List<String> producerConfig;

    @Override
    public Appender<E> build(final LoggerContext context,
                          final String applicationName,
                          final LayoutFactory<E> layoutFactory,
                          final LevelFilterFactory<E> levelFilterFactory,
                          final AsyncAppenderFactory<E> asyncAppenderFactory) {

        final Properties producerConfigProperties = producerConfig.stream()
                .map(s -> s.split("="))
                .filter(values -> values.length == 2)
                .reduce(new Properties(), (props, values) -> {
                    props.setProperty(values[0], values[1]);
                    return props;
                }, (p1, p2) -> {
                    Properties merged = new Properties();
                    merged.putAll(p1);
                    merged.putAll(p2);
                    return merged;
                });

        final Appender<E> appender = new KafkaLogbackAppender<>(producerConfigProperties, topic);

        appender.setContext(context);

        appender.addFilter(levelFilterFactory.build(threshold));
        getFilterFactories().forEach(f -> appender.addFilter(f.build()));
        appender.start();
        appender.setName(APPENDER_NAME);

        return appender;
        //return wrapAsync(appender, asyncAppenderFactory);
    }
}
