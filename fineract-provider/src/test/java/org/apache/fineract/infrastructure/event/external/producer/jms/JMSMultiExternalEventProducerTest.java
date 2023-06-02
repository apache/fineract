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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.fineract.avro.MessageV1;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.messaging.jms.MessageFactory;
import org.apache.fineract.infrastructure.core.service.HashingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JMSMultiExternalEventProducerTest {

    private static final int PRODUCER_COUNT = 3;
    private final Random rnd = new Random();

    @Mock
    private Destination destination;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MessageFactory messageFactory;

    @Mock
    private HashingService hashingService;

    private AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    @Mock
    private Connection connection;
    @Mock
    private Session session1;
    @Mock
    private Session session2;
    @Mock
    private Session session3;

    @Mock
    private MessageProducer producer1;
    @Mock
    private MessageProducer producer2;
    @Mock
    private MessageProducer producer3;

    private JMSMultiExternalEventProducer underTest;

    private FineractProperties fineractProperties;

    @BeforeEach
    public void setUp() throws Exception {
        FineractProperties.FineractExternalEventsProducerJmsProperties jms = new FineractProperties.FineractExternalEventsProducerJmsProperties();
        jms.setProducerCount(PRODUCER_COUNT);
        FineractProperties.FineractExternalEventsProducerProperties producer = new FineractProperties.FineractExternalEventsProducerProperties();
        producer.setJms(jms);
        FineractProperties.FineractExternalEventsProperties external = new FineractProperties.FineractExternalEventsProperties();
        external.setProducer(producer);
        FineractProperties.FineractEventsProperties events = new FineractProperties.FineractEventsProperties();
        events.setExternal(external);
        fineractProperties = new FineractProperties();
        fineractProperties.setEvents(events);
        underTest = new JMSMultiExternalEventProducer(destination, connectionFactory, messageFactory, taskExecutor, hashingService,
                fineractProperties);

        given(connectionFactory.createConnection()).willReturn(connection);
        given(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).willReturn(session1, session2, session3);
        given(session1.createProducer(destination)).willReturn(producer1);
        given(session2.createProducer(destination)).willReturn(producer2);
        given(session3.createProducer(destination)).willReturn(producer3);
    }

    @AfterEach
    public void tearDown() throws Exception {
        verify(session1).close();
        verify(session2).close();
        verify(session3).close();
    }

    @Test
    public void testSendEventsShouldWork() throws Exception {
        // given
        byte[] msg1 = createMessage();
        List<byte[]> messages = new ArrayList<>();
        messages.add(msg1);
        Map<Long, List<byte[]>> partitions = Map.of(1L, messages);

        BytesMessage bytesMsg1 = Mockito.mock(BytesMessage.class);
        given(messageFactory.createByteMessage(msg1)).willReturn(bytesMsg1);
        given(hashingService.consistentHash(1L, PRODUCER_COUNT)).willReturn(0);
        // when
        underTest.sendEvents(partitions);
        // then
        verify(producer1).send(destination, bytesMsg1);
    }

    @Test
    public void testSendEventsBalancesBetweenProducers() throws Exception {
        // given
        byte[] msg1 = createMessage();
        byte[] msg2 = createMessage();
        byte[] msg3 = createMessage();
        List<byte[]> messages1 = new ArrayList<>();
        messages1.add(msg1);
        List<byte[]> messages2 = new ArrayList<>();
        messages2.add(msg2);
        List<byte[]> messages3 = new ArrayList<>();
        messages3.add(msg3);
        Map<Long, List<byte[]>> partitions = Map.of(1L, messages1, 2L, messages2, 3L, messages3);

        BytesMessage bytesMsg1 = Mockito.mock(BytesMessage.class);
        BytesMessage bytesMsg2 = Mockito.mock(BytesMessage.class);
        BytesMessage bytesMsg3 = Mockito.mock(BytesMessage.class);
        given(messageFactory.createByteMessage(msg1)).willReturn(bytesMsg1);
        given(messageFactory.createByteMessage(msg2)).willReturn(bytesMsg2);
        given(messageFactory.createByteMessage(msg3)).willReturn(bytesMsg3);
        given(hashingService.consistentHash(1L, PRODUCER_COUNT)).willReturn(0);
        given(hashingService.consistentHash(2L, PRODUCER_COUNT)).willReturn(1);
        given(hashingService.consistentHash(3L, PRODUCER_COUNT)).willReturn(2);
        // when
        underTest.sendEvents(partitions);
        // then
        verify(producer1).send(destination, bytesMsg1);
        verify(producer2).send(destination, bytesMsg2);
        verify(producer3).send(destination, bytesMsg3);
    }

    private byte[] createMessage() throws IOException {
        MessageV1 messageV1 = new MessageV1();
        messageV1.setId(rnd.nextInt());
        messageV1.setSource("");
        messageV1.setBusinessDate("");
        messageV1.setCategory("");
        messageV1.setCreatedAt("");
        messageV1.setDataschema("");
        messageV1.setIdempotencyKey("");
        messageV1.setTenantId("");
        messageV1.setType("");
        messageV1.setData(ByteBuffer.wrap(new byte[0]));
        return messageV1.toByteBuffer().array();
    }

}
