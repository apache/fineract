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
package org.apache.fineract.infrastructure.event.external.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@DirtiesContext
@TestPropertySource(properties = { "fineract.events.external.enabled=true" })
public class EventsJMSIntegrationTest {

    @Autowired
    @Qualifier("outboundRequestsEvents")
    private DirectChannel outboundRequestsEvents;

    @Autowired
    private ContextConfiguration.TestChannelInterceptor testChannelInterceptor;

    @Autowired
    private ExternalEventProducer underTest;

    @Test
    public void testJmsDownstreamChannelIntegration() {
        assertThat(outboundRequestsEvents.getSubscriberCount()).isEqualTo(1);
    }

    @Test
    void given2EventsThenOutBoundChannelIsInvokedTwice() {
        // when
        underTest.sendEvent(new byte[0]);
        underTest.sendEvent(new byte[0]);
        // then
        assertTrue(outboundRequestsEvents.getInterceptors().contains(this.testChannelInterceptor));
        assertThat(testChannelInterceptor.getInvoked()).isEqualTo(2);

    }

    @Configuration
    @IntegrationComponentScan
    @EnableIntegration
    public static class ContextConfiguration {

        private static ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");

        @Bean
        public DirectChannel outboundRequestsEvents() {
            return new DirectChannel();
        }

        @Bean
        public IntegrationFlow outboundFlow() {
            return IntegrationFlows.from("outboundRequestsEvents") //
                    .log(LoggingHandler.Level.DEBUG) //
                    .handle(Jms.outboundAdapter(connectionFactory).destination("destinationChannel")).get();
        }

        @Component
        @GlobalChannelInterceptor(patterns = "outboundRequestsEvents")
        public static class TestChannelInterceptor implements ChannelInterceptor {

            private final AtomicInteger invoked = new AtomicInteger();

            public int getInvoked() {
                return invoked.get();
            }

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                this.invoked.incrementAndGet();
                return message;
            }

        }

    }
}
