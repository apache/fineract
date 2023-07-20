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

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.messagehandler.conditions.jms.JmsWorkerCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration(proxyBeanMethods = false)
@Conditional(JmsWorkerCondition.class)
@Import(value = { JmsBrokerConfiguration.class })
public class JmsWorkerConfig {

    @Autowired
    private FineractProperties fineractProperties;

    @Bean
    public DefaultJmsListenerContainerFactory jmsBatchWorkerListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConcurrency("1-1"); // at least one consumer and at most one consumer
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(false);
        factory.setSessionTransacted(false);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;
    }

    @Bean
    public DefaultMessageListenerContainer jmsBatchWorkerMessageListenerContainer(
            @Qualifier("jmsBatchWorkerListenerContainerFactory") DefaultJmsListenerContainerFactory factory,
            JmsBatchWorkerMessageListener messageListener) {
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setDestination(fineractProperties.getRemoteJobMessageHandler().getJms().getRequestQueueName());
        endpoint.setMessageListener(messageListener);
        return factory.createListenerContainer(endpoint);
    }

}
