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

import static org.apache.fineract.infrastructure.core.diagnostics.performance.MeasuringUtil.measure;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.core.messaging.jms.MessageFactory;
import org.apache.fineract.infrastructure.core.service.HashingService;
import org.apache.fineract.infrastructure.event.external.exception.AcknowledgementTimeoutException;
import org.apache.fineract.infrastructure.event.external.producer.ExternalEventProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.events.external.producer.jms.enabled", havingValue = "true")
public class JMSMultiExternalEventProducer implements ExternalEventProducer {

    @Qualifier("externalEventDestination")
    private final Destination destination;

    @Qualifier("externalEventConnectionFactory")
    private final ConnectionFactory connectionFactory;

    private final MessageFactory messageFactory;

    @Qualifier(TaskExecutorConstant.EVENT_TASK_EXECUTOR_BEAN_NAME)
    private final AsyncTaskExecutor taskExecutor;

    private final HashingService hashingService;

    private final FineractProperties fineractProperties;

    private int getProducerCount() {
        return fineractProperties.getEvents().getExternal().getProducer().getJms().getProducerCount();
    }

    @Override
    public void sendEvents(Map<Long, List<byte[]>> partitions) throws AcknowledgementTimeoutException {
        Map<Integer, List<byte[]>> indexedPartitions = mapPartitionsToProducers(partitions);
        measure(() -> {
            List<Pair<Session, MessageProducer>> producersWithSessions = obtainProducers();
            List<MessageProducer> producers = producersWithSessions.stream().map(Pair::getRight).collect(Collectors.toList());
            List<Session> sessions = producersWithSessions.stream().map(Pair::getLeft).collect(Collectors.toList());
            List<Future<?>> tasks = sendPartitions(indexedPartitions, producers);
            waitForSendingCompletion(tasks);
            closeSessions(sessions);
        }, timeTaken -> {
            if (log.isDebugEnabled()) {
                int eventCount = partitions.values().stream().map(Collection::size).reduce(0, Integer::sum);
                int msgPerSec = (int) (((double) eventCount / timeTaken.toMillis()) * 1000);
                log.debug("Sent messages with {} msg/s", msgPerSec);
            }
        });
    }

    private void closeSessions(List<Session> sessions) {
        // The sessions retrieved from a CachingConnectionFactory needs to be explicitly closed, otherwise we're making
        // orphan sessions, leaking memory
        for (Session session : sessions) {
            try {
                session.close();
            } catch (JMSException e) {
                log.error("Exception while trying to close sessions", e);
            }
        }
    }

    private List<Pair<Session, MessageProducer>> obtainProducers() {
        List<Pair<Session, MessageProducer>> result = new ArrayList<>();
        int producerCount = getProducerCount();
        try {
            // No need to close the connection since it's a pooled one
            Connection connection = connectionFactory.createConnection();
            for (int i = 0; i < producerCount; i++) {
                // It's crucial to create the session within the loop, otherwise the producers won't be handled as
                // parallel
                // producers
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(destination);
                result.add(new ImmutablePair<>(session, producer));
            }
        } catch (JMSException e) {
            throw new RuntimeException("Error while obtaining message producers", e);
        }
        return result;
    }

    private List<Future<?>> sendPartitions(Map<Integer, List<byte[]>> indexedPartitions, List<MessageProducer> producers) {
        List<Future<?>> tasks = new ArrayList<>();
        for (Map.Entry<Integer, List<byte[]>> entry : indexedPartitions.entrySet()) {
            Integer producerIndex = entry.getKey();
            MessageProducer producer = producers.get(producerIndex);
            List<byte[]> messages = entry.getValue();
            Future<?> future = createSendingTask(producer, messages);
            tasks.add(future);
        }
        return tasks;
    }

    private Future<?> createSendingTask(MessageProducer messageProducer, List<byte[]> messages) {
        return taskExecutor.submit(() -> {
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
