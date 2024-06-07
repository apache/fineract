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
package org.apache.fineract.accounting.journalentry.starter;

import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorForLoanFactory;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorForSavingsFactory;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorForSharesFactory;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.accounting.journalentry.service.CashBasedAccountingProcessorForClientTransactions;
import org.apache.fineract.accounting.journalentry.service.JournalEntryReadPlatformService;
import org.apache.fineract.accounting.journalentry.service.JournalEntryReadPlatformServiceImpl;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.rule.domain.AccountingRuleRepository;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountingJournalEntryConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountingProcessorHelper.class)
    public AccountingProcessorHelper accountingProcessorHelper(JournalEntryRepository glJournalEntryRepository,
            ProductToGLAccountMappingRepository accountMappingRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository, GLClosureRepository closureRepository,
            GLAccountRepository glAccountRepository, OfficeRepository officeRepository, LoanTransactionRepository loanTransactionRepository,
            ClientTransactionRepository clientTransactionRepository,
            SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            AccountTransfersReadPlatformService accountTransfersReadPlatformService, ChargeRepositoryWrapper chargeRepositoryWrapper,
            BusinessEventNotifierService businessEventNotifierService) {
        return new AccountingProcessorHelper(glJournalEntryRepository, accountMappingRepository, financialActivityAccountRepository,
                closureRepository, glAccountRepository, officeRepository, loanTransactionRepository, clientTransactionRepository,
                savingsAccountTransactionRepository, accountTransfersReadPlatformService, chargeRepositoryWrapper,
                businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(JournalEntryReadPlatformService.class)
    public JournalEntryReadPlatformService journalEntryReadPlatformService(JdbcTemplate jdbcTemplate,
            GLAccountReadPlatformService glAccountReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            ColumnValidator columnValidator, FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new JournalEntryReadPlatformServiceImpl(jdbcTemplate, glAccountReadPlatformService, officeReadPlatformService,
                columnValidator, financialActivityAccountRepositoryWrapper, paginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(JournalEntryWritePlatformService.class)
    public JournalEntryWritePlatformService journalEntryWritePlatformService(GLClosureRepository glClosureRepository,
            GLAccountRepository glAccountRepository, JournalEntryRepository glJournalEntryRepository,
            OfficeRepositoryWrapper officeRepositoryWrapper, AccountingProcessorForLoanFactory accountingProcessorForLoanFactory,
            AccountingProcessorForSavingsFactory accountingProcessorForSavingsFactory,
            AccountingProcessorForSharesFactory accountingProcessorForSharesFactory, AccountingProcessorHelper helper,
            JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer, AccountingRuleRepository accountingRuleRepository,
            GLAccountReadPlatformService glAccountReadPlatformService, OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository,
            PlatformSecurityContext context, PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            CashBasedAccountingProcessorForClientTransactions accountingProcessorForClientTransactions) {
        return new JournalEntryWritePlatformServiceJpaRepositoryImpl(glClosureRepository, glAccountRepository, glJournalEntryRepository,
                officeRepositoryWrapper, accountingProcessorForLoanFactory, accountingProcessorForSavingsFactory,
                accountingProcessorForSharesFactory, helper, fromApiJsonDeserializer, accountingRuleRepository,
                glAccountReadPlatformService, organisationCurrencyRepository, context, paymentDetailWritePlatformService,
                financialActivityAccountRepositoryWrapper, accountingProcessorForClientTransactions);
    }
}
