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
package org.apache.fineract.portfolio.loanaccount.starter;

import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.CapitalizedIncomeBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.CapitalizedIncomeBalanceServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.CapitalizedIncomePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.CapitalizedIncomeWritePlatformServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.LoanJournalEntryPoster;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanTransactionValidatorImpl;
import org.apache.fineract.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import org.apache.fineract.portfolio.note.service.NoteWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgressiveLoanAccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(CapitalizedIncomePlatformService.class)
    public CapitalizedIncomePlatformService capitalizedIncomePlatformService(ProgressiveLoanTransactionValidator loanTransactionValidator,
            LoanAssembler loanAssembler, LoanTransactionRepository loanTransactionRepository,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, LoanJournalEntryPoster journalEntryPoster,
            NoteWritePlatformService noteWritePlatformService, ExternalIdFactory externalIdFactory,
            LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService, LoanBalanceService loanBalanceService,
            LoanLifecycleStateMachine loanLifecycleStateMachine, BusinessEventNotifierService businessEventNotifierService) {
        return new CapitalizedIncomeWritePlatformServiceImpl(loanTransactionValidator, loanAssembler, loanTransactionRepository,
                paymentDetailWritePlatformService, journalEntryPoster, noteWritePlatformService, externalIdFactory,
                capitalizedIncomeBalanceRepository, reprocessLoanTransactionsService, loanBalanceService, loanLifecycleStateMachine,
                businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(ProgressiveLoanTransactionValidator.class)
    public ProgressiveLoanTransactionValidator progressiveLoanTransactionValidator(FromJsonHelper fromApiJsonHelper,
            LoanTransactionValidator loanTransactionValidator, LoanRepositoryWrapper loanRepositoryWrapper,
            LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository,
            LoanBuyDownFeeBalanceRepository loanBuydownFeeBalanceRepository, LoanTransactionRepository loanTransactionRepository) {
        return new ProgressiveLoanTransactionValidatorImpl(fromApiJsonHelper, loanTransactionValidator, loanRepositoryWrapper,
                loanCapitalizedIncomeBalanceRepository, loanBuydownFeeBalanceRepository, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(CapitalizedIncomeBalanceService.class)
    public CapitalizedIncomeBalanceService capitalizedIncomeBalanceService(
            LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository) {
        return new CapitalizedIncomeBalanceServiceImpl(loanCapitalizedIncomeBalanceRepository);
    }
}
