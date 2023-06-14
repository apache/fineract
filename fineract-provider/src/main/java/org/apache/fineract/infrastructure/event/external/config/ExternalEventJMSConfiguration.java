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
package org.apache.fineract.infrastructure.event.external.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractExternalEventsProducerJmsProperties;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnProperty(value = "fineract.events.external.producer.jms.enabled", havingValue = "true")
public class ExternalEventJMSConfiguration {

    @Autowired
    private FineractProperties fineractProperties;

    @Bean(name = "externalEventConnectionFactory")
    public CachingConnectionFactory connectionFactory() {
        FineractExternalEventsProducerJmsProperties jmsProps = fineractProperties.getEvents().getExternal().getProducer().getJms();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(jmsProps.getBrokerUrl());
        connectionFactory.setUseAsyncSend(jmsProps.isAsyncSendEnabled());
        connectionFactory.setTrustAllPackages(true);
        if (jmsProps.isBrokerPasswordProtected()) {
            connectionFactory.setUserName(jmsProps.getBrokerUsername());
            connectionFactory.setPassword(jmsProps.getBrokerPassword());
        }
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setSessionCacheSize(jmsProps.getProducerCount());
        cachingConnectionFactory.setReconnectOnException(true);
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        return cachingConnectionFactory;
    }

    @Conditional(EnableExternalEventTopicCondition.class)
    @Bean(name = "externalEventDestination")
    public ActiveMQTopic activeMqTopic() {
        return new ActiveMQTopic(fineractProperties.getEvents().getExternal().getProducer().getJms().getEventTopicName());
    }

    @Conditional(EnableExternalEventQueueCondition.class)
    @Bean(name = "externalEventDestination")
    public ActiveMQQueue activeMqQueue() {
        return new ActiveMQQueue(fineractProperties.getEvents().getExternal().getProducer().getJms().getEventQueueName());
    }

    @Bean(TaskExecutorConstant.EVENT_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor externalEventJmsProducerExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(
                fineractProperties.getEvents().getExternal().getProducer().getJms().getThreadPoolTaskExecutorCorePoolSize());
        threadPoolTaskExecutor
                .setMaxPoolSize(fineractProperties.getEvents().getExternal().getProducer().getJms().getThreadPoolTaskExecutorMaxPoolSize());
        threadPoolTaskExecutor.setThreadNamePrefix("externalEventJms");
        return threadPoolTaskExecutor;
    }
}
