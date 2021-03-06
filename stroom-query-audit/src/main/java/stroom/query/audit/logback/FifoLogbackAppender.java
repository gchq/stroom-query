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

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is a Logback appender that simply puts logs into an in-memory queue.
 *
 * This class is intended for use in integration tests, to prevent those tests needing to connect
 * to kafka, or read files, but they can still check that auditing is happening.
 * @param <E> The log object
 */
public class FifoLogbackAppender<E> extends ContextAwareBase implements Appender<E> {
    private static final ConcurrentLinkedQueue<Object> logs = new ConcurrentLinkedQueue<>();

    private String name;

    public synchronized static List<Object> popLogs() {
        final List<Object> extracted = new ArrayList<>(logs);
        logs.clear();
        return extracted;
    }

    private synchronized static void append(final Object log) {
        logs.add(log);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void doAppend(E e) throws LogbackException {
        FifoLogbackAppender.append(e);
    }

    @Override
    public void setName(String s) {
        this.name = s;
    }

    @Override
    public void addFilter(Filter<E> filter) {

    }

    @Override
    public void clearAllFilters() {

    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return null;
    }

    @Override
    public FilterReply getFilterChainDecision(E e) {
        return FilterReply.ACCEPT;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
