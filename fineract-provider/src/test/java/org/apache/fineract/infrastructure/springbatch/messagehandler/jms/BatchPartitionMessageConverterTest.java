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
package org.apache.fineract.infrastructure.springbatch.messagehandler.jms;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.springbatch.ContextualMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * The partition transport carries {@link StepExecutionRequest}, whose fields are private and have no setters. Whether
 * JSON can restore it is the whole premise of moving this transport off Java serialization, so it is asserted here
 * rather than assumed.
 */
public class BatchPartitionMessageConverterTest {

    private final MessageConverter converter = new JmsBrokerConfiguration().batchPartitionMessageConverter();

    @Test
    public void restoresTheStepExecutionRequestThroughJson() throws Exception {
        ContextualMessage original = new ContextualMessage();
        original.setStepExecutionRequest(new StepExecutionRequest("loanCOBWorkerStep:partition_3", 42L));
        original.setContext(new FineractContext(null, null, null, null, null));

        ContextualMessage restored = (ContextualMessage) converter.fromMessage(roundTrip(original));

        assertThat(restored.getStepExecutionRequest()).isNotNull();
        assertThat(restored.getStepExecutionRequest().getStepExecutionId())
                .as("a step execution id lost in transit would send the worker looking for a partition that does not exist").isEqualTo(42L);
        assertThat(restored.getStepExecutionRequest().getStepName()).isEqualTo("loanCOBWorkerStep:partition_3");
    }

    @Test
    public void sendsJsonTextRatherThanASerializedObject() throws Exception {
        ContextualMessage message = new ContextualMessage();
        message.setStepExecutionRequest(new StepExecutionRequest("loanCOBWorkerStep:partition_0", 7L));

        TextMessage jmsMessage = (TextMessage) converter.toMessage(message, session());

        assertThat(jmsMessage.getText()).contains("\"stepExecutionId\":7").contains("loanCOBWorkerStep:partition_0");
        assertThat(jmsMessage.getStringProperty("_type")).as("the type id travels as a stable alias, not a class name")
                .isEqualTo("contextualMessage");
    }

    private TextMessage roundTrip(ContextualMessage message) throws Exception {
        TextMessage sent = (TextMessage) converter.toMessage(message, session());
        // ActiveMQ messages are write-only until marked as read back.
        ((ActiveMQTextMessage) sent).setReadOnlyBody(true);
        return sent;
    }

    private Session session() throws Exception {
        Session session = Mockito.mock(Session.class);
        Mockito.when(session.createTextMessage(Mockito.anyString())).thenAnswer((Answer<TextMessage>) invocation -> {
            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(invocation.getArgument(0));
            return message;
        });
        return session;
    }
}
