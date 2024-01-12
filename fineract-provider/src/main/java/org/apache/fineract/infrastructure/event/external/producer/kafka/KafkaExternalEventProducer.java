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
package org.apache.fineract.infrastructure.event.external.producer.kafka;

import static org.apache.fineract.infrastructure.core.diagnostics.performance.MeasuringUtil.measure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.event.external.exception.AcknowledgementTimeoutException;
import org.apache.fineract.infrastructure.event.external.producer.ExternalEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(value = "fineract.events.external.producer.kafka.enabled", havingValue = "true")
@AllArgsConstructor
public class KafkaExternalEventProducer implements ExternalEventProducer {

    @Autowired
    private KafkaTemplate<Long, byte[]> externalEventsKafkaTemplate;

    @Autowired
    private FineractProperties fineractProperties;

    @Override
    public void sendEvents(Map<Long, List<byte[]>> partitions) throws AcknowledgementTimeoutException {
        FineractProperties.FineractExternalEventsProducerKafkaProperties kafkaProperties = fineractProperties.getEvents().getExternal()
                .getProducer().getKafka();
        String topicName = kafkaProperties.getTopic().getName();
        List<CompletableFuture<SendResult<Long, byte[]>>> sendResults = new ArrayList<>();
        measure(() -> {
            Set<Long> keys = partitions.keySet();
            for (Map.Entry<Long, List<byte[]>> entry : partitions.entrySet()) {
                for (byte[] message : entry.getValue()) {
                    sendResults.add(externalEventsKafkaTemplate.send(topicName, entry.getKey(), message));
                }
            }

            try {
                CompletableFuture<Void> allOf = CompletableFuture.allOf(sendResults.toArray(new CompletableFuture[0]));
                allOf.get(kafkaProperties.getTimeoutInSeconds(), TimeUnit.SECONDS);
            } catch (Exception exception) {
                throw new RuntimeException("Could not send the messages", exception);
            }
        }, timeTaken -> {
            if (log.isDebugEnabled()) {
                int eventCount = partitions.values().stream().map(Collection::size).reduce(0, Integer::sum);
                int msgPerSec = (int) (((double) eventCount / timeTaken.toMillis()) * 1000);
                log.debug("Sent messages with {} msg/s", msgPerSec);
            }
        });
    }
}
