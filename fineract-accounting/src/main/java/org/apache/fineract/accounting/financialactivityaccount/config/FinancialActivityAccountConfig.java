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
package org.apache.fineract.accounting.financialactivityaccount.config;

import org.apache.fineract.accounting.financialactivityaccount.api.FinancialActivityAccountsApiResource;
import org.apache.fineract.accounting.financialactivityaccount.serialization.FinancialActivityAccountDataValidator;
import org.apache.fineract.accounting.financialactivityaccount.service.FinancialActivityAccountReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Financial Activity Account related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to financial activity accounts,
 * including API resources and validators.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.financialactivityaccount.api.FinancialActivityAccountsApiResource
 * @see org.apache.fineract.accounting.financialactivityaccount.serialization.FinancialActivityAccountDataValidator
 */
@Configuration
public class FinancialActivityAccountConfig {

    /**
     * Creates and configures the {@code FinancialActivityAccountsApiResource} bean.
     *
     * @param context
     *            the platform security context for authorization
     * @param financialActivityAccountReadPlatformService
     *            the service for reading financial activity account data
     * @param apiJsonSerializerService
     *            the JSON serializer service for API responses
     * @param apiRequestParameterHelper
     *            the helper for API request parameters
     * @param commandsSourceWritePlatformService
     *            the service for handling command source write operations
     * @return a fully configured {@code FinancialActivityAccountsApiResource} instance
     * @see org.apache.fineract.accounting.financialactivityaccount.api.FinancialActivityAccountsApiResource
     */
    @Bean
    @ConditionalOnMissingBean
    public FinancialActivityAccountsApiResource financialActivityAccountsApiResource(final PlatformSecurityContext context,
            final FinancialActivityAccountReadPlatformService financialActivityAccountReadPlatformService,
            final DefaultToApiJsonSerializer<org.apache.fineract.accounting.financialactivityaccount.data.FinancialActivityAccountData> apiJsonSerializerService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {

        return new FinancialActivityAccountsApiResource(context, financialActivityAccountReadPlatformService, apiJsonSerializerService,
                apiRequestParameterHelper, commandsSourceWritePlatformService);
    }

    /**
     * Creates and configures the {@code FinancialActivityAccountDataValidator} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code FinancialActivityAccountDataValidator} instance
     * @see org.apache.fineract.accounting.financialactivityaccount.serialization.FinancialActivityAccountDataValidator
     */
    @Bean
    @ConditionalOnMissingBean
    public FinancialActivityAccountDataValidator financialActivityAccountDataValidator(
            final org.apache.fineract.infrastructure.core.serialization.FromJsonHelper fromApiJsonHelper) {
        return new FinancialActivityAccountDataValidator(fromApiJsonHelper);
    }
}
