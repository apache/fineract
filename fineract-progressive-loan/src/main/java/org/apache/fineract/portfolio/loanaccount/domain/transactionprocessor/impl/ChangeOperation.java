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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.DownPaymentFees;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.jspecify.annotations.NonNull;

@Getter
public class ChangeOperation implements Comparable<ChangeOperation> {

    private final Optional<LoanTermVariationsData> loanTermVariationsData;
    private final Optional<LoanCharge> loanCharge;
    private final Optional<LoanTransaction> loanTransaction;

    public ChangeOperation(LoanCharge loanCharge) {
        this.loanTermVariationsData = Optional.empty();
        this.loanCharge = Optional.of(loanCharge);
        this.loanTransaction = Optional.empty();
    }

    public ChangeOperation(LoanTransaction loanTransaction) {
        this.loanTermVariationsData = Optional.empty();
        this.loanTransaction = Optional.of(loanTransaction);
        this.loanCharge = Optional.empty();
    }

    public ChangeOperation(LoanTermVariationsData loanTermVariationsData) {
        this.loanTermVariationsData = Optional.of(loanTermVariationsData);
        this.loanTransaction = Optional.empty();
        this.loanCharge = Optional.empty();
    }

    public boolean isLoanTermVariationsData() {
        return loanTermVariationsData.isPresent();
    }

    public boolean isTransaction() {
        return loanTransaction.isPresent();
    }

    public boolean isCharge() {
        return loanCharge.isPresent();
    }

    public boolean isInstallmentFee() {
        return loanCharge.map(LoanCharge::isInstalmentFee).orElse(false);
    }

    private boolean isAccrualActivity() {
        return isTransaction() && loanTransaction.get().isAccrualActivity();
    }

    private boolean isDownPaymentFee() {
        return loanCharge.map(LoanCharge::isDueAtDownPayment).orElse(false);
    }

    private boolean isDownPaymentTransaction() {
        return loanTransaction.map(LoanTransaction::isDownPayment).orElse(false);
    }

    /**
     * The down payment transaction that pays this down payment fee: the one of the fee's down payment period (see
     * {@link DownPaymentFees}). The fee is ordered right before it, whenever the fee was added.
     */
    private Optional<LoanTransaction> downPaymentAnchor() {
        return loanCharge.filter(LoanCharge::isDueAtDownPayment).flatMap(ChangeOperation::findDownPaymentAnchor);
    }

    private static Optional<LoanTransaction> findDownPaymentAnchor(final LoanCharge fee) {
        final Loan loan = fee.getLoan();
        final LocalDate dueDate = fee.getDueLocalDate();
        if (loan == null || dueDate == null) {
            return Optional.empty();
        }
        final List<LoanTransaction> downPayments = loan.getLoanTransactions().stream()
                .filter(t -> t.isNotReversed() && t.isDownPayment() && DateUtils.isEqual(dueDate, t.getTransactionDate()))
                .sorted(Comparator.comparing(LoanTransaction::getId, Comparator.nullsLast(Long::compareTo))).toList();
        if (downPayments.isEmpty()) {
            return Optional.empty();
        }
        final int periodIndex = DownPaymentFees.periodIndexOf(fee,
                DownPaymentFees.downPaymentPeriodsOn(loan.getRepaymentScheduleInstallments(), dueDate).size());
        return Optional.of(downPayments.get(Math.min(periodIndex, downPayments.size() - 1)));
    }

    private boolean isSameTransactionAs(final ChangeOperation other) {
        return loanTransaction.isPresent() && loanTransaction.equals(other.loanTransaction);
    }

    /**
     * Compares by the anchor transactions (a down payment fee stands for its down payment transaction, anything else
     * for itself); a fee goes right before its own down payment, fees of the same down payment by id.
     */
    private int compareByDownPaymentAnchor(final ChangeOperation o, final ChangeOperation thisKey, final ChangeOperation otherKey) {
        if (!thisKey.isSameTransactionAs(otherKey)) {
            return thisKey.compareTo(otherKey);
        }
        final boolean thisIsAnchoredFee = thisKey != this;
        final boolean otherIsAnchoredFee = otherKey != o;
        if (thisIsAnchoredFee != otherIsAnchoredFee) {
            return thisIsAnchoredFee ? -1 : 1;
        }
        return Comparator.nullsLast(Long::compareTo).compare(loanCharge.map(LoanCharge::getId).orElse(null),
                o.loanCharge.map(LoanCharge::getId).orElse(null));
    }

    private boolean isBackdatedCharge() {
        return isCharge() && loanCharge.isPresent() && loanCharge.get().getDueDate() != null
                && DateUtils.isBefore(loanCharge.get().getDueDate(), loanCharge.get().getSubmittedOnDate());
    }

    private LocalDate getEffectiveDate() {
        if (loanTermVariationsData.isPresent()) {
            return getSubmittedOnDate();
        } else if (loanCharge.isPresent()) {
            if (loanCharge.get().isDueAtDownPayment() && loanCharge.get().getDueDate() != null) {
                return loanCharge.get().getDueDate();
            }
            if (isBackdatedCharge()) {
                return loanCharge.get().getDueDate();
            } else if (isInstallmentFee()) {
                return loanCharge.get().getLoan().isDisbursed() ? loanCharge.get().getSubmittedOnDate() : loanCharge.get().getDueDate();
            } else {
                return loanCharge.get().getSubmittedOnDate();
            }
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getTransactionDate();
        } else {
            throw new RuntimeException("Either charge or transaction should be present");
        }
    }

    private LocalDate getSubmittedOnDate() {
        if (loanTermVariationsData.isPresent()) {
            return loanTermVariationsData.get().getTermVariationApplicableFrom();
        } else if (loanCharge.isPresent()) {
            return loanCharge.get().getSubmittedOnDate();
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getSubmittedOnDate();
        } else {
            throw new RuntimeException("Either charge or transaction should be present");
        }
    }

    private OffsetDateTime getCreatedDateTime() {
        if (loanTermVariationsData.isPresent()) {
            return DateUtils.getOffsetDateTimeOfTenantFromLocalDate(getSubmittedOnDate());
        } else if (loanCharge.isPresent() && loanCharge.get().getCreatedDate().isPresent()) {
            return loanCharge.get().getCreatedDate().get();
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getCreatedDate().orElse(null);
        } else {
            throw new RuntimeException("Either charge with createdDate or transaction created datetime should be present");
        }
    }

    public boolean isDueDatePercentageCharge() {
        return this.isCharge() && this.getLoanCharge().isPresent() && this.getLoanCharge().get().isDueDateCharge()
                && this.getLoanCharge().get().getPercentage() != null;
    }

    @Override
    @SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "TODO: fix this! See: https://stackoverflow.com/questions/2609037/findbugs-how-to-solve-eq-compareto-use-object-equals")
    public int compareTo(@NonNull ChangeOperation o) {
        int datePortion = DateUtils.compareWithNullsLast(this.getEffectiveDate(), o.getEffectiveDate());
        if (datePortion == 0) {
            boolean thisIsDisb = this.isTransaction() && this.getLoanTransaction().isPresent()
                    && this.getLoanTransaction().get().isDisbursement();
            boolean otherIsDisb = o.isTransaction() && o.getLoanTransaction().isPresent() && o.getLoanTransaction().get().isDisbursement();

            if (thisIsDisb && o.isCharge() && o.isInstallmentFee()) {
                return -1;
            }
            if (this.isCharge() && this.isInstallmentFee() && otherIsDisb) {
                return 1;
            }
            final ChangeOperation thisKey = downPaymentAnchor().map(ChangeOperation::new).orElse(this);
            final ChangeOperation otherKey = o.downPaymentAnchor().map(ChangeOperation::new).orElse(o);
            if (thisKey != this || otherKey != o) {
                return compareByDownPaymentAnchor(o, thisKey, otherKey);
            }
            if ((thisIsDisb && o.isDownPaymentFee()) || (isDownPaymentFee() && o.isDownPaymentTransaction())) {
                return -1;
            }
            if ((isDownPaymentFee() && otherIsDisb) || (isDownPaymentTransaction() && o.isDownPaymentFee())) {
                return 1;
            }

            final boolean isAccrual = isAccrualActivity();
            if (isAccrual != o.isAccrualActivity()) {
                return isAccrual ? 1 : -1;
            }
            int submittedDate = DateUtils.compareWithNullsLast(getSubmittedOnDate(), o.getSubmittedOnDate());
            if (submittedDate == 0) {
                // Charges submitted on the same day replay in due-date order, so that an earlier payment reprocessed
                // against them pays the charge due first rather than the one created first.
                if (this.isCharge() && o.isCharge()) {
                    int dueDate = DateUtils.compareWithNullsLast(this.loanCharge.get().getDueDate(), o.loanCharge.get().getDueDate());
                    if (dueDate != 0) {
                        return dueDate;
                    }
                }
                return DateUtils.compareWithNullsLast(getCreatedDateTime(), o.getCreatedDateTime());
            }
            return submittedDate;
        }
        return datePortion;
    }

}
