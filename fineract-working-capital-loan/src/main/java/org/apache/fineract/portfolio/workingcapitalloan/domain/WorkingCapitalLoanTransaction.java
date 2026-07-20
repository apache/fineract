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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionTypeConverter;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

@Entity
@Table(name = "m_wc_loan_transaction", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "external_id" }, name = "wc_loan_transaction_external_id_UNIQUE") })
@Getter
public class WorkingCapitalLoanTransaction extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_id", nullable = false)
    private WorkingCapitalLoan wcLoan;

    @Column(name = "transaction_type_id", nullable = false)
    @Convert(converter = LoanTransactionTypeConverter.class)
    private LoanTransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

    @Column(name = "transaction_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal transactionAmount;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_detail_id")
    private PaymentDetail paymentDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_cv_id")
    @Setter
    private CodeValue classification;

    @Column(name = "external_id", length = 100, unique = true)
    @Setter
    private ExternalId externalId;

    @Column(name = "is_reversed", nullable = false)
    @Setter
    private boolean reversed;

    @Column(name = "reversal_external_id", length = 100, unique = true)
    @Setter
    private ExternalId reversalExternalId;

    @Column(name = "reversed_on_date")
    @Setter
    private LocalDate reversedOnDate;

    @Version
    @Column(name = "version")
    private Integer version;

    @OneToOne(mappedBy = "wcLoanTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkingCapitalLoanTransactionAllocation allocation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "fromTransaction")
    private Set<WorkingCapitalLoanTransactionRelation> loanTransactionRelations = new HashSet<>();

    protected WorkingCapitalLoanTransaction() {}

    public LoanTransactionType getTypeOf() {
        return transactionType;
    }

    /**
     * Points this transaction at its allocation, keeping the eager inverse side in step with the owning side.
     *
     * <p>
     * Refuses to swap in a different allocation once one is set. The association is mapped
     * {@code orphanRemoval = true}, so an unchecked reassignment would not fail - it would quietly delete the existing
     * allocation row and replace it, losing its identity and audit trail. That mistake used to be caught by the
     * {@code uq_m_wc_loan_transaction_allocation_transaction_id} unique constraint, back when a second allocation meant
     * a second row; keeping both sides in sync means the constraint no longer gets the chance, so the check lives here
     * instead. Callers that mean to re-allocate must mutate the existing allocation's portions, not build a new one.
     */
    void attachAllocation(final WorkingCapitalLoanTransactionAllocation allocation) {
        if (this.allocation != null && this.allocation != allocation) {
            throw new IllegalStateException("WC loan transaction " + getId() + " already has allocation " + this.allocation.getId()
                    + "; a transaction has exactly one allocation, so re-allocation must update that one in place rather than attach another");
        }
        this.allocation = allocation;
    }

    public static WorkingCapitalLoanTransaction disbursement(final WorkingCapitalLoan loan, final BigDecimal amount,
            final PaymentDetail paymentDetail, final LocalDate disbursementDate, final ExternalId externalId,
            final CodeValue classification) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.DISBURSEMENT, disbursementDate, amount, paymentDetail, classification, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction repayment(final WorkingCapitalLoan loan, final BigDecimal amount,
            final PaymentDetail paymentDetail, final LocalDate transactionDate, final CodeValue classification,
            final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.REPAYMENT, transactionDate, amount, paymentDetail, classification, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction goodwillCredit(final WorkingCapitalLoan loan, final BigDecimal amount,
            final PaymentDetail paymentDetail, final LocalDate transactionDate, final CodeValue classification,
            final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.GOODWILL_CREDIT, transactionDate, amount, paymentDetail, classification, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction payoutRefund(final WorkingCapitalLoan loan, final BigDecimal amount,
            final PaymentDetail paymentDetail, final LocalDate transactionDate, final CodeValue classification,
            final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.PAYOUT_REFUND, transactionDate, amount, paymentDetail, classification, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction creditBalanceRefund(final WorkingCapitalLoan loan, final BigDecimal amount,
            final PaymentDetail paymentDetail, final LocalDate transactionDate, final CodeValue classification,
            final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.CREDIT_BALANCE_REFUND, transactionDate, amount, paymentDetail, classification, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction discountFeeAmortization(final WorkingCapitalLoan loan, final BigDecimal amount,
            final LocalDate transactionDate, final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.DISCOUNT_FEE_AMORTIZATION, transactionDate, amount, null, null, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction discountFeeAmortizationAdjustment(final WorkingCapitalLoan loan, final BigDecimal amount,
            final LocalDate transactionDate, final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT, transactionDate, amount, null, null, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction discountFee(final WorkingCapitalLoan loan, final ExternalId externalId,
            final BigDecimal amount, final LocalDate transactionDate, final CodeValue classification, final PaymentDetail paymentDetail) {
        WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        transaction.wcLoan = loan;
        transaction.transactionType = LoanTransactionType.DISCOUNT_FEE;
        transaction.transactionAmount = amount;
        transaction.transactionDate = transactionDate;
        transaction.submittedOnDate = DateUtils.getBusinessLocalDate();
        transaction.externalId = externalId != null ? externalId : ExternalId.empty();
        transaction.paymentDetail = paymentDetail;
        transaction.classification = classification;
        transaction.reversed = false;
        transaction.reversalExternalId = null;
        transaction.reversedOnDate = null;
        return transaction;
    }

    public static WorkingCapitalLoanTransaction discountFeeAdjustment(final WorkingCapitalLoan loan, final ExternalId externalId,
            final BigDecimal amount, final LocalDate transactionDate, final CodeValue classification, final PaymentDetail paymentDetail) {
        WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        transaction.wcLoan = loan;
        transaction.transactionType = LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT;
        transaction.transactionAmount = amount;
        transaction.transactionDate = transactionDate;
        transaction.submittedOnDate = DateUtils.getBusinessLocalDate();
        transaction.externalId = externalId != null ? externalId : ExternalId.empty();
        transaction.paymentDetail = paymentDetail;
        transaction.classification = classification;
        transaction.reversed = false;
        transaction.reversalExternalId = null;
        transaction.reversedOnDate = null;
        return transaction;
    }

    /**
     * Charge-off is a terminal, non-monetary transaction: it records the charged-off amount (the outstanding balance as
     * of the charge-off date) but does not move the running balance, and it is excluded from replay (not a repayment
     * type). The loan stays ACTIVE.
     */
    public static WorkingCapitalLoanTransaction chargeOff(final WorkingCapitalLoan loan, final BigDecimal amount,
            final LocalDate transactionDate, final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.initialize(loan, LoanTransactionType.CHARGE_OFF, transactionDate, amount, null, null, externalId);
        return txn;
    }

    public static WorkingCapitalLoanTransaction chargeAdjustment(final WorkingCapitalLoan loan, final ExternalId externalId,
            final BigDecimal amount, final LocalDate transactionDate, final PaymentDetail paymentDetail) {
        final WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        transaction.initialize(loan, LoanTransactionType.CHARGE_ADJUSTMENT, transactionDate, amount, paymentDetail, null, externalId);
        return transaction;
    }

    public static WorkingCapitalLoanTransaction accrual(final WorkingCapitalLoan loan, final ExternalId externalId, final BigDecimal amount,
            final LocalDate transactionDate) {
        final WorkingCapitalLoanTransaction transaction = new WorkingCapitalLoanTransaction();
        transaction.initialize(loan, LoanTransactionType.ACCRUAL, transactionDate, amount, null, null, externalId);
        return transaction;
    }

    private void initialize(final WorkingCapitalLoan loan, final LoanTransactionType transactionType, final LocalDate transactionDate,
            final BigDecimal amount, final PaymentDetail paymentDetail, final CodeValue classification, final ExternalId externalId) {
        this.wcLoan = loan;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
        this.transactionAmount = amount;
        this.paymentDetail = paymentDetail;
        this.classification = classification;
        this.externalId = externalId != null ? externalId : ExternalId.empty();
        this.reversed = false;
        this.reversalExternalId = null;
        this.reversedOnDate = null;
    }
}
