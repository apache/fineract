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

import java.util.List;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.CreocoreLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.EarlyPaymentLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.FineractStyleLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.RBILoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeService;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanInterestRefundServiceImpl;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class LoanAccountAutoStarter {

    @Bean
    @Conditional(CreocoreLoanRepaymentScheduleTransactionProcessorCondition.class)
    public CreocoreLoanRepaymentScheduleTransactionProcessor creocoreLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new CreocoreLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(EarlyRepaymentLoanRepaymentScheduleTransactionProcessorCondition.class)
    public EarlyPaymentLoanRepaymentScheduleTransactionProcessor earlyPaymentLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new EarlyPaymentLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(MifosStandardLoanRepaymentScheduleTransactionProcessorCondition.class)
    public FineractStyleLoanRepaymentScheduleTransactionProcessor fineractStyleLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new FineractStyleLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(HeavensFamilyLoanRepaymentScheduleTransactionProcessorCondition.class)
    public HeavensFamilyLoanRepaymentScheduleTransactionProcessor heavensFamilyLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new HeavensFamilyLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(InterestPrincipalPenaltiesFeesLoanRepaymentScheduleTransactionProcessorCondition.class)
    public InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor interestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @Conditional(PrincipalInterestPenaltiesFeesLoanRepaymentScheduleTransactionProcessorCondition.class)
    public PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor principalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @Conditional(RBIIndiaLoanRepaymentScheduleTransactionProcessorCondition.class)
    public RBILoanRepaymentScheduleTransactionProcessor rbiLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new RBILoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessorCondition.class)
    public DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor duePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @Conditional(DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessorCondition.class)
    public DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor duePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanRepaymentScheduleTransactionProcessorFactory.class)
    public LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory(
            PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor defaultLoanRepaymentScheduleTransactionProcessor,
            List<LoanRepaymentScheduleTransactionProcessor> processors) {
        return new LoanRepaymentScheduleTransactionProcessorFactory(defaultLoanRepaymentScheduleTransactionProcessor, processors);
    }

    @Bean
    @Conditional(AdvancedPaymentScheduleTransactionProcessorCondition.class)
    public AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor(final EMICalculator emiCalculator,
            final LoanRepositoryWrapper loanRepositoryWrapper,
            final @Lazy ProgressiveLoanInterestRefundServiceImpl progressiveLoanInterestRefundService,
            final ExternalIdFactory externalIdFactory, final LoanScheduleComponent loanSchedule,
            final LoanTransactionRepository loanTransactionRepository, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService, @Lazy final LoanChargeService loanChargeService) {
        return new AdvancedPaymentScheduleTransactionProcessor(emiCalculator, loanRepositoryWrapper, progressiveLoanInterestRefundService,
                externalIdFactory, loanSchedule, loanTransactionRepository, loanChargeValidator, loanBalanceService, loanChargeService);
    }
}
