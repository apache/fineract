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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.MathUtil;

@Entity
@Table(name = "m_wc_loan_transaction_allocation", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "wc_loan_transaction_id" }, name = "uq_m_wc_loan_transaction_allocation_transaction_id") })
@Getter
public class WorkingCapitalLoanTransactionAllocation extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_transaction_id", nullable = false, unique = true)
    private WorkingCapitalLoanTransaction wcLoanTransaction;

    @Column(name = "principal_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal principalPortion;

    @Column(name = "fee_charges_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal feeChargesPortion;

    @Column(name = "penalty_charges_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal penaltyChargesPortion;

    /**
     * The part of the transaction that moved the overpayment balance: for a repayment the amount paid beyond the
     * outstanding, for a credit balance refund the amount taken back out of the overpayment. Disjoint from
     * {@code principalPortion}, so the portions always sum to the transaction amount for money-moving transactions.
     */
    @Column(name = "overpayment_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal overpaymentPortion;

    @Version
    @Column(name = "version")
    private Integer version;

    protected WorkingCapitalLoanTransactionAllocation() {}

    public static WorkingCapitalLoanTransactionAllocation forPrincipalAllocation(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal principalAmount) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.principalPortion = principalAmount != null ? principalAmount : BigDecimal.ZERO;
        allocation.feeChargesPortion = BigDecimal.ZERO;
        allocation.penaltyChargesPortion = BigDecimal.ZERO;
        allocation.overpaymentPortion = BigDecimal.ZERO;
        allocation.link(transaction);
        return allocation;
    }

    public static WorkingCapitalLoanTransactionAllocation forPortions(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal principalAmount, final BigDecimal feeAmount, final BigDecimal penaltyAmount,
            final BigDecimal overpaymentAmount) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.principalPortion = MathUtil.nullToZero(principalAmount);
        allocation.feeChargesPortion = MathUtil.nullToZero(feeAmount);
        allocation.penaltyChargesPortion = MathUtil.nullToZero(penaltyAmount);
        allocation.overpaymentPortion = MathUtil.nullToZero(overpaymentAmount);
        allocation.link(transaction);
        return allocation;
    }

    public static WorkingCapitalLoanTransactionAllocation forDisbursementDiscount(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal principalAmount) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.principalPortion = MathUtil.nullToZero(principalAmount);
        allocation.feeChargesPortion = BigDecimal.ZERO;
        allocation.penaltyChargesPortion = BigDecimal.ZERO;
        allocation.overpaymentPortion = BigDecimal.ZERO;
        allocation.link(transaction);
        return allocation;
    }

    public static WorkingCapitalLoanTransactionAllocation forDiscountFeeAdjustment(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal principalAmount) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.principalPortion = MathUtil.nullToZero(principalAmount);
        allocation.feeChargesPortion = BigDecimal.ZERO;
        allocation.penaltyChargesPortion = BigDecimal.ZERO;
        allocation.overpaymentPortion = BigDecimal.ZERO;
        allocation.link(transaction);
        return allocation;
    }

    public static WorkingCapitalLoanTransactionAllocation forChargeAccrual(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal amount, final boolean isPenalty) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.principalPortion = BigDecimal.ZERO;
        allocation.feeChargesPortion = isPenalty ? BigDecimal.ZERO : MathUtil.nullToZero(amount);
        allocation.penaltyChargesPortion = isPenalty ? MathUtil.nullToZero(amount) : BigDecimal.ZERO;
        allocation.overpaymentPortion = BigDecimal.ZERO;
        allocation.link(transaction);
        return allocation;
    }

    /**
     * A credit balance refund's split: the part taken back out of the overpayment balance and the over-refund excess
     * that became newly-lent principal (zero while the refund is fully funded by the overpayment).
     */
    public static WorkingCapitalLoanTransactionAllocation forCreditBalanceRefund(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal excessPrincipal, final BigDecimal overpaymentConsumed) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.principalPortion = MathUtil.nullToZero(excessPrincipal);
        allocation.feeChargesPortion = BigDecimal.ZERO;
        allocation.penaltyChargesPortion = BigDecimal.ZERO;
        allocation.overpaymentPortion = MathUtil.nullToZero(overpaymentConsumed);
        allocation.link(transaction);
        return allocation;
    }

    /** Wires both sides of the one-to-one so the transaction's eager inverse reflects this allocation immediately. */
    private void link(final WorkingCapitalLoanTransaction transaction) {
        this.wcLoanTransaction = transaction;
        transaction.attachAllocation(this);
    }
}
