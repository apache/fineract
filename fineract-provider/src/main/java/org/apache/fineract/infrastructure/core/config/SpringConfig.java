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

import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

@Configuration
public class SpringConfig {

    @Bean
    public SimpleApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster saem = new SimpleApplicationEventMulticaster();
        saem.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return saem;
    }

    // The application events (for importing) rely on the inheritable thread local security context strategy
    // This is NOT compatible with threadpools so if we use threadpools the below will need to be reworked
    @Bean
    public MethodInvokingFactoryBean overrideSecurityContextHolderStrategy() {
        MethodInvokingFactoryBean mifb = new MethodInvokingFactoryBean();
        mifb.setTargetClass(SecurityContextHolder.class);
        mifb.setTargetMethod("setStrategyName");
        mifb.setArguments("MODE_INHERITABLETHREADLOCAL");
        return mifb;
    }

    @Bean
    @DependsOn("overrideSecurityContextHolderStrategy")
    public SecurityContextHolderStrategy securityContextHolderStrategy() {
        return SecurityContextHolder.getContextHolderStrategy();
    }
}
