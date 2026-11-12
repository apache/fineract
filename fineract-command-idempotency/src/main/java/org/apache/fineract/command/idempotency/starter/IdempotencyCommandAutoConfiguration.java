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
package org.apache.fineract.command.idempotency.starter;

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_HOOK_BASE_PACKAGE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_ENABLED;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_WEB_BASE_PACKAGE;

import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.core.RecoverableCommandErrorClassifier;
import org.apache.fineract.command.idempotency.IdempotencyCommandProperties;
import org.apache.fineract.command.idempotency.distinctor.CustomIdempotencyDistinctor;
import org.apache.fineract.command.idempotency.web.IdempotencyResponseCaptureFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;

@AutoConfiguration
@EnableConfigurationProperties({ IdempotencyCommandProperties.class })
@ComponentScan({ COMMAND_IDEMPOTENCY_HOOK_BASE_PACKAGE, COMMAND_IDEMPOTENCY_WEB_BASE_PACKAGE })
@ConditionalOnProperty(value = COMMAND_IDEMPOTENCY_PROPERTY_ENABLED, havingValue = "true", matchIfMissing = true)
public class IdempotencyCommandAutoConfiguration {

    /**
     * Fills in what a command could not record about its own answer: the status it was sent with, always, and the body
     * itself only when the command stored no result of its own - a failure, or an answer that is not JSON. Ordered
     * early so that it sees the response as the client will, and it steps aside entirely for requests that carry no
     * key.
     */
    @Bean
    @ConditionalOnProperty(value = COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE, havingValue = "true", matchIfMissing = true)
    FilterRegistrationBean<IdempotencyResponseCaptureFilter> idempotencyResponseCaptureFilter(CommandStore store,
            IdempotencyCommandProperties properties) {
        var registration = new FilterRegistrationBean<>(new IdempotencyResponseCaptureFilter(store, properties));

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);

        return registration;
    }

    /**
     * What an idempotency key is unique together with, as {@code fineract.command.idempotency.custom-distinctor}
     * selects. Backs off for a distinctor the application defines itself.
     */
    @Bean
    @ConditionalOnMissingBean
    CustomIdempotencyDistinctor customIdempotencyDistinctor(IdempotencyCommandProperties properties) {
        return properties.getCustomDistinctor().create();
    }

    /**
     * Treats no failure as recoverable, so every failure keeps its key, until the application says otherwise by
     * defining its own classifier - Fineract does, with the rules of the legacy command processing.
     */
    @Bean
    @ConditionalOnMissingBean
    RecoverableCommandErrorClassifier recoverableCommandErrorClassifier() {
        return error -> false;
    }
}
