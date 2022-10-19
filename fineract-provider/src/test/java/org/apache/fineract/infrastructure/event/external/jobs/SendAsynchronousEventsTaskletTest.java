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
package org.apache.fineract.infrastructure.event.external.jobs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.avro.MessageV1;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.external.exception.AcknowledgementTimeoutException;
import org.apache.fineract.infrastructure.event.external.producer.ExternalEventProducer;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventStatus;
import org.apache.fineract.infrastructure.event.external.service.message.MessageFactory;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class SendAsynchronousEventsTaskletTest {

    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private ExternalEventRepository repository;
    @Mock
    private ExternalEventProducer eventProducer;
    @Mock
    private MessageFactory messageFactory;
    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private ByteBufferConverter byteBufferConverter;
    private SendAsynchronousEventsTasklet underTest;
    private RepeatStatus resultStatus;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        configureExternalEventsProducerReadBatchSizeProperty(1000);
        underTest = new SendAsynchronousEventsTasklet(fineractProperties, repository, eventProducer, messageFactory, byteBufferConverter);
    }

    private void configureExternalEventsProducerReadBatchSizeProperty(int readBatchSize) {
        FineractProperties.FineractEventsProperties eventsProperties = new FineractProperties.FineractEventsProperties();
        FineractProperties.FineractExternalEventsProperties externalProperties = new FineractProperties.FineractExternalEventsProperties();
        FineractProperties.FineractExternalEventsProducerProperties externalEventsProducerProperties = new FineractProperties.FineractExternalEventsProducerProperties();
        FineractProperties.FineractExternalEventsProducerJmsProperties externalEventsProducerJMSProperties = new FineractProperties.FineractExternalEventsProducerJmsProperties();
        externalEventsProducerJMSProperties.setEnabled(true);
        externalProperties.setEnabled(true);
        externalEventsProducerProperties.setReadBatchSize(readBatchSize);
        externalEventsProducerProperties.setJms(externalEventsProducerJMSProperties);
        externalProperties.setProducer(externalEventsProducerProperties);
        eventsProperties.setExternal(externalProperties);
        when(fineractProperties.getEvents()).thenReturn(eventsProperties);
    }

    @Test
    public void givenBatchSize2WhenTaskExecutionThenSend2Events() throws Exception {
        // given
        List<ExternalEvent> events = Arrays.asList(new ExternalEvent("aType", "aCategory", "aSchema", new byte[0], "aIdempotencyKey"),
                new ExternalEvent("aType", "aCategory", "aSchema", new byte[0], "aIdempotencyKey"));
        // Dummy Message
        MessageV1 dummyMessage = new MessageV1(1, "aSource", "aType", "nocategory", "aCreateDate", "aBusinessDate", "aTenantId",
                "anidempotencyKey", "aSchema", Mockito.mock(ByteBuffer.class));

        when(repository.findByStatusOrderById(Mockito.any(), Mockito.any())).thenReturn(events);
        when(messageFactory.createMessage(Mockito.any())).thenReturn(dummyMessage);
        when(byteBufferConverter.convert(Mockito.any(ByteBuffer.class))).thenReturn(new byte[0]);
        doNothing().when(eventProducer).sendEvent(Mockito.any());
        // when
        resultStatus = this.underTest.execute(stepContribution, chunkContext);
        // then
        verify(eventProducer, times(2)).sendEvent(new byte[0]);
        verify(repository, times(2)).save(Mockito.any(ExternalEvent.class));
        assertEquals(RepeatStatus.FINISHED, resultStatus);
    }

    @Test
    public void givenBatchSize2WhenEventSendFailsThenExecutionStops() throws Exception {
        // given
        List<ExternalEvent> events = Arrays.asList(new ExternalEvent("aType", "aCategory", "aSchema", new byte[0], "aIdempotencyKey"),
                new ExternalEvent("aType", "aCategory", "aSchema", new byte[0], "aIdempotencyKey"));
        MessageV1 dummyMessage = new MessageV1(1, "aSource", "aType", "nocategory", "aCreateDate", "aBusinessDate", "aTenantId",
                "anidempotencyKey", "aSchema", Mockito.mock(ByteBuffer.class));
        when(repository.findByStatusOrderById(Mockito.any(), Mockito.any())).thenReturn(events);
        when(messageFactory.createMessage(Mockito.any())).thenReturn(dummyMessage);
        when(byteBufferConverter.convert(Mockito.any(ByteBuffer.class))).thenReturn(new byte[0]);
        doThrow(new AcknowledgementTimeoutException("Event Send Exception", new RuntimeException())).when(eventProducer)
                .sendEvent(Mockito.any());
        // when
        resultStatus = this.underTest.execute(stepContribution, chunkContext);
        // then
        verify(repository, times(0)).save(Mockito.any(ExternalEvent.class));
        assertEquals(RepeatStatus.FINISHED, resultStatus);
    }

    @Test
    public void givenOneEventWhenEventSentThenEventStatusUpdates() throws Exception {
        // given
        ArgumentCaptor<ExternalEvent> externalEventArgumentCaptor = ArgumentCaptor.forClass(ExternalEvent.class);
        List<ExternalEvent> events = Arrays.asList(new ExternalEvent("aType", "aCategory", "aSchema", new byte[0], "aIdempotencyKey"));
        MessageV1 dummyMessage = new MessageV1(1, "aSource", "aType", "nocategory", "aCreateDate", "aBusinessDate", "aTenantId",
                "anidempotencyKey", "aSchema", Mockito.mock(ByteBuffer.class));
        when(repository.findByStatusOrderById(Mockito.any(), Mockito.any())).thenReturn(events);
        when(messageFactory.createMessage(Mockito.any())).thenReturn(dummyMessage);
        when(byteBufferConverter.convert(Mockito.any(ByteBuffer.class))).thenReturn(new byte[0]);
        doNothing().when(eventProducer).sendEvent(Mockito.any());
        // when
        resultStatus = this.underTest.execute(stepContribution, chunkContext);
        // then
        verify(messageFactory).createMessage(Mockito.any());
        verify(repository).save(externalEventArgumentCaptor.capture());
        ExternalEvent externalEvent = externalEventArgumentCaptor.getValue();
        assertThat(externalEvent.getStatus()).isEqualTo(ExternalEventStatus.SENT);
        assertEquals(RepeatStatus.FINISHED, resultStatus);
    }
}
