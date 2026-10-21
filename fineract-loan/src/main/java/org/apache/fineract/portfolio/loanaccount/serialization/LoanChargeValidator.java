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
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.charge.exception.LoanChargeCannotBeAddedException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanChargeAdjustmentException;
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

    /**
     * Validates the amount of a charge adjustment against the charge. The transaction identified by
     * excludedTransactionId (if any) is ignored, so the amount of a charge adjustment being replaced counts as
     * available again.
     */
    public void validateChargeAdjustmentAmount(final LoanCharge loanCharge, final BigDecimal transactionAmount,
            final Long excludedTransactionId) {
        if (transactionAmount.compareTo(loanCharge.amount()) > 0) {
            throw new LoanChargeAdjustmentException("loan.charge.adjustment.invalid.amount",
                    "Transaction amount cannot be higher than the charge amount: " + loanCharge.amount());
        }

        final BigDecimal availableAmountForAdjustment = calculateAvailableAmountForChargeAdjustment(loanCharge, excludedTransactionId);
        if (transactionAmount.compareTo(availableAmountForAdjustment) > 0) {
            throw new LoanChargeAdjustmentException("loan.charge.adjustment.invalid.amount",
                    "Transaction amount cannot be higher than the available charge amount for adjustment: " + availableAmountForAdjustment);
        }
    }

    public void validateLoanStatusForChargeAdjustment(final Loan loan) {
        if (!(loan.isOpen() || loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid())) {
            throw new LoanChargeAdjustmentException("loan.charge.adjustment.invalid.status",
                    "Adjustment is not supported for the status of " + loan.getStatus().toString());
        }
    }

    /**
     * A charge adjustment cannot be dated before the charge exists on the loan, which is the earlier of its submitted
     * on date and its due date.
     */
    public void validateChargeAdjustmentDate(final LoanCharge loanCharge, final LocalDate transactionDate) {
        final LocalDate chargeEffectiveDate = Stream.of(loanCharge.getSubmittedOnDate(), loanCharge.getDueLocalDate())
                .filter(Objects::nonNull).min(LocalDate::compareTo).orElse(null);
        if (DateUtils.isBefore(transactionDate, chargeEffectiveDate)) {
            throw new LoanChargeAdjustmentException("loan.charge.adjustment.date.before.charge.date",
                    "Charge adjustment date cannot be before the charge date: " + chargeEffectiveDate, transactionDate,
                    chargeEffectiveDate);
        }
    }

    // package-private for testing purposes
    BigDecimal calculateAvailableAmountForChargeAdjustment(final LoanCharge loanCharge, final Long excludedTransactionId) {
        BigDecimal availableAmountForAdjustment = loanCharge.amount();
        for (LoanTransaction loanTransaction : loanCharge.getLoan().getLoanTransactions()) {
            if (loanTransaction.isNotReversed() && loanTransaction.getTypeOf().isChargeAdjustment()
                    && !Objects.equals(excludedTransactionId, loanTransaction.getId())
                    && loanCharge.equals(getAdjustedCharge(loanTransaction))) {
                availableAmountForAdjustment = availableAmountForAdjustment.subtract(loanTransaction.getAmount());
            }
        }
        return availableAmountForAdjustment;
    }

    /**
     * Returns the loan charge a charge adjustment transaction belongs to. A charge adjustment without a charge link is
     * corrupt data: it cannot be counted against any charge, so the request is rejected rather than over-adjusting.
     */
    public LoanCharge getAdjustedCharge(final LoanTransaction chargeAdjustmentTransaction) {
        return chargeAdjustmentTransaction.getLoanTransactionRelations().stream()
                .filter(relation -> LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT.equals(relation.getRelationType())
                        && relation.getToCharge() != null)
                .map(LoanTransactionRelation::getToCharge).findFirst()
                .orElseThrow(() -> new LoanChargeAdjustmentException("loan.charge.adjustment.charge.not.found",
                        "Charge adjustment transaction: " + chargeAdjustmentTransaction.getId() + " is not linked to a loan charge",
                        chargeAdjustmentTransaction.getId()));
    }
}
