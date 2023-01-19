/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.event.external.producer.jms;

import static org.apache.fineract.infrastructure.core.service.MeasuringUtil.measure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.messaging.jms.MessageFactory;
import org.apache.fineract.infrastructure.core.service.HashingService;
import org.apache.fineract.infrastructure.event.external.exception.AcknowledgementTimeoutException;
import org.apache.fineract.infrastructure.event.external.producer.ExternalEventProducer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.events.external.producer.jms.enabled", havingValue = "true")
public class JMSMultiExternalEventProducer implements ExternalEventProducer, InitializingBean, DisposableBean {

    @Qualifier("eventDestination")
    private final Destination destination;

    private final ConnectionFactory connectionFactory;

    private final MessageFactory messageFactory;

    @Qualifier("externalEventJmsProducerExecutor")
    private final AsyncTaskExecutor taskExecutor;

    private final HashingService hashingService;

    private final FineractProperties fineractProperties;

    private final List<MessageProducer> producers = new ArrayList<>();

    private Connection connection;

    @Override
    public void afterPropertiesSet() throws Exception {
        int producerCount = getProducerCount();
        connection = connectionFactory.createConnection();
        for (int i = 0; i < producerCount; i++) {
            // It's crucial to create the session within the loop, otherwise the producers won't be handled as
            // parallel
            // producers
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);
            producers.add(producer);
        }
        log.info("Initialized JMS multi producer for external events with {} parallel producers", producerCount);
    }

    @Override
    public void destroy() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    private int getProducerCount() {
        return fineractProperties.getEvents().getExternal().getProducer().getJms().getProducerCount();
    }

    @Override
    public void sendEvents(Map<Long, List<byte[]>> partitions) throws AcknowledgementTimeoutException {
        Map<Integer, List<byte[]>> indexedPartitions = mapPartitionsToProducers(partitions);
        measure(() -> {
            List<Future<?>> tasks = sendPartitions(indexedPartitions);
            waitForSendingCompletion(tasks);
        }, timeTaken -> {
            if (log.isDebugEnabled()) {
                // in case execution is faster than 1sec
                long seconds = Math.max(timeTaken.toSeconds(), 1L);
                Integer eventCount = partitions.values().stream().map(Collection::size).reduce(0, Integer::sum);
                log.debug("Sent messages with {} msg/s", (eventCount / seconds));
            }
        });
    }

    private List<Future<?>> sendPartitions(Map<Integer, List<byte[]>> indexedPartitions) {
        List<Future<?>> tasks = new ArrayList<>();
        for (Map.Entry<Integer, List<byte[]>> entry : indexedPartitions.entrySet()) {
            Integer producerIndex = entry.getKey();
            List<byte[]> messages = entry.getValue();
            Future<?> future = createSendingTask(producerIndex, messages);
            tasks.add(future);
        }
        return tasks;
    }

    private Future<?> createSendingTask(Integer producerIndex, List<byte[]> messages) {
        return taskExecutor.submit(() -> {
            MessageProducer messageProducer = producers.get(producerIndex);

            for (byte[] message : messages) {
                try {
                    messageProducer.send(destination, messageFactory.createByteMessage(message));
                } catch (JMSException e) {
                    throw new RuntimeException("Error while sending the message", e);
                }
            }
        });
    }

    private Map<Integer, List<byte[]>> mapPartitionsToProducers(Map<Long, List<byte[]>> partitions) {
        Map<Integer, List<byte[]>> indexedPartitions = new HashMap<>();
        for (Map.Entry<Long, List<byte[]>> partition : partitions.entrySet()) {
            Long key = partition.getKey();
            List<byte[]> messages = partition.getValue();

            int producerIndex = hashingService.consistentHash(key, getProducerCount());
            indexedPartitions.putIfAbsent(producerIndex, new ArrayList<>());
            indexedPartitions.get(producerIndex).addAll(messages);
        }
        return indexedPartitions;
    }

    private void waitForSendingCompletion(List<Future<?>> tasks) {
        try {
            for (Future<?> task : tasks) {
                task.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
