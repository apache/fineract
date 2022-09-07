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
package org.apache.fineract.infrastructure.springbatch.messagehandler;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.InputChannelInterceptor;
import org.apache.fineract.infrastructure.springbatch.messagehandler.conditions.JmsWorkerCondition;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jms.dsl.Jms;

@Configuration
@EnableBatchIntegration
@Conditional(JmsWorkerCondition.class)
@Import(value = { JmsBrokerConfiguration.class })
public class JmsWorkerConfig {

    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private InputChannelInterceptor inputInterceptor;
    @Autowired
    private FineractProperties fineractProperties;

    @Bean
    public IntegrationFlow inboundFlow(ActiveMQConnectionFactory connectionFactory) {
        return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory) //
                .configureListenerContainer(c -> c.subscriptionDurable(false)) //
                .destination(fineractProperties.getRemoteJobMessageHandler().getJms().getRequestQueueName())) //
                .channel(inboundRequests) //
                .intercept(inputInterceptor).log(LoggingHandler.Level.DEBUG) //
                .get();
    }
}
