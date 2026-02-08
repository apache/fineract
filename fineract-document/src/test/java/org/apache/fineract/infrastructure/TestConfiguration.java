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
package org.apache.fineract.infrastructure;

import static org.apache.fineract.infrastructure.contentstore.processor.ContentProcessor.BEAN_NAME_EXECUTOR;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties({ FineractProperties.class })
@PropertySource("classpath:application-test.properties")
@ComponentScan({ "org.apache.fineract.infrastructure.contentstore.util", "org.apache.fineract.infrastructure.contentstore.detector",
        "org.apache.fineract.infrastructure.contentstore.processor", "org.apache.fineract.infrastructure.contentstore.policy",
        "org.apache.fineract.infrastructure.contentstore.service" })
public class TestConfiguration {

    @Bean(BEAN_NAME_EXECUTOR)
    public ExecutorService contentProcessorExecutor() {
        return Executors.newCachedThreadPool();
    }
}
