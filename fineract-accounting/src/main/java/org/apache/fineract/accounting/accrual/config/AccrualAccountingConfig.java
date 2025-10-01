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
package org.apache.fineract.accounting.accrual.config;

import org.apache.fineract.accounting.accrual.api.AccrualAccountingApiResource;
import org.apache.fineract.accounting.accrual.serialization.AccrualAccountingDataValidator;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Accrual Accounting related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to accrual accounting
 * functionality, including API resources and validators.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.accrual.api.AccrualAccountingApiResource
 * @see org.apache.fineract.accounting.accrual.serialization.AccrualAccountingDataValidator
 */
@Configuration
public class AccrualAccountingConfig {

    /**
     * Creates and configures the {@code AccrualAccountingApiResource} bean.
     *
     * @param commandsSourceWritePlatformService
     *            the service for handling command source write operations
     * @param apiJsonSerializerService
     *            the JSON serializer service for API responses
     * @return a fully configured {@code AccrualAccountingApiResource} instance
     * @see org.apache.fineract.accounting.accrual.api.AccrualAccountingApiResource
     */
    @Bean
    @ConditionalOnMissingBean
    public AccrualAccountingApiResource accrualAccountingApiResource(
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<String> apiJsonSerializerService) {
        return new AccrualAccountingApiResource(commandsSourceWritePlatformService, apiJsonSerializerService);
    }

    /**
     * Creates and configures the {@code AccrualAccountingDataValidator} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code AccrualAccountingDataValidator} instance
     * @see org.apache.fineract.accounting.accrual.serialization.AccrualAccountingDataValidator
     */
    @Bean
    @ConditionalOnMissingBean
    public AccrualAccountingDataValidator accrualAccountingDataValidator(FromJsonHelper fromApiJsonHelper) {
        return new AccrualAccountingDataValidator(fromApiJsonHelper);
    }
}
