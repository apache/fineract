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

package org.apache.fineract.infrastructure.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
public class SpringConfig {

    private static final int AWAIT_TERMINATION_SECONDS = 60;

    private final FineractProperties fineractProperties;

    public SpringConfig(FineractProperties fineractProperties) {
        this.fineractProperties = fineractProperties;
    }

    private int getEventExecutorCorePoolSize() {
        int configured = fineractProperties.getTaskExecutor().getEventTaskExecutorCorePoolSize();
        if (configured > 0) {
            return configured;
        }
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    private int getEventExecutorMaxPoolSize() {
        int configured = fineractProperties.getTaskExecutor().getEventTaskExecutorMaxPoolSize();
        if (configured > 0) {
            return configured;
        }
        return Runtime.getRuntime().availableProcessors() * 5;
    }

    @Bean(name = "fineractEventExecutor")
    public ThreadPoolTaskExecutor fineractEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(getEventExecutorCorePoolSize());
        executor.setMaxPoolSize(getEventExecutorMaxPoolSize());
        executor.setThreadNamePrefix("FineractEvent-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        return executor;
    }

    @Bean
    @DependsOn("overrideSecurityContextHolderStrategy")
    public SimpleApplicationEventMulticaster applicationEventMulticaster(
            @Qualifier("fineractEventExecutor") ThreadPoolTaskExecutor taskExecutor) {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(new DelegatingSecurityContextAsyncTaskExecutor(taskExecutor));
        return multicaster;
    }

    @Bean
    public MethodInvokingFactoryBean overrideSecurityContextHolderStrategy() {
        MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
        factoryBean.setTargetClass(SecurityContextHolder.class);
        factoryBean.setTargetMethod("setStrategyName");
        factoryBean.setArguments(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        return factoryBean;
    }

    @Bean
    @DependsOn("overrideSecurityContextHolderStrategy")
    public SecurityContextHolderStrategy securityContextHolderStrategy() {
        return SecurityContextHolder.getContextHolderStrategy();
    }
}
