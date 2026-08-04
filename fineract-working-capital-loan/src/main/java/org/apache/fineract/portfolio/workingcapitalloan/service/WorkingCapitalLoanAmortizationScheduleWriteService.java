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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;

public interface WorkingCapitalLoanAmortizationScheduleWriteService {

    /**
     * A principal repayment applied to the amortization schedule on a given date. {@code createdDate} and
     * {@code transactionId} are the source transaction's audit creation timestamp and id; together they order this
     * payment against the <em>rate changes</em> it is replayed alongside, which is the only same-date ordering that can
     * change the resulting schedule. Creation time is the tie-break here because it is the sole key a payment and a
     * rate change share - a rate change has no submitted-on date - and not because it is the loan module's rule: that
     * one ranks submitted-on date ahead of creation time, and the allocation replay follows it. The two cannot disagree
     * on an outcome, because the model aggregates payments by date before projecting, so the relative order of two
     * payments sharing a date is immaterial. Both are {@code null} when the payment is reconstructed from the schedule
     * model rather than from a transaction.
     */
    record PrincipalPayment(LocalDate date, BigDecimal amount, OffsetDateTime createdDate, Long transactionId) {
    }

    /** Principal re-injected on a given date by an over-refunding credit balance refund. */
    record PrincipalAdjustment(LocalDate date, BigDecimal amount) {
    }

    void generateAndSaveAmortizationSchedule(Long loanId, ProjectedAmortizationScheduleGenerateRequest request);

    void generateAndSaveAmortizationScheduleOnDisbursement(WorkingCapitalLoan loan, BigDecimal disbursedAmount, LocalDate disbursementDate);

    void generateAndSaveAmortizationScheduleOnApproval(WorkingCapitalLoan loan);

    void regenerateAmortizationScheduleOnUndoDisbursal(WorkingCapitalLoan loan);

    void applyRepayment(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal repaymentAmount);

    BigDecimal getWorkingCapitalLoanDiscountAmount(WorkingCapitalLoan loan);

    void applyRepaymentUndo(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal repaymentAmount);

    void regenerateAmortizationScheduleOnRateChange(WorkingCapitalLoan loan, BigDecimal newRate);

    /**
     * After a discount fee adjustment: regenerates the projected schedule with the new loan-level discount (as on
     * disbursement generation) and re-applies recorded actual repayments only.
     */
    void applyDiscountFeeAdjustment(WorkingCapitalLoan loan);

    /**
     * Rebuilds the projected schedule from scratch (as on disbursement) and re-applies the given principal payments in
     * chronological order, then the given principal adjustments. Used by transaction reprocessing, where re-allocation
     * can change the principal portion recorded on each transaction date.
     */
    void rebuildScheduleFromPrincipalPayments(WorkingCapitalLoan loan, List<PrincipalPayment> principalPayments,
            List<PrincipalAdjustment> principalAdjustments);
}
