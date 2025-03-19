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
            ExternalIdFactory externalIdFactory) {
        return new CreocoreLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(EarlyRepaymentLoanRepaymentScheduleTransactionProcessorCondition.class)
    public EarlyPaymentLoanRepaymentScheduleTransactionProcessor earlyPaymentLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new EarlyPaymentLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(MifosStandardLoanRepaymentScheduleTransactionProcessorCondition.class)
    public FineractStyleLoanRepaymentScheduleTransactionProcessor fineractStyleLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new FineractStyleLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(HeavensFamilyLoanRepaymentScheduleTransactionProcessorCondition.class)
    public HeavensFamilyLoanRepaymentScheduleTransactionProcessor heavensFamilyLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new HeavensFamilyLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(InterestPrincipalPenaltiesFeesLoanRepaymentScheduleTransactionProcessorCondition.class)
    public InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor interestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(PrincipalInterestPenaltiesFeesLoanRepaymentScheduleTransactionProcessorCondition.class)
    public PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor principalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(RBIIndiaLoanRepaymentScheduleTransactionProcessorCondition.class)
    public RBILoanRepaymentScheduleTransactionProcessor rbiLoanRepaymentScheduleTransactionProcessor(ExternalIdFactory externalIdFactory) {
        return new RBILoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessorCondition.class)
    public DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor duePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
    }

    @Bean
    @Conditional(DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessorCondition.class)
    public DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor duePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor(
            ExternalIdFactory externalIdFactory) {
        return new DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor(externalIdFactory);
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
    public AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor(EMICalculator emiCalculator,
            LoanRepositoryWrapper loanRepositoryWrapper,
            @Lazy ProgressiveLoanInterestRefundServiceImpl progressiveLoanInterestRefundService, ExternalIdFactory externalIdFactory,
            LoanScheduleComponent loanSchedule) {
        return new AdvancedPaymentScheduleTransactionProcessor(emiCalculator, loanRepositoryWrapper, progressiveLoanInterestRefundService,
                externalIdFactory, loanSchedule);
    }
}
