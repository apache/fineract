package org.mifosplatform.portfolio.savingsaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class SavingSchedulePeriodData {

    private final Integer period;
    private final LocalDate dueDate;
    private final BigDecimal depositDue;
    private final BigDecimal depositPaid;
    private final BigDecimal interestAccured;

    public SavingSchedulePeriodData(final Integer period, final LocalDate dueDate, final BigDecimal depositDue, final BigDecimal depositPaid, final BigDecimal interestAccured) {
        this.period = period;
        this.dueDate = dueDate;
        this.depositDue = depositDue;
        this.depositPaid = depositPaid;
        this.interestAccured = interestAccured;
    }

    public static SavingSchedulePeriodData addScheduleInformation(int periodNumber, LocalDate scheduleDate,
            BigDecimal depositAmountPerPeriod, BigDecimal interestAccured) {
        return new SavingSchedulePeriodData(periodNumber, scheduleDate, depositAmountPerPeriod, BigDecimal.ZERO, interestAccured);
    }

    public Integer getPeriod() {
        return this.period;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public BigDecimal getDepositDue() {
        return this.depositDue;
    }

    public BigDecimal getDepositPaid() {
        return this.depositPaid;
    }

	public BigDecimal getInterestAccured() {
		return this.interestAccured;
	}
}
