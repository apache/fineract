/*
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
package org.apache.fineract.infrastructure.monitoring;

import io.sentry.Sentry;
import io.sentry.spring.jakarta.SentryExceptionResolver;
import io.sentry.spring.jakarta.SentryTaskDecorator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Configuration for Sentry error logging
 */
@Configuration
@ConditionalOnProperty(value = "sentry.dsn")
public class SentryConfiguration {

    @Value("${sentry.dsn}")
    private String sentryDsn;

    @Value("${sentry.environment:production}")
    private String environment;

    @Value("${sentry.traces-sample-rate:0.1}")
    private double tracesSampleRate;
    
    private final Environment springEnv;
    
    public SentryConfiguration(Environment springEnv) {
        this.springEnv = springEnv;
    }

    @PostConstruct
    public void initSentry() {
        Sentry.init(options -> {            options.setDsn(sentryDsn);
            options.setEnvironment(environment);
            options.setTracesSampleRate(tracesSampleRate);
            options.setDebug(false);
            
            // Add useful tags for Railway deployment
            options.setTag("deployment", "railway");
            options.setTag("java.version", System.getProperty("java.version"));
            options.setTag("postgres.host", springEnv.getProperty("PGHOST", "unknown"));
            options.setTag("redis.host", springEnv.getProperty("REDISHOST", "unknown"));
            
            // Add performance monitoring
            options.setEnableUncaughtExceptionHandler(true);
            options.setAttachStacktrace(true);
            options.setEnableDeduplication(true);
            
            // Configure beforeSend to filter sensitive data
            options.setBeforeSend((event, hint) -> {
                // Filter out sensitive information if needed
                if (event.getServerName() != null) {
                    event.setServerName("redacted");
                }
                return event;
            });
        });
    }

    @Bean
    @Order(0)
    public HandlerExceptionResolver sentryExceptionResolver() {
        return new SentryExceptionResolver();
    }

    @Bean
    public SentryTaskDecorator sentryTaskDecorator() {
        return new SentryTaskDecorator();
    }
}
