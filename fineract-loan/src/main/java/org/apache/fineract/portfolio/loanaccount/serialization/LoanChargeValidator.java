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
package org.apache.fineract.portfolio.loanaccount.serialization;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeRefundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LoanChargeValidator {

    private final LoanTransactionRepository loanTransactionRepository;

    public void validateLoanIsNotClosed(final Loan loan, final LoanCharge loanCharge) {
        if (loan.isClosed()) {
            final String defaultUserMessage = "This charge cannot be added as the loan is already closed.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "loan.is.closed", defaultUserMessage, loan.getId(), loanCharge.name());

        }
    }

    // NOTE: to remove this constraint requires that loan transactions
    // that represent the waive of charges also be removed (or reversed)M
    // if you want ability to remove loan charges that are waived.
    public void validateLoanChargeIsNotWaived(final Loan loan, final LoanCharge loanCharge) {
        if (loanCharge.isWaived()) {
            final String defaultUserMessage = "This loan charge cannot be removed as the charge as already been waived.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "loanCharge.is.waived", defaultUserMessage, loan.getId(),
                    loanCharge.name());
        }
    }

    public void validateChargeHasValidSpecifiedDateIfApplicable(final Loan loan, final LoanCharge loanCharge,
            final LocalDate disbursementDate) {
        if (loanCharge.isSpecifiedDueDate() && DateUtils.isBefore(loanCharge.getDueLocalDate(), disbursementDate)) {
            final String defaultUserMessage = "This charge with specified due date cannot be added as the it is not in schedule range.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "specified.due.date.outside.range", defaultUserMessage,
                    loan.getDisbursementDate(), loanCharge.name());
        }
    }

    public void validateChargeAdditionForDisbursedLoan(final Loan loan, final LoanCharge loanCharge) {
        if (loan.isChargesAdditionAllowed() && loanCharge.isDueAtDisbursement()) {
            // Note: added this constraint to restrict adding disbursement
            // charges to a loan
            // after it is disbursed
            // if the loan charge payment type is 'Disbursement'.
            // To undo this constraint would mean resolving how charges due are
            // disbursement are handled at present.
            // When a loan is disbursed and has charges due at disbursement, a
            // transaction is created to auto record
            // payment of the charges (user has no choice in saying they were or
            // were not paid) - so its assumed they were paid.
            final String defaultUserMessage = "This charge which is due at disbursement cannot be added as the loan is already disbursed.";
            throw new LoanChargeCannotBeAddedException("loanCharge", "due.at.disbursement.and.loan.is.disbursed", defaultUserMessage,
                    loan.getId(), loanCharge.name());
        }
    }

    public void validateRepaymentTypeTransactionNotBeforeAChargeRefund(final Loan loan, final LoanTransaction repaymentTransaction,
            final String reversedOrCreated) {
        if (repaymentTransaction.isRepaymentLikeType() && !repaymentTransaction.isChargeRefund()) {
            final boolean existsChargeRefund = loanTransactionRepository.existsNonReversedByLoanAndTypeAndAfterDate(loan,
                    LoanTransactionType.CHARGE_REFUND, repaymentTransaction.getTransactionDate());
            if (existsChargeRefund) {
                final String errorMessage = "loan.transaction.cant.be." + reversedOrCreated + ".because.later.charge.refund.exists";
                final String details = "Loan Transaction: " + loan.getId() + " Can't be " + reversedOrCreated
                        + " because a Later Charge Refund Exists.";
                throw new LoanChargeRefundException(errorMessage, details);
            }
        }
    }

    public void validateChargePaymentNotInFuture(final LoanTransaction paymentTransaction) {
        if (DateUtils.isDateInTheFuture(paymentTransaction.getTransactionDate())) {
            final String errorMessage = "The date on which a loan charge paid cannot be in the future.";
            throw new InvalidLoanStateTransitionException("charge.payment", "cannot.be.a.future.date", errorMessage,
                    paymentTransaction.getTransactionDate());
        }
    }
}
