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
package org.apache.fineract.accounting.glaccount.config;

import org.apache.fineract.accounting.glaccount.api.GLAccountsApiResource;
import org.apache.fineract.accounting.glaccount.serialization.GLAccountCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for GL Account related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to GL (General Ledger) accounts,
 * including API resources, validators, and deserializers.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.glaccount.api.GLAccountsApiResource
 * @see org.apache.fineract.accounting.glaccount.serialization.GLAccountCommandFromApiJsonDeserializer
 */
@Configuration
public class GLAccountConfig {

    /**
     * Creates and configures the GLAccountsApiResource bean.
     *
     * @param context
     *            the platform security context
     * @param glAccountReadPlatformService
     *            the GL account read platform service
     * @param apiJsonSerializerService
     *            the API JSON serializer service
     * @param apiRequestParameterHelper
     *            the API request parameter helper
     * @param commandsSourceWritePlatformService
     *            the command source write platform service
     * @param dropdownReadPlatformService
     *            the accounting dropdown read platform service
     * @param codeValueReadPlatformService
     *            the code value read platform service
     * @param bulkImportWorkbookService
     *            the bulk import workbook service
     * @param bulkImportWorkbookPopulatorService
     *            the bulk import workbook populator service
     * @return configured GLAccountsApiResource instance
     */
    @Bean
    @ConditionalOnMissingBean
    public GLAccountsApiResource glAccountsApiResource(final PlatformSecurityContext context,
            final GLAccountReadPlatformService glAccountReadPlatformService,
            final DefaultToApiJsonSerializer<org.apache.fineract.accounting.glaccount.data.GLAccountData> apiJsonSerializerService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService dropdownReadPlatformService,
            final org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService codeValueReadPlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {

        return new GLAccountsApiResource(context, glAccountReadPlatformService, apiJsonSerializerService, apiRequestParameterHelper,
                commandsSourceWritePlatformService, dropdownReadPlatformService, codeValueReadPlatformService, bulkImportWorkbookService,
                bulkImportWorkbookPopulatorService);
    }

    /**
     * Creates and configures the {@code GLAccountCommandFromApiJsonDeserializer} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code GLAccountCommandFromApiJsonDeserializer} instance
     * @see org.apache.fineract.accounting.glaccount.serialization.GLAccountCommandFromApiJsonDeserializer
     */
    @Bean
    @ConditionalOnMissingBean
    public GLAccountCommandFromApiJsonDeserializer glAccountCommandFromApiJsonDeserializer(
            final org.apache.fineract.infrastructure.core.serialization.FromJsonHelper fromApiJsonHelper) {
        return new GLAccountCommandFromApiJsonDeserializer(fromApiJsonHelper);
    }
}
