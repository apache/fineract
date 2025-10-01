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
package org.apache.fineract.accounting.rule.config;

import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.rule.api.AccountingRuleApiResource;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.rule.service.AccountingRuleReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Accounting Rule related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to accounting rules, including API
 * resources and JSON deserializers.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.rule.api.AccountingRuleApiResource
 * @see org.apache.fineract.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer
 */
@Configuration
public class AccountingRuleConfig {

    /**
     * Creates and configures the AccountingRuleApiResource bean.
     *
     * @param accountingRuleReadPlatformService
     *            the accounting rule read platform service
     * @param accountReadPlatformService
     *            the account read platform service
     * @param officeReadPlatformService
     *            the office read platform service
     * @param apiJsonSerializerService
     *            the API JSON serializer service
     * @param apiRequestParameterHelper
     *            the API request parameter helper
     * @param context
     *            the platform security context
     * @param commandsSourceWritePlatformService
     *            the commands source write platform service
     * @param codeValueReadPlatformService
     *            the code value read platform service
     * @return configured AccountingRuleApiResource instance
     */
    @Bean
    @ConditionalOnMissingBean
    public AccountingRuleApiResource accountingRuleApiResource(final AccountingRuleReadPlatformService accountingRuleReadPlatformService,
            final GLAccountReadPlatformService accountReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final DefaultToApiJsonSerializer<AccountingRuleData> apiJsonSerializerService,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PlatformSecurityContext context,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {

        return new AccountingRuleApiResource(accountingRuleReadPlatformService, accountReadPlatformService, officeReadPlatformService,
                apiJsonSerializerService, apiRequestParameterHelper, context, commandsSourceWritePlatformService,
                codeValueReadPlatformService);
    }

    /**
     * Creates and configures the {@code AccountingRuleCommandFromApiJsonDeserializer} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code AccountingRuleCommandFromApiJsonDeserializer} instance
     * @see org.apache.fineract.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer
     */
    @Bean
    @ConditionalOnMissingBean
    public AccountingRuleCommandFromApiJsonDeserializer accountingRuleCommandFromApiJsonDeserializer(
            final FromJsonHelper fromApiJsonHelper) {
        return new AccountingRuleCommandFromApiJsonDeserializer(fromApiJsonHelper);
    }
}
