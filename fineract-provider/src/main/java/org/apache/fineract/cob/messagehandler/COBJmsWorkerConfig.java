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
package org.apache.fineract.cob.messagehandler;

import javax.jms.ConnectionFactory;
import org.apache.fineract.cob.messagehandler.conditions.JmsWorkerCondition;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jms.dsl.Jms;

@Configuration
@EnableBatchIntegration
@Conditional(JmsWorkerCondition.class)
public class COBJmsWorkerConfig {

    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private QueueChannel inboundRequests;
    @Autowired
    private FineractProperties fineractProperties;

    @Bean
    public IntegrationFlow inboundFlow() {
        return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory) //
                .configureListenerContainer(c -> c.subscriptionDurable(true)) //
                .destination(fineractProperties.getRemoteJobMessageHandler().getJms().getRequestQueueName())) //
                .channel(inboundRequests) //
                .log(LoggingHandler.Level.DEBUG) //
                .get();
    }
}
