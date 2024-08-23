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
package org.apache.fineract.portfolio.account.starter;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDataValidator;
import org.apache.fineract.portfolio.account.domain.AccountTransferAssembler;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionAssembler;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformServiceImpl;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformServiceImpl;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformServiceImpl;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformServiceImpl;
import org.apache.fineract.portfolio.account.service.StandingInstructionHistoryReadPlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionHistoryReadPlatformServiceImpl;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformServiceImpl;
import org.apache.fineract.portfolio.account.service.StandingInstructionWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionWritePlatformServiceImpl;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountAssociationsReadPlatformService.class)
    public AccountAssociationsReadPlatformService accountAssociationsReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new AccountAssociationsReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AccountTransfersReadPlatformService.class)
    public AccountTransfersReadPlatformService accountTransfersReadPlatformService(JdbcTemplate jdbcTemplate,
            ClientReadPlatformService clientReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            PortfolioAccountReadPlatformService portfolioAccountReadPlatformService, ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        return new AccountTransfersReadPlatformServiceImpl(jdbcTemplate, clientReadPlatformService, officeReadPlatformService,
                portfolioAccountReadPlatformService, columnValidator, sqlGenerator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(AccountTransfersWritePlatformService.class)
    public AccountTransfersWritePlatformService accountTransfersWritePlatformService(
            AccountTransfersDataValidator accountTransfersDataValidator, AccountTransferAssembler accountTransferAssembler,
            AccountTransferRepository accountTransferRepository, SavingsAccountAssembler savingsAccountAssembler,
            SavingsAccountDomainService savingsAccountDomainService, LoanAssembler loanAccountAssembler,
            LoanAccountDomainService loanAccountDomainService, SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            AccountTransferDetailRepository accountTransferDetailRepository, LoanReadPlatformService loanReadPlatformService,
            GSIMRepositoy gsimRepository, ConfigurationDomainService configurationDomainService, ExternalIdFactory externalIdFactory,
            FineractProperties fineractProperties) {
        return new AccountTransfersWritePlatformServiceImpl(accountTransfersDataValidator, accountTransferAssembler,
                accountTransferRepository, savingsAccountAssembler, savingsAccountDomainService, loanAccountAssembler,
                loanAccountDomainService, savingsAccountWritePlatformService, accountTransferDetailRepository, loanReadPlatformService,
                gsimRepository, configurationDomainService, externalIdFactory, fineractProperties);
    }

    @Bean
    @ConditionalOnMissingBean(PortfolioAccountReadPlatformService.class)
    public PortfolioAccountReadPlatformService portfolioAccountReadPlatformService(JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new PortfolioAccountReadPlatformServiceImpl(jdbcTemplate, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(StandingInstructionHistoryReadPlatformService.class)
    public StandingInstructionHistoryReadPlatformService standingInstructionHistoryReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        return new StandingInstructionHistoryReadPlatformServiceImpl(jdbcTemplate, columnValidator, sqlGenerator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(StandingInstructionReadPlatformService.class)
    public StandingInstructionReadPlatformService standingInstructionReadPlatformService(JdbcTemplate jdbcTemplate,
            ClientReadPlatformService clientReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            PortfolioAccountReadPlatformService portfolioAccountReadPlatformService,
            DropdownReadPlatformService dropdownReadPlatformService, ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        return new StandingInstructionReadPlatformServiceImpl(jdbcTemplate, clientReadPlatformService, officeReadPlatformService,
                portfolioAccountReadPlatformService, dropdownReadPlatformService, columnValidator, sqlGenerator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(StandingInstructionWritePlatformService.class)
    public StandingInstructionWritePlatformService standingInstructionWritePlatformService(
            StandingInstructionDataValidator standingInstructionDataValidator, StandingInstructionAssembler standingInstructionAssembler,
            AccountTransferDetailRepository accountTransferDetailRepository, StandingInstructionRepository standingInstructionRepository) {
        return new StandingInstructionWritePlatformServiceImpl(standingInstructionDataValidator, standingInstructionAssembler,
                accountTransferDetailRepository, standingInstructionRepository);
    }
}
