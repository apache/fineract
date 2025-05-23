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
package org.apache.fineract.command.starter;

import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import java.util.List;
import org.apache.fineract.command.core.CommandMiddleware;
import org.apache.fineract.command.core.CommandProperties;
import org.apache.fineract.command.core.CommandRouter;
import org.apache.fineract.command.implementation.DisruptorCommandExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CommandProperties.class)
@ComponentScan("org.apache.fineract.command.core")
@ComponentScan("org.apache.fineract.command.implementation")
class CommandConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "fineract.command.executor", havingValue = "disruptor")
    WaitStrategy waitStrategy() {
        return new YieldingWaitStrategy();
    }

    @Bean
    @ConditionalOnProperty(value = "fineract.command.executor", havingValue = "disruptor")
    Disruptor<?> disruptor(CommandProperties properties, WaitStrategy waitStrategy, List<CommandMiddleware> middlewares,
            CommandRouter router) {
        // TODO: make this more configurable

        // Create the disruptor
        Disruptor<DisruptorCommandExecutor.CommandEvent> disruptor = new Disruptor<>(DisruptorCommandExecutor.CommandEvent::new,
                properties.getRingBufferSize(), DaemonThreadFactory.INSTANCE, properties.getProducerType(), waitStrategy);

        disruptor.handleEventsWith(new DisruptorCommandExecutor.CompleteableCommandEventHandler(middlewares, router));
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());

        // Start the disruptor
        disruptor.start();

        return disruptor;
    }
}
