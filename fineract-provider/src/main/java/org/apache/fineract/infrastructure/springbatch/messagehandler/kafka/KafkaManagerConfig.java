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

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.ContextualMessage;
import org.apache.fineract.infrastructure.springbatch.OutputChannelInterceptor;
import org.apache.fineract.infrastructure.springbatch.messagehandler.conditions.kafka.KafkaManagerCondition;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.support.GenericMessage;

@Configuration
@EnableBatchIntegration
@Conditional(KafkaManagerCondition.class)
public class KafkaManagerConfig {

    @Autowired
    private DirectChannel outboundRequests;
    @Autowired
    private OutputChannelInterceptor outputInterceptor;
    @Autowired
    private FineractProperties fineractProperties;

    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>(
                fineractProperties.getRemoteJobMessageHandler().getKafka().getProducer().getExtraPropertiesMap());
        props.put(BOOTSTRAP_SERVERS_CONFIG, fineractProperties.getRemoteJobMessageHandler().getKafka().getBootstrapServers());
        props.put(KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<?, ?> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public IntegrationFlow outboundFlow(KafkaTemplate<?, ?> kafkaTemplate) {
        FineractProperties.KafkaTopicProperties topic = fineractProperties.getRemoteJobMessageHandler().getKafka().getTopic();

        KafkaProducerMessageHandler<?, ?> messageHandler = new KafkaProducerMessageHandler<>(kafkaTemplate);
        messageHandler.setTopicExpression(new LiteralExpression(topic.getName()));
        messageHandler.setPartitionIdExpression(new FunctionExpression<GenericMessage<ContextualMessage>>(
                message -> message.getPayload().getStepExecutionRequest().getStepExecutionId() % topic.getPartitions()));

        return IntegrationFlow.from(outboundRequests) //
                .intercept(outputInterceptor) //
                .log(LoggingHandler.Level.DEBUG) //
                .handle(messageHandler).get();
    }

}
