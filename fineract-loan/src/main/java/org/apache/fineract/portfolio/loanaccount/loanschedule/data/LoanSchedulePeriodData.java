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
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.service.DateUtils;

/**
 * Immutable data object that represents a period of a loan schedule.
 *
 */
@Getter
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

    public static LoanSchedulePeriodData disbursementOnlyPeriod(final LocalDate disbursementDate, final BigDecimal principalDisbursed,
            final BigDecimal feeChargesDueAtTimeOfDisbursement, final boolean isDisbursed) {
        final Integer periodNumber = null;
        final LocalDate from = null;
        return new LoanSchedulePeriodData(periodNumber, from, disbursementDate, principalDisbursed, feeChargesDueAtTimeOfDisbursement,
                isDisbursed);
    }

    public static LoanSchedulePeriodData repaymentOnlyPeriod(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final BigDecimal principalDue, final BigDecimal principalOutstanding, final BigDecimal interestDueOnPrincipalOutstanding,
            final BigDecimal feeChargesDueForPeriod, final BigDecimal penaltyChargesDueForPeriod, final BigDecimal totalDueForPeriod,
            final BigDecimal totalInstallmentAmountForPeriod) {

        return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, principalDue, principalOutstanding,
                interestDueOnPrincipalOutstanding, feeChargesDueForPeriod, penaltyChargesDueForPeriod, totalDueForPeriod,
                totalInstallmentAmountForPeriod);
    }

    public static LoanSchedulePeriodData downPaymentOnlyPeriod(final Integer periodNumber, final LocalDate periodDate,
            final BigDecimal principalDue, final BigDecimal principalOutstanding) {
        return new LoanSchedulePeriodData(periodNumber, periodDate, periodDate, principalDue, principalOutstanding);
    }

    public static LoanSchedulePeriodData repaymentPeriodWithPayments(@SuppressWarnings("unused") final Long loanId,
            final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate, final LocalDate obligationsMetOnDate,
            final boolean complete, final BigDecimal principalOriginalDue, final BigDecimal principalPaid,
            final BigDecimal principalWrittenOff, final BigDecimal principalOutstanding, final BigDecimal outstandingPrincipalBalanceOfLoan,
            final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal interestPaid, final BigDecimal interestWaived,
            final BigDecimal interestWrittenOff, final BigDecimal interestOutstanding, final BigDecimal feeChargesDue,
            final BigDecimal feeChargesPaid, final BigDecimal feeChargesWaived, final BigDecimal feeChargesWrittenOff,
            final BigDecimal feeChargesOutstanding, final BigDecimal penaltyChargesDue, final BigDecimal penaltyChargesPaid,
            final BigDecimal penaltyChargesWaived, final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesOutstanding,
            final BigDecimal totalDueForPeriod, final BigDecimal totalPaid, final BigDecimal totalPaidInAdvanceForPeriod,
            final BigDecimal totalPaidLateForPeriod, final BigDecimal totalWaived, final BigDecimal totalWrittenOff,
            final BigDecimal totalOutstanding, final BigDecimal totalActualCostOfLoanForPeriod,
            final BigDecimal totalInstallmentAmountForPeriod, final BigDecimal totalCredits) {

        return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, obligationsMetOnDate, complete, principalOriginalDue,
                principalPaid, principalWrittenOff, principalOutstanding, outstandingPrincipalBalanceOfLoan,
                interestDueOnPrincipalOutstanding, interestPaid, interestWaived, interestWrittenOff, interestOutstanding, feeChargesDue,
                feeChargesPaid, feeChargesWaived, feeChargesWrittenOff, feeChargesOutstanding, penaltyChargesDue, penaltyChargesPaid,
                penaltyChargesWaived, penaltyChargesWrittenOff, penaltyChargesOutstanding, totalDueForPeriod, totalPaid,
                totalPaidInAdvanceForPeriod, totalPaidLateForPeriod, totalWaived, totalWrittenOff, totalOutstanding,
                totalActualCostOfLoanForPeriod, totalInstallmentAmountForPeriod, totalCredits);
    }

    public static LoanSchedulePeriodData withPaidDetail(final LoanSchedulePeriodData loanSchedulePeriodData, final boolean complete,
            final BigDecimal principalPaid, final BigDecimal interestPaid, final BigDecimal feeChargesPaid,
            final BigDecimal penaltyChargesPaid) {

        return new LoanSchedulePeriodData(loanSchedulePeriodData.period, loanSchedulePeriodData.fromDate, loanSchedulePeriodData.dueDate,
                loanSchedulePeriodData.obligationsMetOnDate, complete, loanSchedulePeriodData.principalOriginalDue, principalPaid,
                loanSchedulePeriodData.principalWrittenOff, loanSchedulePeriodData.principalOutstanding,
                loanSchedulePeriodData.principalLoanBalanceOutstanding, loanSchedulePeriodData.interestDue, interestPaid,
                loanSchedulePeriodData.interestWaived, loanSchedulePeriodData.interestWrittenOff,
                loanSchedulePeriodData.interestOutstanding, loanSchedulePeriodData.feeChargesDue, feeChargesPaid,
                loanSchedulePeriodData.feeChargesWaived, loanSchedulePeriodData.feeChargesWrittenOff,
                loanSchedulePeriodData.feeChargesOutstanding, loanSchedulePeriodData.penaltyChargesDue, penaltyChargesPaid,
                loanSchedulePeriodData.penaltyChargesWaived, loanSchedulePeriodData.penaltyChargesWrittenOff,
                loanSchedulePeriodData.penaltyChargesOutstanding, loanSchedulePeriodData.totalDueForPeriod,
                loanSchedulePeriodData.totalPaidForPeriod, loanSchedulePeriodData.totalPaidInAdvanceForPeriod,
                loanSchedulePeriodData.totalPaidLateForPeriod, loanSchedulePeriodData.totalWaivedForPeriod,
                loanSchedulePeriodData.totalWrittenOffForPeriod, loanSchedulePeriodData.totalOutstandingForPeriod,
                loanSchedulePeriodData.totalActualCostOfLoanForPeriod, loanSchedulePeriodData.totalInstallmentAmountForPeriod,
                loanSchedulePeriodData.totalCredits);
    }

    /*
     * constructor used for creating period on loan schedule that is only a disbursement (typically first period)
     */
    private LoanSchedulePeriodData(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final BigDecimal principalDisbursed, final BigDecimal chargesDueAtTimeOfDisbursement, final boolean isDisbursed) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.obligationsMetOnDate = null;
        this.complete = null;
        if (fromDate != null) {
            this.daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(this.fromDate, this.dueDate));
        } else {
            this.daysInPeriod = null;
        }
        this.principalDisbursed = principalDisbursed;
        this.principalOriginalDue = null;
        this.principalDue = null;
        this.principalPaid = null;
        this.principalWrittenOff = null;
        this.principalOutstanding = null;
        this.principalLoanBalanceOutstanding = principalDisbursed;

        this.interestOriginalDue = null;
        this.interestDue = null;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.interestOutstanding = null;

        this.feeChargesDue = chargesDueAtTimeOfDisbursement;
        if (isDisbursed) {
            this.feeChargesPaid = chargesDueAtTimeOfDisbursement;
            this.feeChargesWaived = null;
            this.feeChargesWrittenOff = null;
            this.feeChargesOutstanding = null;
        } else {
            this.feeChargesPaid = null;
            this.feeChargesWaived = null;
            this.feeChargesWrittenOff = null;
            this.feeChargesOutstanding = chargesDueAtTimeOfDisbursement;
        }

        this.penaltyChargesDue = null;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.penaltyChargesOutstanding = null;

        this.totalOriginalDueForPeriod = chargesDueAtTimeOfDisbursement;
        this.totalDueForPeriod = chargesDueAtTimeOfDisbursement;
        this.totalPaidForPeriod = this.feeChargesPaid;
        this.totalPaidInAdvanceForPeriod = null;
        this.totalPaidLateForPeriod = null;
        this.totalWaivedForPeriod = null;
        this.totalWrittenOffForPeriod = null;
        this.totalOutstandingForPeriod = this.feeChargesOutstanding;
        this.totalActualCostOfLoanForPeriod = this.feeChargesDue;
        this.totalInstallmentAmountForPeriod = null;
        if (dueDate.isBefore(DateUtils.getBusinessLocalDate())) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }
        this.totalCredits = BigDecimal.ZERO;
    }

    /*
     * used for repayment only period when creating an empty loan schedule for preview etc
     */
    private LoanSchedulePeriodData(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final BigDecimal principalOriginalDue, final BigDecimal principalOutstanding,
            final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal feeChargesDueForPeriod,
            final BigDecimal penaltyChargesDueForPeriod, final BigDecimal totalDueForPeriod, BigDecimal totalInstallmentAmountForPeriod) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.obligationsMetOnDate = null;
        this.complete = null;
        if (fromDate != null) {
            this.daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(this.fromDate, this.dueDate));
        } else {
            this.daysInPeriod = null;
        }
        this.principalDisbursed = null;
        this.principalOriginalDue = principalOriginalDue;
        this.principalDue = principalOriginalDue;
        this.principalPaid = null;
        this.principalWrittenOff = null;
        this.principalOutstanding = principalOriginalDue;
        this.principalLoanBalanceOutstanding = principalOutstanding;

        this.interestOriginalDue = interestDueOnPrincipalOutstanding;
        this.interestDue = interestDueOnPrincipalOutstanding;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.interestOutstanding = interestDueOnPrincipalOutstanding;

        this.feeChargesDue = feeChargesDueForPeriod;
        this.feeChargesPaid = null;
        this.feeChargesWaived = null;
        this.feeChargesWrittenOff = null;
        this.feeChargesOutstanding = null;

        this.penaltyChargesDue = penaltyChargesDueForPeriod;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.penaltyChargesOutstanding = null;

        this.totalOriginalDueForPeriod = totalDueForPeriod;
        this.totalDueForPeriod = totalDueForPeriod;
        this.totalPaidForPeriod = BigDecimal.ZERO;
        this.totalPaidInAdvanceForPeriod = null;
        this.totalPaidLateForPeriod = null;
        this.totalWaivedForPeriod = null;
        this.totalWrittenOffForPeriod = null;
        this.totalOutstandingForPeriod = totalDueForPeriod;
        this.totalActualCostOfLoanForPeriod = interestDueOnPrincipalOutstanding.add(feeChargesDueForPeriod);
        this.totalInstallmentAmountForPeriod = totalInstallmentAmountForPeriod;

        if (dueDate.isBefore(DateUtils.getBusinessLocalDate())) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }
        this.totalCredits = BigDecimal.ZERO;
    }

    // TODO refactor the class to builder pattern
    private LoanSchedulePeriodData(Integer periodNumber, LocalDate fromDate, LocalDate dueDate, BigDecimal principalDue,
            BigDecimal principalOutstanding) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.obligationsMetOnDate = null;
        this.complete = null;
        this.daysInPeriod = 1;
        this.principalDisbursed = null;
        this.principalOriginalDue = principalDue;
        this.principalDue = principalOriginalDue;
        this.principalPaid = null;
        this.principalWrittenOff = null;
        this.principalOutstanding = principalOriginalDue;
        this.principalLoanBalanceOutstanding = principalOutstanding;

        this.interestOriginalDue = null;
        this.interestDue = null;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.interestOutstanding = null;

        this.feeChargesDue = null;
        this.feeChargesPaid = null;
        this.feeChargesWaived = null;
        this.feeChargesWrittenOff = null;
        this.feeChargesOutstanding = null;

        this.penaltyChargesDue = null;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.penaltyChargesOutstanding = null;

        this.totalOriginalDueForPeriod = principalDue;
        this.totalDueForPeriod = principalDue;
        this.totalPaidForPeriod = BigDecimal.ZERO;
        this.totalPaidInAdvanceForPeriod = null;
        this.totalPaidLateForPeriod = null;
        this.totalWaivedForPeriod = null;
        this.totalWrittenOffForPeriod = null;
        this.totalOutstandingForPeriod = totalDueForPeriod;
        this.totalActualCostOfLoanForPeriod = null;
        this.totalInstallmentAmountForPeriod = totalDueForPeriod;

        if (dueDate.isBefore(DateUtils.getBusinessLocalDate())) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }
        this.totalCredits = BigDecimal.ZERO;
    }

    /*
     * Used for creating loan schedule periods with full information on expected principal, interest & charges along
     * with what portion of each is paid.
     */
    private LoanSchedulePeriodData(final Integer periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final LocalDate obligationsMetOnDate, final boolean complete, final BigDecimal principalOriginalDue,
            final BigDecimal principalPaid, final BigDecimal principalWrittenOff, final BigDecimal principalOutstanding,
            final BigDecimal principalLoanBalanceOutstanding, final BigDecimal interestDueOnPrincipalOutstanding,
            final BigDecimal interestPaid, final BigDecimal interestWaived, final BigDecimal interestWrittenOff,
            final BigDecimal interestOutstanding, final BigDecimal feeChargesDue, final BigDecimal feeChargesPaid,
            final BigDecimal feeChargesWaived, final BigDecimal feeChargesWrittenOff, final BigDecimal feeChargesOutstanding,
            final BigDecimal penaltyChargesDue, final BigDecimal penaltyChargesPaid, final BigDecimal penaltyChargesWaived,
            final BigDecimal penaltyChargesWrittenOff, final BigDecimal penaltyChargesOutstanding, final BigDecimal totalDueForPeriod,
            final BigDecimal totalPaid, final BigDecimal totalPaidInAdvanceForPeriod, final BigDecimal totalPaidLateForPeriod,
            final BigDecimal totalWaived, final BigDecimal totalWrittenOff, final BigDecimal totalOutstanding,
            final BigDecimal totalActualCostOfLoanForPeriod, final BigDecimal totalInstallmentAmountForPeriod,
            final BigDecimal totalCredits) {
        this.period = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.obligationsMetOnDate = obligationsMetOnDate;
        this.complete = complete;
        if (fromDate != null) {
            this.daysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(this.fromDate, this.dueDate));
        } else {
            this.daysInPeriod = null;
        }
        this.principalDisbursed = null;
        this.principalOriginalDue = principalOriginalDue;
        this.principalDue = principalOriginalDue;
        this.principalPaid = principalPaid;
        this.principalWrittenOff = principalWrittenOff;
        this.principalOutstanding = principalOutstanding;
        this.principalLoanBalanceOutstanding = principalLoanBalanceOutstanding;

        this.interestOriginalDue = interestDueOnPrincipalOutstanding;
        this.interestDue = interestDueOnPrincipalOutstanding;
        this.interestPaid = interestPaid;
        this.interestWaived = interestWaived;
        this.interestWrittenOff = interestWrittenOff;
        this.interestOutstanding = interestOutstanding;

        this.feeChargesDue = feeChargesDue;
        this.feeChargesPaid = feeChargesPaid;
        this.feeChargesWaived = feeChargesWaived;
        this.feeChargesWrittenOff = feeChargesWrittenOff;
        this.feeChargesOutstanding = feeChargesOutstanding;

        this.penaltyChargesDue = penaltyChargesDue;
        this.penaltyChargesPaid = penaltyChargesPaid;
        this.penaltyChargesWaived = penaltyChargesWaived;
        this.penaltyChargesWrittenOff = penaltyChargesWrittenOff;
        this.penaltyChargesOutstanding = penaltyChargesOutstanding;

        this.totalOriginalDueForPeriod = totalDueForPeriod;
        this.totalDueForPeriod = totalDueForPeriod;
        this.totalPaidForPeriod = totalPaid;
        this.totalPaidInAdvanceForPeriod = totalPaidInAdvanceForPeriod;
        this.totalPaidLateForPeriod = totalPaidLateForPeriod;
        this.totalWaivedForPeriod = totalWaived;
        this.totalWrittenOffForPeriod = totalWrittenOff;
        this.totalOutstandingForPeriod = totalOutstanding;
        this.totalActualCostOfLoanForPeriod = totalActualCostOfLoanForPeriod;
        this.totalInstallmentAmountForPeriod = totalInstallmentAmountForPeriod;

        if (dueDate.isBefore(DateUtils.getBusinessLocalDate())) {
            this.totalOverdue = this.totalOutstandingForPeriod;
        } else {
            this.totalOverdue = null;
        }
        this.totalCredits = totalCredits;
    }

    private BigDecimal defaultToZeroIfNull(final BigDecimal possibleNullValue) {
        BigDecimal value = BigDecimal.ZERO;
        if (possibleNullValue != null) {
            value = possibleNullValue;
        }
        return value;
    }

    public BigDecimal getPrincipalDisbursed() {
        return defaultToZeroIfNull(this.principalDisbursed);
    }

    public BigDecimal getPrincipalDue() {
        return defaultToZeroIfNull(this.principalDue);
    }

    public BigDecimal getPrincipalPaid() {
        return defaultToZeroIfNull(this.principalPaid);
    }

    public BigDecimal getPrincipalWrittenOff() {
        return defaultToZeroIfNull(this.principalWrittenOff);
    }

    public BigDecimal getPrincipalOutstanding() {
        return defaultToZeroIfNull(this.principalOutstanding);
    }

    public BigDecimal getInterestDue() {
        return defaultToZeroIfNull(this.interestDue);
    }

    public BigDecimal getInterestPaid() {
        return defaultToZeroIfNull(this.interestPaid);
    }

    public BigDecimal getInterestWaived() {
        return defaultToZeroIfNull(this.interestWaived);
    }

    public BigDecimal getInterestWrittenOff() {
        return defaultToZeroIfNull(this.interestWrittenOff);
    }

    public BigDecimal getInterestOutstanding() {
        return defaultToZeroIfNull(this.interestOutstanding);
    }

    public BigDecimal getFeeChargesDue() {
        return defaultToZeroIfNull(this.feeChargesDue);
    }

    public BigDecimal getFeeChargesWaived() {
        return defaultToZeroIfNull(this.feeChargesWaived);
    }

    public BigDecimal getFeeChargesWrittenOff() {
        return defaultToZeroIfNull(this.feeChargesWrittenOff);
    }

    public BigDecimal getFeeChargesPaid() {
        return defaultToZeroIfNull(this.feeChargesPaid);
    }

    public BigDecimal getFeeChargesOutstanding() {
        return defaultToZeroIfNull(this.feeChargesOutstanding);
    }

    public BigDecimal getPenaltyChargesDue() {
        return defaultToZeroIfNull(this.penaltyChargesDue);
    }

    public BigDecimal getPenaltyChargesWaived() {
        return defaultToZeroIfNull(this.penaltyChargesWaived);
    }

    public BigDecimal getPenaltyChargesWrittenOff() {
        return defaultToZeroIfNull(this.penaltyChargesWrittenOff);
    }

    public BigDecimal getPenaltyChargesPaid() {
        return defaultToZeroIfNull(this.penaltyChargesPaid);
    }

    public BigDecimal getPenaltyChargesOutstanding() {
        return defaultToZeroIfNull(this.penaltyChargesOutstanding);
    }

    public BigDecimal getTotalOverdue() {
        return defaultToZeroIfNull(this.totalOverdue);
    }

    public BigDecimal totalOutstandingForPeriod() {
        return defaultToZeroIfNull(this.totalOutstandingForPeriod);
    }
}
