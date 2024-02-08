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
package org.apache.fineract.infrastructure.springbatch.messagehandler.spring;

import org.apache.fineract.infrastructure.springbatch.InputChannelInterceptor;
import org.apache.fineract.infrastructure.springbatch.messagehandler.conditions.spring.SpringEventWorkerCondition;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.event.core.MessagingEvent;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.handler.LoggingHandler;

@Configuration
@EnableBatchIntegration
@Conditional(SpringEventWorkerCondition.class)
public class SpringEventWorkerConfig {

    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private InputChannelInterceptor inputInterceptor;

    @Bean
    public IntegrationFlow inboundFlow() {
        return IntegrationFlow.from(eventListener()) //
                .log(LoggingHandler.Level.DEBUG) //
                .channel(inboundRequests) //
                .intercept(inputInterceptor) //
                .get(); //
    }

    @Bean
    public ApplicationEventListeningMessageProducer eventListener() {
        ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
        producer.setEventTypes(MessagingEvent.class);
        return producer;
    }
}
