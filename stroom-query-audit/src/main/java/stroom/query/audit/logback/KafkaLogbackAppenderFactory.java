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
