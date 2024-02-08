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
package org.apache.fineract.infrastructure.springbatch.messagehandler.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.springbatch.ContextualMessage;
import org.apache.fineract.infrastructure.springbatch.InputChannelInterceptor;
import org.apache.fineract.infrastructure.springbatch.messagehandler.StepExecutionRequestHandler;
import org.apache.fineract.infrastructure.springbatch.messagehandler.conditions.kafka.KafkaWorkerCondition;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Conditional(KafkaWorkerCondition.class)
public class KafkaRemoteMessageListener {

    @Autowired
    private StepExecutionRequestHandler stepExecutionRequestHandler;

    @Autowired
    private InputChannelInterceptor inputInterceptor;

    @KafkaListener(topics = "${fineract.remote-job-message-handler.kafka.topic.name}")
    public void onMessage(@Payload ContextualMessage contextualMessage, Acknowledgment acknowledgment) {
        try {
            log.debug("Received Kafka partition message {}", contextualMessage);
            StepExecutionRequest stepExecutionRequest = inputInterceptor.beforeHandleMessage(contextualMessage);
            stepExecutionRequestHandler.handle(stepExecutionRequest);
        } catch (Exception e) {
            log.error("Exception while processing Kafka message", e);
        }
        acknowledgment.acknowledge();
        log.debug("Message was acknowledged {}", acknowledgment);
    }
}
