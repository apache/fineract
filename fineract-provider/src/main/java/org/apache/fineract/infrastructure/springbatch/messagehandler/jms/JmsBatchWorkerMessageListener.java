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

import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.springbatch.ContextualMessage;
import org.apache.fineract.infrastructure.springbatch.InputChannelInterceptor;
import org.apache.fineract.infrastructure.springbatch.messagehandler.StepExecutionRequestHandler;
import org.apache.fineract.infrastructure.springbatch.messagehandler.conditions.jms.JmsWorkerCondition;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.jms.support.converter.MessagingMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(JmsWorkerCondition.class)
public class JmsBatchWorkerMessageListener implements MessageListener, InitializingBean {

    private final StepExecutionRequestHandler stepExecutionRequestHandler;
    private final InputChannelInterceptor inputInterceptor;
    private MessagingMessageConverter converter;

    @Override
    public void afterPropertiesSet() throws Exception {
        converter = new MessagingMessageConverter();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public void onMessage(jakarta.jms.Message message) {
        try {
            Message<ContextualMessage> msg = (Message<ContextualMessage>) converter.fromMessage(message);
            log.debug("Received JMS partition message {}", msg);
            Message<StepExecutionRequest> requestMessage = inputInterceptor.beforeHandleMessage(msg);
            stepExecutionRequestHandler.handle(requestMessage.getPayload());
        } catch (Exception e) {
            log.error("Exception while processing JMS message", e);
        }

        try {
            message.acknowledge();
        } catch (JMSException e) {
            throw new RuntimeException("Unable to acknowledge message", e);
        }
    }
}
