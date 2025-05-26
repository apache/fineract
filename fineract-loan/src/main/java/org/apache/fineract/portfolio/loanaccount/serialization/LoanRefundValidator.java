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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.springframework.stereotype.Component;

@Component
public final class LoanRefundValidator {

    public void validateTransferRefund(final Loan loan, final LoanTransaction loanTransaction) {
        if (loan.getStatus().isOverpaid()) {
            if (loan.getTotalOverpaid().compareTo(loanTransaction.getAmount(loan.getCurrency()).getAmount()) < 0) {
                final String errorMessage = "The refund amount must be less than or equal to overpaid amount ";
                throw new InvalidLoanStateTransitionException("transaction", "is.exceeding.overpaid.amount", errorMessage,
                        loan.getTotalOverpaid(), loanTransaction.getAmount(loan.getCurrency()).getAmount());
            } else if (!loan.isAfterLastRepayment(loanTransaction, loan.getLoanTransactions())) {
                final String errorMessage = "Transfer funds is allowed only after last repayment date";
                throw new InvalidLoanStateTransitionException("transaction", "is.not.after.repayment.date", errorMessage);
            }
        } else {
            final String errorMessage = "Transfer funds is allowed only for loan accounts with overpaid status ";
            throw new InvalidLoanStateTransitionException("transaction", "is.not.a.overpaid.loan", errorMessage);
        }
    }

    public void validateTransactionDateAfterDisbursement(final Loan loan, final LocalDate loanTransactionDate) {
        if (DateUtils.isBefore(loanTransactionDate, loan.getDisbursementDate())) {
            final String errorMessage = "The transaction date cannot be before the loan disbursement date: "
                    + loan.getDisbursementDate().toString();
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.before.disbursement.date", errorMessage,
                    loanTransactionDate, loan.getDisbursementDate());
        }
    }

    public void validateCreditBalanceRefund(final Loan loan, final LoanTransaction newCreditBalanceRefundTransaction) {
        if (!newCreditBalanceRefundTransaction.isGreaterThanZeroAndLessThanOrEqualTo(loan.getTotalOverpaid())) {
            final String errorMessage = "Transaction Amount ("
                    + newCreditBalanceRefundTransaction.getAmount(loan.getCurrency()).getAmount().toString()
                    + ") must be > zero and <= Overpaid amount (" + loan.getTotalOverpaid().toString() + ").";
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError(
                    "error.msg.transactionAmount.invalid.must.be.>zero.and<=overpaidamount", errorMessage, "transactionAmount",
                    newCreditBalanceRefundTransaction.getAmount(loan.getCurrency()));
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public void validateRefundEligibility(final Loan loan, final LoanTransaction loanTransaction) {
        if (loan.getStatus().isOverpaid() || loan.getStatus().isClosed()) {
            final String errorMessage = "This refund option is only for active loans ";
            throw new InvalidLoanStateTransitionException("transaction", "is.exceeding.overpaid.amount", errorMessage,
                    loan.getTotalOverpaid(), loanTransaction.getAmount(loan.getCurrency()).getAmount());
        } else if (loan.getTotalPaidInRepayments().isZero()) {
            final String errorMessage = "Cannot refund when no payment has been made";
            throw new InvalidLoanStateTransitionException("transaction", "no.payment.yet.made.for.loan", errorMessage);
        }
    }

    public void validateRefundTransactionType(final LoanTransaction loanTransaction) {
        if (loanTransaction.isNotRefundForActiveLoan()) {
            final String errorMessage = "A transaction of type refund was expected but not received.";
            throw new InvalidLoanTransactionTypeException("transaction", "is.not.a.refund.transaction", errorMessage);
        }
    }

    public void validateTransactionDateNotInFuture(final LocalDate transactionDate) {
        if (DateUtils.isDateInTheFuture(transactionDate)) {
            final String errorMessage = "The transaction date cannot be in the future.";
            throw new InvalidLoanStateTransitionException("transaction", "cannot.be.a.future.date", errorMessage, transactionDate);
        }
    }

    public void validateTransactionAmountThreshold(final Loan loan, final LoanTransaction adjustedTransaction) {
        if (loan.getLoanProduct().isMultiDisburseLoan() && adjustedTransaction == null) {
            final BigDecimal totalDisbursed = loan.getDisbursedAmount();
            final BigDecimal totalPrincipalAdjusted = loan.getSummary().getTotalPrincipalAdjustments();
            final BigDecimal totalPrincipalCredited = totalDisbursed.add(totalPrincipalAdjusted);
            if (totalPrincipalCredited.compareTo(loan.getSummary().getTotalPrincipalRepaid()) < 0) {
                final String errorMessage = "The transaction amount cannot exceed threshold.";
                throw new InvalidLoanStateTransitionException("transaction", "amount.exceeds.threshold", errorMessage);
            }
        }
    }
}
