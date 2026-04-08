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
package org.apache.fineract.command.disruptor.starter;

import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.CommandHookManager;
import org.apache.fineract.command.disruptor.DisruptorCommandProperties;
import org.apache.fineract.command.disruptor.implementation.DisruptorCommandDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DisruptorCommandConfiguration {

    @Bean
    @ConditionalOnMissingBean
    WaitStrategy waitStrategy() {
        return new YieldingWaitStrategy();
    }

    @Bean
    Disruptor<?> disruptor(DisruptorCommandProperties properties, WaitStrategy waitStrategy, CommandHookManager hookManager,
            CommandHandlerManager handlerManager) {
        // TODO: make this more configurable

        @SuppressWarnings("rawtypes")
        Disruptor<DisruptorCommandDispatcher.CommandEvent> disruptor = new Disruptor<>(DisruptorCommandDispatcher.CommandEvent::new,
                properties.getRingBufferSize(), DaemonThreadFactory.INSTANCE, properties.getProducerType(), waitStrategy);

        disruptor.handleEventsWith(new DisruptorCommandDispatcher.CompleteableCommandEventHandler(hookManager, handlerManager));
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());

        return disruptor;
    }
}
