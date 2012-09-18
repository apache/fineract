package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Immutable data object that represents a period of a loan schedule.
 * 
 */
@SuppressWarnings("unused")
public class LoanSchedulePeriodData {

	private final Integer period;
	private final LocalDate fromDate;
	private final LocalDate dueDate;
	private final Integer daysInPeriod;
	private final BigDecimal principalDisbursed;
	private final BigDecimal principalOriginalDue;
	private final BigDecimal principalDue;
	private final BigDecimal principalPaid;
	private final BigDecimal principalOutstanding;
	private final BigDecimal principalTotalBalanceOutstanding;
	
	private final BigDecimal interestOriginalDue;
	private final BigDecimal interestDue;
	private final BigDecimal interestPaid;
	private final BigDecimal interestOutstanding;
	
	private final BigDecimal chargesDue;
	private final BigDecimal chargesPaid;
	private final BigDecimal chargesOutstanding;
	
	private final BigDecimal totalDueForPeriod;

	public static LoanSchedulePeriodData disbursement(final LocalDate disbursementDate, final BigDecimal principalDisbursed) {
		Integer periodNumber = Integer.valueOf(0);
		LocalDate from = null;
		return new LoanSchedulePeriodData(periodNumber, from, disbursementDate, principalDisbursed);
	}
	
	public static LoanSchedulePeriodData repaymentPeriod(final Integer periodNumber,
			final LocalDate fromDate, final LocalDate dueDate, final BigDecimal principalDue,
			final BigDecimal principalOutstanding, final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal totalDueForPeriod) {
		
		BigDecimal principalDisbursed = null;
		return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, principalDue, principalOutstanding, interestDueOnPrincipalOutstanding, totalDueForPeriod);
	}
	
	private LoanSchedulePeriodData(
			final Integer periodNumber, 
			final LocalDate fromDate, 
			final LocalDate dueDate,
			final BigDecimal principalDisbursed) {
		this.period = periodNumber;
		this.fromDate = fromDate;
		this.dueDate = dueDate;
		if (fromDate != null) {
			this.daysInPeriod = Days.daysBetween(fromDate, dueDate).getDays();
		} else {
			this.daysInPeriod = null;
		}
		this.principalDisbursed = principalDisbursed;
		this.principalOriginalDue = null;
		this.principalDue = null;
		this.principalPaid = null;
		this.principalOutstanding = null;
		this.principalTotalBalanceOutstanding = principalDisbursed;
		
		this.interestOriginalDue = null;
		this.interestDue = null;
		this.interestPaid = null;
		this.interestOutstanding = null;
	
		this.chargesDue = null;
		this.chargesPaid = null;
		this.chargesOutstanding = null;
		
		this.totalDueForPeriod = null;
	}
	
	private LoanSchedulePeriodData(
			final Integer periodNumber, 
			final LocalDate fromDate, 
			final LocalDate dueDate,
			final BigDecimal principalOriginalDue, final BigDecimal principalOutstanding, final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal totalDueForPeriod) {
		this.period = periodNumber;
		this.fromDate = fromDate;
		this.dueDate = dueDate;
		if (fromDate != null) {
			this.daysInPeriod = Days.daysBetween(fromDate, dueDate).getDays();
		} else {
			this.daysInPeriod = null;
		}
		this.principalDisbursed = null;
		this.principalOriginalDue = principalOriginalDue;
		this.principalDue = principalOriginalDue;
		this.principalPaid = null;
		this.principalOutstanding = principalOriginalDue;
		this.principalTotalBalanceOutstanding = principalOutstanding;
		
		this.interestOriginalDue = interestDueOnPrincipalOutstanding;
		this.interestDue = interestDueOnPrincipalOutstanding;
		this.interestPaid = null;
		this.interestOutstanding = interestDueOnPrincipalOutstanding;
	
		this.chargesDue = null;
		this.chargesPaid = null;
		this.chargesOutstanding = null;
		
		this.totalDueForPeriod = totalDueForPeriod;
	}

	public boolean isRepaymentPeriod() {
		boolean isRepaymentPeriod = false;
		if (principalDue != null && this.interestDue != null) {
			isRepaymentPeriod = BigDecimal.ZERO.compareTo(this.principalDue) == -1 || BigDecimal.ZERO.compareTo(this.interestDue) == -1;
		}
		return isRepaymentPeriod;
	}

	public Integer periodNumber() {
		return this.period;
	}

	public LocalDate periodDueDate() {
		return this.dueDate;
	}

	public BigDecimal principalDue() {
		return this.principalDue;
	}

	public BigDecimal interestDue() {
		return this.interestDue;
	}
}