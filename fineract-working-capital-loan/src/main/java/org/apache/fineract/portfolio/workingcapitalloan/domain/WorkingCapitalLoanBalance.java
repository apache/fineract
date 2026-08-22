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
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;

/**
 * Stores all balances of a working capital loan (one row per loan). Updated from allocations; accounting depends on
 * this.
 */
@Entity
@Table(name = "m_wc_loan_balance", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "wc_loan_id" }, name = "uq_m_wc_loan_balance_loan_id") })
@Getter
public class WorkingCapitalLoanBalance extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_id", nullable = false, unique = true)
    private WorkingCapitalLoan wcLoan;

    /**
     * Total repayable principal: the disbursed amount plus the discount fee, adjusted by later discount changes. Zero
     * until disbursement, and zero again once a disbursal is undone - a loan whose disbursal was undone is
     * indistinguishable from one that was only ever approved, because in both cases nothing has been paid out.
     */
    @Column(name = "principal", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal principal = BigDecimal.ZERO;

    @Column(name = "principal_paid", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal principalPaid = BigDecimal.ZERO;

    @Column(name = "principal_adjustment", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal principalAdjustment = BigDecimal.ZERO;

    @Column(name = "fee", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "fee_paid", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal feePaid = BigDecimal.ZERO;

    @Column(name = "penalty", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal penalty = BigDecimal.ZERO;

    @Column(name = "penalty_paid", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal penaltyPaid = BigDecimal.ZERO;

    /**
     * Portions moved out of the outstanding balance by a write-off. They lower the computed outstanding to zero without
     * touching the paid columns, so an undo simply resets them back to zero and the original outstanding reappears.
     */
    @Column(name = "principal_written_off", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal principalWrittenOff = BigDecimal.ZERO;

    @Column(name = "fee_written_off", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal feeWrittenOff = BigDecimal.ZERO;

    @Column(name = "penalty_written_off", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal penaltyWrittenOff = BigDecimal.ZERO;

    @Column(name = "realized_income_from_discount_fee", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal realizedIncomeFromDiscountFee = BigDecimal.ZERO;

    @Column(name = "overpayment_amount", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal overpaymentAmount = BigDecimal.ZERO;

    /** Total amount disbursed on the loan, excluding the discount fee. Reset to zero when the disbursal is undone. */
    @Column(name = "total_disbursement", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal totalDisbursement = BigDecimal.ZERO;

    @Column(name = "total_discount_fee", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal totalDiscountFee = BigDecimal.ZERO;

    @Column(name = "total_discount_fee_adjustment", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal totalDiscountFeeAdjustment = BigDecimal.ZERO;

    @Column(name = "breach_pastdue_amount", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal breachPastDueAmount = BigDecimal.ZERO;

    @Version
    @Column(name = "version")
    private Integer version;

    protected WorkingCapitalLoanBalance() {}

    public static WorkingCapitalLoanBalance createFor(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanBalance balance = new WorkingCapitalLoanBalance();
        balance.wcLoan = loan;
        return balance;
    }

    public void applyDisbursement(final BigDecimal disbursedAmount) {
        final BigDecimal discount = Optional.ofNullable(wcLoan.getLoanProductRelatedDetails())
                .map(WorkingCapitalLoanProductRelatedDetails::getDiscount).orElse(BigDecimal.ZERO);
        this.totalDiscountFee = discount;
        this.principal = disbursedAmount.add(discount);
        this.totalDisbursement = disbursedAmount;
        this.overpaymentAmount = BigDecimal.ZERO;
    }

    public BigDecimal getTotalPrincipalDue() {
        return MathUtil.add(getPrincipal(), getPrincipalAdjustment());
    }

    public BigDecimal getPrincipalOutstanding() {
        return MathUtil.subtract(getTotalPrincipalDue(), getPrincipalPaid(), getPrincipalWrittenOff()).max(BigDecimal.ZERO);
    }

    public BigDecimal getFeeOutstanding() {
        return MathUtil.subtract(getFee(), getFeePaid(), getFeeWrittenOff()).max(BigDecimal.ZERO);
    }

    public BigDecimal getPenaltyOutstanding() {
        return MathUtil.subtract(getPenalty(), getPenaltyPaid(), getPenaltyWrittenOff()).max(BigDecimal.ZERO);
    }

    public BigDecimal getTotalOutstanding() {
        return MathUtil.add(getPrincipalOutstanding()).add(getFeeOutstanding()).add(getPenaltyOutstanding());
    }

    public BigDecimal getTotalExpectedRepayment() {
        return MathUtil.add(getPrincipal()).add(getPrincipalAdjustment()).add(getPenalty()).add(getFee());
    }

    public BigDecimal getTotalRepayment() {
        return MathUtil.add(getPrincipalPaid()).add(getFeePaid()).add(getPenaltyPaid());
    }

    public BigDecimal getUnrealizedIncomeFromDiscountFee() {
        return MathUtil
                .subtract(MathUtil.subtract(getTotalDiscountFee(), getTotalDiscountFeeAdjustment()), getRealizedIncomeFromDiscountFee())
                .max(BigDecimal.ZERO);
    }
}
