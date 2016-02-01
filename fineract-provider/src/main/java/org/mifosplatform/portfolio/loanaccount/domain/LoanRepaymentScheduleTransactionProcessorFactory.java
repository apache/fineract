/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.CreocoreLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.MifosStyleLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.RBILoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.EarlyPaymentLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.springframework.stereotype.Component;

@Component
public class LoanRepaymentScheduleTransactionProcessorFactory {

    public LoanRepaymentScheduleTransactionProcessor determineProcessor(
            final LoanTransactionProcessingStrategy transactionProcessingStrategy) {

        LoanRepaymentScheduleTransactionProcessor processor = new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();

        if (transactionProcessingStrategy != null) {

            if (transactionProcessingStrategy.isStandardMifosStrategy()) {
                processor = new MifosStyleLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isHeavensfamilyStrategy()) {
                processor = new HeavensFamilyLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isEarlyPaymentStrategy()) {
                processor = new EarlyPaymentLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isCreocoreStrategy()) {
                processor = new CreocoreLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isIndianRBIStrategy()) {
                processor = new RBILoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isPrincipalInterestPenaltiesFeesOrderStrategy()) {
                processor = new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();
            }

            if (transactionProcessingStrategy.isInterestPrincipalPenaltiesFeesOrderStrategy()) {
                processor = new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor();
            }
        }

        return processor;
    }
}