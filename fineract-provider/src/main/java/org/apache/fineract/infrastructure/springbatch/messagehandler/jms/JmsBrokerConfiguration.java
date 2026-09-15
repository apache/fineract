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

import java.util.Map;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.ContextualMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@ConditionalOnProperty(value = "fineract.remote-job-message-handler.jms.enabled", havingValue = "true")
public class JmsBrokerConfiguration {

    @Autowired
    private FineractProperties fineractProperties;
    private static final int PREFETCH_MESSAGE_NUMBER = 1;
    private static final String TYPE_ID_PROPERTY = "_type";
    private static final String CONTEXTUAL_MESSAGE_TYPE_ID = "contextualMessage";

    /**
     * Partition requests travel as JSON rather than as Java-serialized {@code ObjectMessage}s.
     * <p>
     * Java serialization ties the wire format to the exact field layout of Spring Batch's {@code StepExecutionRequest},
     * which changed between Batch 5 and Batch 6 without a new {@code serialVersionUID} - so a manager and a worker on
     * different versions could not exchange messages at all. JSON matches on field names instead, which is what the
     * Kafka transport has always done here ({@code KafkaManagerConfig} / {@code KafkaWorkerConfig}), and it tolerates
     * fields being added or removed.
     * <p>
     * The type id travels under a stable alias rather than a fully qualified class name, so moving or renaming the
     * payload class does not become a wire change.
     */
    @Bean
    public MessageConverter batchPartitionMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY);
        converter.setTypeIdMappings(Map.of(CONTEXTUAL_MESSAGE_TYPE_ID, ContextualMessage.class));
        return converter;
    }

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(fineractProperties.getRemoteJobMessageHandler().getJms().getBrokerUrl());
        connectionFactory.setTrustAllPackages(true);
        ActiveMQPrefetchPolicy activeMQPrefetchPolicy = new ActiveMQPrefetchPolicy();
        activeMQPrefetchPolicy.setAll(PREFETCH_MESSAGE_NUMBER);
        connectionFactory.setPrefetchPolicy(activeMQPrefetchPolicy);
        FineractProperties.FineractRemoteJobMessageHandlerJmsProperties jmsProps = fineractProperties.getRemoteJobMessageHandler().getJms();
        if (jmsProps.isBrokerPasswordProtected()) {
            connectionFactory.setUserName(jmsProps.getBrokerUsername());
            connectionFactory.setPassword(jmsProps.getBrokerPassword());
        }
        return connectionFactory;
    }
}
