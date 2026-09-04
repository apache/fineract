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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.springbatch.InputChannelInterceptor;
import org.apache.fineract.infrastructure.springbatch.messagehandler.StepExecutionRequestHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

/**
 * Regression test for the batch worker Kafka listener wiring. Since Spring Boot 4 the Kafka auto-configuration (which
 * used to apply {@code @EnableKafka} whenever spring-kafka was on the classpath) lives in the separate
 * {@code spring-boot-kafka} module that Fineract does not depend on. {@link KafkaWorkerConfig} must therefore enable
 * {@code @KafkaListener} processing itself, otherwise {@link KafkaRemoteMessageListener} is silently ignored and the
 * worker never consumes partition requests.
 */
class KafkaWorkerConfigTest {

    private static final String TOPIC_NAME = "job-topic";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withInitializer(context -> {
        PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        placeholderConfigurer.setEnvironment(context.getEnvironment());
        context.getBeanFactory().registerSingleton("propertySourcesPlaceholderConfigurer", placeholderConfigurer);
    }).withBean(FineractProperties.class, KafkaWorkerConfigTest::workerKafkaProperties)
            .withBean("inboundRequests", QueueChannel.class, QueueChannel::new)
            .withBean(StepExecutionRequestHandler.class, () -> mock(StepExecutionRequestHandler.class))
            .withBean(InputChannelInterceptor.class, () -> mock(InputChannelInterceptor.class))
            .withUserConfiguration(KafkaWorkerConfig.class, KafkaRemoteMessageListener.class);

    @Test
    void kafkaListenerIsRegisteredForJobTopicInWorkerMode() {
        contextRunner
                .withPropertyValues("fineract.mode.batch-worker-enabled=true", "fineract.remote-job-message-handler.kafka.enabled=true",
                        "fineract.remote-job-message-handler.kafka.topic.name=" + TOPIC_NAME)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).as("@EnableKafka must register the @KafkaListener annotation processor")
                            .hasBean(KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME);
                    KafkaListenerEndpointRegistry registry = context.getBean(KafkaListenerEndpointRegistry.class);
                    assertThat(registry.getListenerContainers()).hasSize(1);
                    MessageListenerContainer container = registry.getListenerContainers().iterator().next();
                    assertThat(container.getContainerProperties().getTopics()).containsExactly(TOPIC_NAME);
                });
    }

    @Test
    void kafkaListenerIsNotRegisteredWhenKafkaMessageHandlerIsDisabled() {
        contextRunner
                .withPropertyValues("fineract.mode.batch-worker-enabled=true", "fineract.remote-job-message-handler.kafka.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(KafkaRemoteMessageListener.class);
                    assertThat(context).doesNotHaveBean(KafkaListenerEndpointRegistry.class);
                });
    }

    private static FineractProperties workerKafkaProperties() {
        FineractProperties.KafkaConsumerProperties consumerProperties = new FineractProperties.KafkaConsumerProperties();
        consumerProperties.setGroupId("test-group");
        FineractProperties.FineractRemoteJobMessageHandlerKafkaProperties kafkaProperties = new FineractProperties.FineractRemoteJobMessageHandlerKafkaProperties();
        kafkaProperties.setEnabled(true);
        kafkaProperties.setBootstrapServers("localhost:9092");
        kafkaProperties.setConsumer(consumerProperties);
        FineractProperties.FineractRemoteJobMessageHandlerProperties remoteJobMessageHandlerProperties = new FineractProperties.FineractRemoteJobMessageHandlerProperties();
        remoteJobMessageHandlerProperties.setKafka(kafkaProperties);
        FineractProperties fineractProperties = new FineractProperties();
        fineractProperties.setRemoteJobMessageHandler(remoteJobMessageHandlerProperties);
        return fineractProperties;
    }
}
