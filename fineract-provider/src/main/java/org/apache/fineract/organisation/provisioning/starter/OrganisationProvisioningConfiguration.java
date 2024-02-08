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
package org.apache.fineract.organisation.provisioning.starter;

import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesReadPlatformService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCategoryRepository;
import org.apache.fineract.organisation.provisioning.domain.ProvisioningCriteriaRepository;
import org.apache.fineract.organisation.provisioning.serialization.ProvisioningCategoryDefinitionJsonDeserializer;
import org.apache.fineract.organisation.provisioning.serialization.ProvisioningCriteriaDefinitionJsonDeserializer;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCategoryReadPlatformService;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCategoryReadPlatformServiceImpl;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCategoryWritePlatformService;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCategoryWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaAssembler;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformServiceImpl;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaWritePlatformService;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationProvisioningConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProvisioningCategoryReadPlatformService.class)
    public ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new ProvisioningCategoryReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ProvisioningCategoryWritePlatformService.class)
    public ProvisioningCategoryWritePlatformService provisioningCategoryWritePlatformService(
            ProvisioningCategoryRepository provisioningCategoryRepository,
            ProvisioningCategoryDefinitionJsonDeserializer fromApiJsonDeserializer, JdbcTemplate jdbcTemplate) {
        return new ProvisioningCategoryWritePlatformServiceJpaRepositoryImpl(provisioningCategoryRepository, fromApiJsonDeserializer,
                jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ProvisioningCriteriaAssembler.class)
    public ProvisioningCriteriaAssembler provisioningCriteriaAssembler(FromJsonHelper fromApiJsonHelper,
            ProvisioningCategoryRepository provisioningCategoryRepository, LoanProductRepository loanProductRepository,
            GLAccountRepository glAccountRepository, PlatformSecurityContext platformSecurityContext) {
        return new ProvisioningCriteriaAssembler(fromApiJsonHelper, provisioningCategoryRepository, loanProductRepository,
                glAccountRepository, platformSecurityContext);
    }

    @Bean
    @ConditionalOnMissingBean(ProvisioningCriteriaReadPlatformService.class)
    public ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService(JdbcTemplate jdbcTemplate,
            ProvisioningCategoryReadPlatformService provisioningCategoryReadPlatformService,
            LoanProductReadPlatformService loanProductReadPlatformService, GLAccountReadPlatformService glAccountReadPlatformService,
            LoanProductReadPlatformService loanProductReaPlatformService) {
        return new ProvisioningCriteriaReadPlatformServiceImpl(jdbcTemplate, provisioningCategoryReadPlatformService,
                loanProductReadPlatformService, glAccountReadPlatformService, loanProductReaPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(ProvisioningCriteriaWritePlatformService.class)
    public ProvisioningCriteriaWritePlatformService provisioningCriteriaWritePlatformService(
            ProvisioningCriteriaDefinitionJsonDeserializer fromApiJsonDeserializer,
            ProvisioningCriteriaAssembler provisioningCriteriaAssembler, ProvisioningCriteriaRepository provisioningCriteriaRepository,
            FromJsonHelper fromApiJsonHelper, GLAccountRepository glAccountRepository,
            ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService) {
        return new ProvisioningCriteriaWritePlatformServiceJpaRepositoryImpl(fromApiJsonDeserializer, provisioningCriteriaAssembler,
                provisioningCriteriaRepository, fromApiJsonHelper, glAccountRepository, provisioningEntriesReadPlatformService);
    }

}
