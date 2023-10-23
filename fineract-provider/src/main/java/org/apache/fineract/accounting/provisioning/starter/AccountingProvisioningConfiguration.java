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
package org.apache.fineract.accounting.provisioning.starter;

import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.accounting.provisioning.domain.ProvisioningEntryRepository;
import org.apache.fineract.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformServiceImpl;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesWritePlatformService;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountingProvisioningConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProvisioningEntriesReadPlatformService.class)
    public ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService(JdbcTemplate jdbcTemplate,
            PaginationHelper loanProductProvisioningEntryDataPaginationHelper, PaginationHelper provisioningEntryDataPaginationHelper,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ProvisioningEntriesReadPlatformServiceImpl(jdbcTemplate, loanProductProvisioningEntryDataPaginationHelper,
                provisioningEntryDataPaginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(ProvisioningEntriesWritePlatformService.class)
    public ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService(
            ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService,
            ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService, LoanProductRepository loanProductRepository,
            GLAccountRepository glAccountRepository, OfficeRepositoryWrapper officeRepositoryWrapper,
            ProvisioningCategoryRepository provisioningCategoryRepository, PlatformSecurityContext platformSecurityContext,
            ProvisioningEntryRepository provisioningEntryRepository, JournalEntryWritePlatformService journalEntryWritePlatformService,
            ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer, FromJsonHelper fromApiJsonHelper) {
        return new ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl(provisioningEntriesReadPlatformService,
                provisioningCriteriaReadPlatformService, loanProductRepository, glAccountRepository, officeRepositoryWrapper,
                provisioningCategoryRepository, platformSecurityContext, provisioningEntryRepository, journalEntryWritePlatformService,
                fromApiJsonDeserializer, fromApiJsonHelper) {};
    }

}
