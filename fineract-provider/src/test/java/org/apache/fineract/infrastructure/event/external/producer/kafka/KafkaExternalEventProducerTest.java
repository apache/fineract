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

import static org.mockito.Mockito.times;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
class KafkaExternalEventProducerTest {

    public static final String TOPIC_NAME = "unit-test";
    @Mock
    private KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Mock
    private SendResult<Long, byte[]> sendResult1;

    @Mock
    private SendResult<Long, byte[]> sendResult2;

    @Mock
    private SendResult<Long, byte[]> sendResult3;

    private static final byte[] FIRST = "first".getBytes(Charset.defaultCharset());
    private static final byte[] SECOND = "second".getBytes(Charset.defaultCharset());
    private static final byte[] THIRD = "third".getBytes(Charset.defaultCharset());

    @Test
    public void testSendOK() {
        // given
        KafkaExternalEventProducer underTest = new KafkaExternalEventProducer(kafkaTemplate, createProperties());
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 1L, FIRST)).thenReturn(CompletableFuture.completedFuture(sendResult1));
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 1L, SECOND)).thenReturn(CompletableFuture.completedFuture(sendResult2));
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 2L, THIRD)).thenReturn(CompletableFuture.completedFuture(sendResult2));

        // when
        underTest.sendEvents(Map.of(1L, List.of(FIRST, SECOND), 2L, List.of(THIRD)));

        // then
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 1L, FIRST);
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 1L, SECOND);
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 2L, THIRD);
        Mockito.verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    public void testSendOneFails() {
        // given
        KafkaExternalEventProducer underTest = new KafkaExternalEventProducer(kafkaTemplate, createProperties());
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 1L, FIRST)).thenReturn(CompletableFuture.completedFuture(sendResult1));
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 1L, SECOND)).thenReturn(CompletableFuture.completedFuture(sendResult2));
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 2L, THIRD))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka error")));

        // when
        Assertions.assertThrows(RuntimeException.class, () -> underTest.sendEvents(Map.of(1L, List.of(FIRST, SECOND), 2L, List.of(THIRD))));

        // then
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 1L, FIRST);
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 1L, SECOND);
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 2L, THIRD);
        Mockito.verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    public void testTimeOut() {
        // given
        KafkaExternalEventProducer underTest = new KafkaExternalEventProducer(kafkaTemplate, createProperties());
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 1L, FIRST)).thenReturn(CompletableFuture.completedFuture(sendResult1));
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 1L, SECOND)).thenReturn(CompletableFuture.completedFuture(sendResult2));
        Mockito.when(kafkaTemplate.send(TOPIC_NAME, 2L, THIRD)).thenReturn(new CompletableFuture<>());

        // when
        Assertions.assertThrows(RuntimeException.class, () -> underTest.sendEvents(Map.of(1L, List.of(FIRST, SECOND), 2L, List.of(THIRD))));

        // then
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 1L, FIRST);
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 1L, SECOND);
        Mockito.verify(kafkaTemplate, times(1)).send(TOPIC_NAME, 2L, THIRD);
        Mockito.verifyNoMoreInteractions(kafkaTemplate);
    }

    @NotNull
    private static FineractProperties createProperties() {
        FineractProperties props = new FineractProperties();

        FineractProperties.FineractEventsProperties fineractEventsProperties = new FineractProperties.FineractEventsProperties();
        props.setEvents(fineractEventsProperties);

        FineractProperties.FineractExternalEventsProperties externalEventsProperties = new FineractProperties.FineractExternalEventsProperties();
        fineractEventsProperties.setExternal(externalEventsProperties);

        FineractProperties.FineractExternalEventsProducerProperties producer = new FineractProperties.FineractExternalEventsProducerProperties();
        externalEventsProperties.setProducer(producer);

        FineractProperties.FineractExternalEventsProducerKafkaProperties kafkaProperties = new FineractProperties.FineractExternalEventsProducerKafkaProperties();
        producer.setKafka(kafkaProperties);

        FineractProperties.KafkaTopicProperties kafkaTopicProperties = new FineractProperties.KafkaTopicProperties();
        kafkaProperties.setTopic(kafkaTopicProperties);
        kafkaProperties.setTimeoutInSeconds(1);

        kafkaTopicProperties.setName(TOPIC_NAME);
        return props;
    }

}
