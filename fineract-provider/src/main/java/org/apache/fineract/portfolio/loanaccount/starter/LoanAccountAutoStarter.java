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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.CreocoreLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.EarlyPaymentLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.FineractStyleLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.RBILoanRepaymentScheduleTransactionProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoanAccountAutoStarter {

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.creocore.enabled")
    public CreocoreLoanRepaymentScheduleTransactionProcessor creocoreLoanRepaymentScheduleTransactionProcessor() {
        return new CreocoreLoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.early-repayment.enabled")
    public EarlyPaymentLoanRepaymentScheduleTransactionProcessor earlyPaymentLoanRepaymentScheduleTransactionProcessor() {
        return new EarlyPaymentLoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.mifos-standard.enabled")
    public FineractStyleLoanRepaymentScheduleTransactionProcessor fineractStyleLoanRepaymentScheduleTransactionProcessor() {
        return new FineractStyleLoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.heavensfamily.enabled")
    public HeavensFamilyLoanRepaymentScheduleTransactionProcessor heavensFamilyLoanRepaymentScheduleTransactionProcessor() {
        return new HeavensFamilyLoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.interest-principal-penalties-fees.enabled")
    public InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor interestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor() {
        return new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.principal-interest-penalties-fees.enabled")
    public PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor principalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor() {
        return new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnProperty("fineract.loan.transactionprocessor.rbi-india.enabled")
    public RBILoanRepaymentScheduleTransactionProcessor rbiLoanRepaymentScheduleTransactionProcessor() {
        return new RBILoanRepaymentScheduleTransactionProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(LoanRepaymentScheduleTransactionProcessorFactory.class)
    public LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory(
            PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor defaultLoanRepaymentScheduleTransactionProcessor,
            List<LoanRepaymentScheduleTransactionProcessor> processors) {
        return new LoanRepaymentScheduleTransactionProcessorFactory(defaultLoanRepaymentScheduleTransactionProcessor, processors);
    }
}
