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

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
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
    private static final int DEFAULT_QUEUE_CAPACITY = 100;

    @Bean(name = "fineractEventExecutor")
    public ThreadPoolTaskExecutor fineractEventExecutor(ThreadPoolTaskExecutorBuilder builder,
            @Value("${spring.task.execution.pool.core-size:-1}") int configuredCore,
            @Value("${spring.task.execution.pool.max-size:-1}") int configuredMax,
            @Value("${spring.task.execution.pool.queue-capacity:-1}") int configuredQueueCapacity) {

        int cpus = Runtime.getRuntime().availableProcessors();
        int smartCore = cpus * 2;
        int smartMax = cpus * 5;

        int coreSize = configuredCore > 0 ? configuredCore : smartCore;
        int rawMaxSize = configuredMax > 0 ? configuredMax : smartMax;
        int queueCapacity = configuredQueueCapacity >= 0 ? configuredQueueCapacity : DEFAULT_QUEUE_CAPACITY;

        int finalMaxSize = Math.max(rawMaxSize, coreSize);

        ThreadPoolTaskExecutor executor = builder.threadNamePrefix("FineractEvent-").corePoolSize(coreSize).maxPoolSize(finalMaxSize)
                .queueCapacity(queueCapacity).build();

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
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
