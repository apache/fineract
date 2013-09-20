/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

/**
 * Domain representation of a Loan Schedule Repayment Period (not used for
 * persistence)
 */
public final class LoanScheduleModelRepaymentPeriod implements LoanScheduleModelPeriod {

    private final int periodNumber;
    private final LocalDate fromDate;
    private final LocalDate dueDate;
    private final Money principalDue;
    private final Money outstandingLoanBalance;
    private final Money interestDue;
    private final Money feeChargesDue;
    private final Money penaltyChargesDue;
    private final Money totalDue;

    public static LoanScheduleModelRepaymentPeriod repayment(final int periodNumber, final LocalDate startDate,
            final LocalDate scheduledDueDate, final Money principalDue, final Money outstandingLoanBalance, final Money interestDue,
            final Money feeChargesDue, final Money penaltyChargesDue, final Money totalDue) {

        return new LoanScheduleModelRepaymentPeriod(periodNumber, startDate, scheduledDueDate, principalDue, outstandingLoanBalance,
                interestDue, feeChargesDue, penaltyChargesDue, totalDue);
    }

    public LoanScheduleModelRepaymentPeriod(final int periodNumber, final LocalDate fromDate, final LocalDate dueDate,
            final Money principalDue, final Money outstandingLoanBalance, final Money interestDue, final Money feeChargesDue,
            final Money penaltyChargesDue, final Money totalDue) {
        this.periodNumber = periodNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.principalDue = principalDue;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.interestDue = interestDue;
        this.feeChargesDue = feeChargesDue;
        this.penaltyChargesDue = penaltyChargesDue;
        this.totalDue = totalDue;
    }

    @Override
    public LoanSchedulePeriodData toData() {
        return LoanSchedulePeriodData.repaymentOnlyPeriod(this.periodNumber, this.fromDate, this.dueDate, this.principalDue.getAmount(),
                this.outstandingLoanBalance.getAmount(), this.interestDue.getAmount(), this.feeChargesDue.getAmount(),
                this.penaltyChargesDue.getAmount(), this.totalDue.getAmount());
    }

    @Override
    public boolean isRepaymentPeriod() {
        return true;
    }

    @Override
    public Integer periodNumber() {
        return this.periodNumber;
    }

    @Override
    public LocalDate periodFromDate() {
        return this.fromDate;
    }

    @Override
    public LocalDate periodDueDate() {
        return this.dueDate;
    }

    @Override
    public BigDecimal principalDue() {
        BigDecimal value = null;
        if (this.principalDue != null) {
            value = this.principalDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal interestDue() {
        BigDecimal value = null;
        if (this.interestDue != null) {
            value = this.interestDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal feeChargesDue() {
        BigDecimal value = null;
        if (this.feeChargesDue != null) {
            value = this.feeChargesDue.getAmount();
        }

        return value;
    }

    @Override
    public BigDecimal penaltyChargesDue() {
        BigDecimal value = null;
        if (this.penaltyChargesDue != null) {
            value = this.penaltyChargesDue.getAmount();
        }

        return value;
    }
}