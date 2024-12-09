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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;

/**
 * Immutable data object that represents a period of a loan schedule.
 *
 */
@Getter
@Builder
public final class LoanSchedulePeriodData {

    private final Integer period;
    private final LocalDate fromDate;
    private final LocalDate dueDate;
    private final LocalDate obligationsMetOnDate;
    private final Boolean complete;
    private final Integer daysInPeriod;
    private final BigDecimal principalDisbursed;
    private final BigDecimal principalOriginalDue;
    private final BigDecimal principalDue;
    private final BigDecimal principalPaid;
    private final BigDecimal principalWrittenOff;
    private final BigDecimal principalOutstanding;
    private final BigDecimal principalLoanBalanceOutstanding;
    private final BigDecimal interestOriginalDue;
    private final BigDecimal interestDue;
    private final BigDecimal interestPaid;
    private final BigDecimal interestWaived;
    private final BigDecimal interestWrittenOff;
    private final BigDecimal interestOutstanding;
    private final BigDecimal feeChargesDue;
    private final BigDecimal feeChargesPaid;
    private final BigDecimal feeChargesWaived;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargesOutstanding;
    private final BigDecimal penaltyChargesDue;
    private final BigDecimal penaltyChargesPaid;
    private final BigDecimal penaltyChargesWaived;
    private final BigDecimal penaltyChargesWrittenOff;
    private final BigDecimal penaltyChargesOutstanding;
    private final BigDecimal totalOriginalDueForPeriod;
    private final BigDecimal totalDueForPeriod;
    private final BigDecimal totalPaidForPeriod;
    private final BigDecimal totalPaidInAdvanceForPeriod;
    private final BigDecimal totalPaidLateForPeriod;
    private final BigDecimal totalWaivedForPeriod;
    private final BigDecimal totalWrittenOffForPeriod;
    private final BigDecimal totalOutstandingForPeriod;
    private final BigDecimal totalOverdue;
    private final BigDecimal totalActualCostOfLoanForPeriod;
    private final BigDecimal totalInstallmentAmountForPeriod;
    private final BigDecimal totalCredits;
    private final BigDecimal totalAccruedInterest;
    private final boolean downPaymentPeriod;

    public static LoanSchedulePeriodData disbursementOnlyPeriod(final LocalDate disbursementDate, final BigDecimal principalDisbursed,
            final BigDecimal feeChargesDueAtTimeOfDisbursement, final boolean isDisbursed) {
        return builder().dueDate(disbursementDate) //
                .principalDisbursed(principalDisbursed) //
                .principalLoanBalanceOutstanding(principalDisbursed) //
                .feeChargesDue(feeChargesDueAtTimeOfDisbursement) //
                .feeChargesPaid(isDisbursed ? feeChargesDueAtTimeOfDisbursement : null) //
                .feeChargesOutstanding(isDisbursed ? null : feeChargesDueAtTimeOfDisbursement) //
                .totalOriginalDueForPeriod(feeChargesDueAtTimeOfDisbursement) //
                .totalDueForPeriod(feeChargesDueAtTimeOfDisbursement) //
                .totalPaidForPeriod(isDisbursed ? feeChargesDueAtTimeOfDisbursement : null) //
                .totalOutstandingForPeriod(isDisbursed ? null : feeChargesDueAtTimeOfDisbursement) //
                .totalActualCostOfLoanForPeriod(feeChargesDueAtTimeOfDisbursement) //
                .totalOverdue(DateUtils.isBeforeBusinessDate(disbursementDate) && !isDisbursed ? feeChargesDueAtTimeOfDisbursement : null) //
                .build();
    }

    public static LoanSchedulePeriodData repaymentOnlyPeriod(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final BigDecimal principalDue, final BigDecimal outstandingLoanBalance, final BigDecimal interestDue, final BigDecimal feeDue,
            final BigDecimal penaltyDue) {

        BigDecimal totalDue = MathUtil.add(principalDue, interestDue, feeDue, penaltyDue);
        BigDecimal totalActualCostOfLoanForPeriod = MathUtil.add(interestDue, feeDue, penaltyDue);
        BigDecimal totalInstallmentAmount = MathUtil.add(principalDue, interestDue);

        return builder().period(periodNumber) //
                .fromDate(fromDate) //
                .dueDate(dueDate) //
                .daysInPeriod(DateUtils.getExactDifferenceInDays(fromDate, dueDate)) //
                .principalDue(principalDue) //
                .principalOriginalDue(principalDue) //
                .principalOutstanding(principalDue) //
                .principalLoanBalanceOutstanding(outstandingLoanBalance) //
                .interestDue(interestDue) //
                .interestOriginalDue(interestDue) //
                .interestOutstanding(interestDue) //
                .feeChargesDue(feeDue) //
                .feeChargesOutstanding(feeDue) //
                .penaltyChargesDue(penaltyDue) //
                .penaltyChargesOutstanding(penaltyDue) //
                .totalOriginalDueForPeriod(totalDue) //
                .totalDueForPeriod(totalDue) //
                .totalOutstandingForPeriod(totalDue) //
                .totalActualCostOfLoanForPeriod(totalActualCostOfLoanForPeriod) //
                .totalInstallmentAmountForPeriod(totalInstallmentAmount) //
                .totalOverdue(DateUtils.isBeforeBusinessDate(dueDate) ? totalDue : null) //
                .build();
    }

    public static LoanSchedulePeriodData downPaymentOnlyPeriod(final Integer periodNumber, final LocalDate periodDate,
            final BigDecimal principalDue, final BigDecimal outstandingLoanBalance) {
        return builder().period(periodNumber) //
                .fromDate(periodDate) //
                .dueDate(periodDate) //
                .principalOriginalDue(principalDue) //
                .principalDue(principalDue) //
                .principalOutstanding(principalDue) //
                .principalLoanBalanceOutstanding(outstandingLoanBalance) //
                .totalOriginalDueForPeriod(principalDue) //
                .totalDueForPeriod(principalDue) //
                .totalOutstandingForPeriod(principalDue) //
                .totalInstallmentAmountForPeriod(principalDue) //
                .downPaymentPeriod(true) //
                .totalOverdue(DateUtils.isBeforeBusinessDate(periodDate) ? principalDue : null) //
                .build();
    }

    public static LoanSchedulePeriodData periodWithPayments(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final LocalDate obligationsMetOnDate, final boolean complete, final BigDecimal principalOriginalDue,
            final BigDecimal principalPaid, final BigDecimal principalWrittenOff, final BigDecimal principalOutstanding,
            final BigDecimal outstandingPrincipalBalanceOfLoan, final BigDecimal interestDue, final BigDecimal interestPaid,
            final BigDecimal interestWaived, final BigDecimal interestWrittenOff, final BigDecimal interestOutstanding,
            final BigDecimal feeChargesDue, final BigDecimal feeChargesPaid, final BigDecimal feeChargesWaived,
            final BigDecimal feeChargesWrittenOff, final BigDecimal feeChargesOutstanding, final BigDecimal penaltyChargesDue,
            final BigDecimal penaltyChargesPaid, final BigDecimal penaltyChargesWaived, final BigDecimal penaltyChargesWrittenOff,
            final BigDecimal penaltyChargesOutstanding, final BigDecimal totalPaid, final BigDecimal totalPaidInAdvanceForPeriod,
            final BigDecimal totalPaidLateForPeriod, final BigDecimal totalWaived, final BigDecimal totalWrittenOff,
            final BigDecimal totalCredits, final boolean isDownPayment, final BigDecimal totalAccruedInterest) {

        BigDecimal totalDue = MathUtil.add(principalOriginalDue, interestDue, feeChargesDue, penaltyChargesDue);
        BigDecimal totalOutstanding = MathUtil.add(principalOutstanding, interestOutstanding, feeChargesOutstanding,
                penaltyChargesOutstanding);
        BigDecimal totalActualCostOfLoanForPeriod = MathUtil.add(interestDue, feeChargesDue, penaltyChargesDue);
        BigDecimal totalInstallmentAmount = MathUtil.add(principalOriginalDue, interestDue);

        return builder().period(periodNumber) //
                .fromDate(fromDate) //
                .dueDate(dueDate) //
                .obligationsMetOnDate(obligationsMetOnDate) //
                .complete(complete) //
                .daysInPeriod(DateUtils.getExactDifferenceInDays(fromDate, dueDate)) //
                .principalDue(principalOriginalDue) //
                .principalOriginalDue(principalOriginalDue) //
                .principalPaid(principalPaid) //
                .principalWrittenOff(principalWrittenOff) //
                .principalOutstanding(principalOutstanding) //
                .principalLoanBalanceOutstanding(outstandingPrincipalBalanceOfLoan) //
                .interestDue(interestDue) //
                .interestOriginalDue(interestDue) //
                .interestPaid(interestPaid) //
                .interestWaived(interestWaived) //
                .interestWrittenOff(interestWrittenOff) //
                .interestOutstanding(interestOutstanding) //
                .feeChargesDue(feeChargesDue) //
                .feeChargesPaid(feeChargesPaid) //
                .feeChargesWaived(feeChargesWaived) //
                .feeChargesWrittenOff(feeChargesWrittenOff) //
                .feeChargesOutstanding(feeChargesOutstanding) //
                .penaltyChargesDue(penaltyChargesDue) //
                .penaltyChargesPaid(penaltyChargesPaid) //
                .penaltyChargesWaived(penaltyChargesWaived) //
                .penaltyChargesWrittenOff(penaltyChargesWrittenOff) //
                .penaltyChargesOutstanding(penaltyChargesOutstanding) //
                .totalOriginalDueForPeriod(totalDue) //
                .totalDueForPeriod(totalDue) //
                .totalPaidForPeriod(totalPaid) //
                .totalPaidInAdvanceForPeriod(totalPaidInAdvanceForPeriod) //
                .totalPaidLateForPeriod(totalPaidLateForPeriod) //
                .totalWaivedForPeriod(totalWaived) //
                .totalWrittenOffForPeriod(totalWrittenOff) //
                .totalOutstandingForPeriod(totalOutstanding) //
                .totalActualCostOfLoanForPeriod(totalActualCostOfLoanForPeriod) //
                .totalInstallmentAmountForPeriod(totalInstallmentAmount) //
                .totalOverdue(DateUtils.isBeforeBusinessDate(dueDate) ? totalOutstanding : null) //
                .totalCredits(totalCredits) //
                .downPaymentPeriod(isDownPayment) //
                .totalAccruedInterest(totalAccruedInterest) //
                .build();
    }

    public static LoanSchedulePeriodData withPaidDetail(final LoanSchedulePeriodData loanSchedulePeriodData, final boolean complete,
            final BigDecimal principalPaid, final BigDecimal interestPaid, final BigDecimal feeChargesPaid,
            final BigDecimal penaltyChargesPaid) {
        BigDecimal totalOutstanding = MathUtil.subtract(loanSchedulePeriodData.totalDueForPeriod, principalPaid, interestPaid,
                feeChargesPaid, penaltyChargesPaid);

        return builder().period(loanSchedulePeriodData.period) //
                .fromDate(loanSchedulePeriodData.fromDate) //
                .dueDate(loanSchedulePeriodData.dueDate) //
                .obligationsMetOnDate(loanSchedulePeriodData.obligationsMetOnDate) //
                .complete(complete) //
                .daysInPeriod(DateUtils.getExactDifferenceInDays(loanSchedulePeriodData.fromDate, loanSchedulePeriodData.dueDate)) //
                .principalDue(loanSchedulePeriodData.principalOriginalDue) //
                .principalOriginalDue(loanSchedulePeriodData.principalOriginalDue) //
                .principalPaid(principalPaid) //
                .principalWrittenOff(loanSchedulePeriodData.principalWrittenOff) //
                .principalOutstanding(MathUtil.subtract(loanSchedulePeriodData.principalOriginalDue, principalPaid)) //
                .principalLoanBalanceOutstanding(loanSchedulePeriodData.principalLoanBalanceOutstanding) //
                .interestDue(loanSchedulePeriodData.interestDue) //
                .interestOriginalDue(loanSchedulePeriodData.interestDue) //
                .interestPaid(interestPaid) //
                .interestWaived(loanSchedulePeriodData.interestWaived) //
                .interestWrittenOff(loanSchedulePeriodData.interestWrittenOff) //
                .interestOutstanding(MathUtil.subtract(loanSchedulePeriodData.interestDue, interestPaid)) //
                .feeChargesDue(loanSchedulePeriodData.feeChargesDue) //
                .feeChargesPaid(feeChargesPaid) //
                .feeChargesWaived(loanSchedulePeriodData.feeChargesWaived) //
                .feeChargesWrittenOff(loanSchedulePeriodData.feeChargesWrittenOff) //
                .feeChargesOutstanding(MathUtil.subtract(loanSchedulePeriodData.feeChargesDue, feeChargesPaid)) //
                .penaltyChargesDue(loanSchedulePeriodData.penaltyChargesDue) //
                .penaltyChargesPaid(penaltyChargesPaid) //
                .penaltyChargesWaived(loanSchedulePeriodData.penaltyChargesWaived) //
                .penaltyChargesWrittenOff(loanSchedulePeriodData.penaltyChargesWrittenOff) //
                .penaltyChargesOutstanding(MathUtil.subtract(loanSchedulePeriodData.penaltyChargesDue, penaltyChargesPaid)) //
                .totalOriginalDueForPeriod(loanSchedulePeriodData.totalDueForPeriod) //
                .totalDueForPeriod(loanSchedulePeriodData.totalDueForPeriod) //
                .totalPaidForPeriod(MathUtil.add(principalPaid, interestPaid, feeChargesPaid, penaltyChargesPaid)) //
                .totalPaidInAdvanceForPeriod(loanSchedulePeriodData.totalPaidInAdvanceForPeriod) //
                .totalPaidLateForPeriod(loanSchedulePeriodData.totalPaidLateForPeriod) //
                .totalWaivedForPeriod(loanSchedulePeriodData.totalWaivedForPeriod) //
                .totalWrittenOffForPeriod(loanSchedulePeriodData.totalWrittenOffForPeriod) //
                .totalOutstandingForPeriod(totalOutstanding) //
                .totalActualCostOfLoanForPeriod(loanSchedulePeriodData.totalActualCostOfLoanForPeriod) //
                .totalInstallmentAmountForPeriod(loanSchedulePeriodData.totalInstallmentAmountForPeriod) //
                .totalOverdue(DateUtils.isBeforeBusinessDate(loanSchedulePeriodData.dueDate) ? totalOutstanding : null) //
                .totalCredits(loanSchedulePeriodData.totalCredits) //
                .downPaymentPeriod(loanSchedulePeriodData.isDownPaymentPeriod()) //
                .totalAccruedInterest(loanSchedulePeriodData.totalAccruedInterest) //
                .build();
    }

    public BigDecimal getPrincipalDisbursed() {
        return MathUtil.nullToDefault(this.principalDisbursed, BigDecimal.ZERO);
    }

    public BigDecimal getPrincipalDue() {
        return MathUtil.nullToDefault(this.principalDue, BigDecimal.ZERO);
    }

    public BigDecimal getPrincipalPaid() {
        return MathUtil.nullToDefault(this.principalPaid, BigDecimal.ZERO);
    }

    public BigDecimal getPrincipalWrittenOff() {
        return MathUtil.nullToDefault(this.principalWrittenOff, BigDecimal.ZERO);
    }

    public BigDecimal getPrincipalOutstanding() {
        return MathUtil.nullToDefault(this.principalOutstanding, BigDecimal.ZERO);
    }

    public BigDecimal getInterestDue() {
        return MathUtil.nullToDefault(this.interestDue, BigDecimal.ZERO);
    }

    public BigDecimal getInterestPaid() {
        return MathUtil.nullToDefault(this.interestPaid, BigDecimal.ZERO);
    }

    public BigDecimal getInterestWaived() {
        return MathUtil.nullToDefault(this.interestWaived, BigDecimal.ZERO);
    }

    public BigDecimal getInterestWrittenOff() {
        return MathUtil.nullToDefault(this.interestWrittenOff, BigDecimal.ZERO);
    }

    public BigDecimal getInterestOutstanding() {
        return MathUtil.nullToDefault(this.interestOutstanding, BigDecimal.ZERO);
    }

    public BigDecimal getFeeChargesDue() {
        return MathUtil.nullToDefault(this.feeChargesDue, BigDecimal.ZERO);
    }

    public BigDecimal getFeeChargesWaived() {
        return MathUtil.nullToDefault(this.feeChargesWaived, BigDecimal.ZERO);
    }

    public BigDecimal getFeeChargesWrittenOff() {
        return MathUtil.nullToDefault(this.feeChargesWrittenOff, BigDecimal.ZERO);
    }

    public BigDecimal getFeeChargesPaid() {
        return MathUtil.nullToDefault(this.feeChargesPaid, BigDecimal.ZERO);
    }

    public BigDecimal getFeeChargesOutstanding() {
        return MathUtil.nullToDefault(this.feeChargesOutstanding, BigDecimal.ZERO);
    }

    public BigDecimal getPenaltyChargesDue() {
        return MathUtil.nullToDefault(this.penaltyChargesDue, BigDecimal.ZERO);
    }

    public BigDecimal getPenaltyChargesWaived() {
        return MathUtil.nullToDefault(this.penaltyChargesWaived, BigDecimal.ZERO);
    }

    public BigDecimal getPenaltyChargesWrittenOff() {
        return MathUtil.nullToDefault(this.penaltyChargesWrittenOff, BigDecimal.ZERO);
    }

    public BigDecimal getPenaltyChargesPaid() {
        return MathUtil.nullToDefault(this.penaltyChargesPaid, BigDecimal.ZERO);
    }

    public BigDecimal getPenaltyChargesOutstanding() {
        return MathUtil.nullToDefault(this.penaltyChargesOutstanding, BigDecimal.ZERO);
    }

    public BigDecimal getTotalOverdue() {
        return MathUtil.nullToDefault(this.totalOverdue, BigDecimal.ZERO);
    }

    public BigDecimal totalOutstandingForPeriod() {
        return MathUtil.nullToDefault(this.totalOutstandingForPeriod, BigDecimal.ZERO);
    }

}
