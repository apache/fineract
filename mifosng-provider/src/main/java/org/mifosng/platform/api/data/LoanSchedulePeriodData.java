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
	private final BigDecimal principalLoanBalanceOutstanding;
	
	private final BigDecimal interestOriginalDue;
	private final BigDecimal interestDue;
	private final BigDecimal interestPaid;
	private final BigDecimal interestWaived;
	private final BigDecimal interestOutstanding;
	
	private final BigDecimal chargesDue;
	private final BigDecimal chargesPaid;
	private final BigDecimal chargesOutstanding;
	
	private final BigDecimal totalOriginalDueForPeriod;
	private final BigDecimal totalDueForPeriod;
	private final BigDecimal totalPaidForPeriod;
	private final BigDecimal totalWaivedForPeriod;
	private final BigDecimal totalOutstandingForPeriod;
	private final BigDecimal totalOverdue;

	public static LoanSchedulePeriodData disbursementOnlyPeriod(
			final LocalDate disbursementDate, 
			final BigDecimal principalDisbursed, 
			final BigDecimal chargesDueAtTimeOfDisbursement, 
			final boolean isDisbursed) {
		Integer periodNumber = Integer.valueOf(0);
		LocalDate from = null;
		return new LoanSchedulePeriodData(periodNumber, from, disbursementDate, principalDisbursed, chargesDueAtTimeOfDisbursement, isDisbursed);
	}
	
	public static LoanSchedulePeriodData repaymentOnlyPeriod(final Integer periodNumber,
			final LocalDate fromDate, final LocalDate dueDate, final BigDecimal principalDue,
			final BigDecimal principalOutstanding, final BigDecimal interestDueOnPrincipalOutstanding, final BigDecimal totalDueForPeriod) {
		
		BigDecimal principalDisbursed = null;
		return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, principalDue, principalOutstanding, interestDueOnPrincipalOutstanding, totalDueForPeriod);
	}
	
	public static LoanSchedulePeriodData repaymentPeriodWithPayments(
			final Long loanId, 
			final Integer periodNumber, 
			final LocalDate fromDate, 
			final LocalDate dueDate, 
			final BigDecimal principalOriginalDue,
			final BigDecimal principalPaid,
			final BigDecimal principalOutstanding,
			final BigDecimal outstandingPrincipalBalanceOfLoan,
			final BigDecimal interestDueOnPrincipalOutstanding, 
			final BigDecimal interestPaid,
			final BigDecimal interestWaived, 
			final BigDecimal interestOutstanding,
			final BigDecimal totalDueForPeriod, 
			final BigDecimal totalPaid, 
			final BigDecimal totalWaived,
			final BigDecimal totalOutstanding) {
		
		return new LoanSchedulePeriodData(periodNumber, fromDate, dueDate, 
				principalOriginalDue, principalPaid, principalOutstanding, outstandingPrincipalBalanceOfLoan, 
				interestDueOnPrincipalOutstanding, interestPaid, interestWaived, interestOutstanding,
				totalDueForPeriod, totalPaid, totalWaived, totalOutstanding);
	}
	
	/*
	 * constructor used for creating period on loan schedule that is only a disbursement (typically first period) 
	 */
	private LoanSchedulePeriodData(
			final Integer periodNumber, 
			final LocalDate fromDate, 
			final LocalDate dueDate,
			final BigDecimal principalDisbursed,
			final BigDecimal chargesDueAtTimeOfDisbursement, 
			final boolean isDisbursed) {
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
		this.principalLoanBalanceOutstanding = principalDisbursed;
		
		this.interestOriginalDue = null;
		this.interestDue = null;
		this.interestPaid = null;
		this.interestWaived = null;
		this.interestOutstanding = null;
	
		this.chargesDue = chargesDueAtTimeOfDisbursement;
		if (isDisbursed) {
			this.chargesPaid = chargesDueAtTimeOfDisbursement;
			this.chargesOutstanding = BigDecimal.ZERO;
		} else {
			this.chargesPaid = BigDecimal.ZERO;
			this.chargesOutstanding = chargesDueAtTimeOfDisbursement;
		}
		
		this.totalOriginalDueForPeriod = chargesDueAtTimeOfDisbursement;
		this.totalDueForPeriod = chargesDueAtTimeOfDisbursement;
		this.totalPaidForPeriod = this.chargesPaid;
		this.totalWaivedForPeriod = null;
		this.totalOutstandingForPeriod = this.chargesOutstanding;
		if (dueDate.isBefore(new LocalDate())) {
			this.totalOverdue = this.totalOutstandingForPeriod;
		} else {
			this.totalOverdue = null;
		}
	}
	
	private LoanSchedulePeriodData(
			final Integer periodNumber, 
			final LocalDate fromDate, 
			final LocalDate dueDate,
			final BigDecimal principalOriginalDue,
			final BigDecimal principalOutstanding, 
			final BigDecimal interestDueOnPrincipalOutstanding, 
			final BigDecimal totalDueForPeriod) {
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
		this.principalLoanBalanceOutstanding = principalOutstanding;
		
		this.interestOriginalDue = interestDueOnPrincipalOutstanding;
		this.interestDue = interestDueOnPrincipalOutstanding;
		this.interestPaid = null;
		this.interestWaived = null;
		this.interestOutstanding = interestDueOnPrincipalOutstanding;
	
		this.chargesDue = BigDecimal.ZERO;
		this.chargesPaid = null;
		this.chargesOutstanding = null;
		
		this.totalOriginalDueForPeriod = totalDueForPeriod;
		this.totalDueForPeriod = totalDueForPeriod;
		this.totalPaidForPeriod = BigDecimal.ZERO;
		this.totalWaivedForPeriod = null;
		this.totalOutstandingForPeriod = totalDueForPeriod;
		if (dueDate.isBefore(new LocalDate())) {
			this.totalOverdue = this.totalOutstandingForPeriod;
		} else {
			this.totalOverdue = null;
		}
	}
	
	/*
	 * Used for creating loan schedule periods with full information on expected principal, interest & charges along with what portion of each is paid. 
	 */
	private LoanSchedulePeriodData(
			final Integer periodNumber, 
			final LocalDate fromDate, 
			final LocalDate dueDate,
			final BigDecimal principalOriginalDue,
			final BigDecimal principalPaid,
			final BigDecimal principalOutstanding, 
			final BigDecimal principalLoanBalanceOutstanding, 
			final BigDecimal interestDueOnPrincipalOutstanding, 
			final BigDecimal interestPaid,
			final BigDecimal interestWaived, 
			final BigDecimal interestOutstanding,
			final BigDecimal totalDueForPeriod,
			final BigDecimal totalPaid, 
			final BigDecimal totalWaived,
			final BigDecimal totalOutstanding) {
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
		this.principalPaid = principalPaid;
		this.principalOutstanding = principalOutstanding;
		this.principalLoanBalanceOutstanding = principalLoanBalanceOutstanding;
		
		this.interestOriginalDue = interestDueOnPrincipalOutstanding;
		this.interestDue = interestDueOnPrincipalOutstanding;
		this.interestPaid = interestPaid;
		this.interestWaived = interestWaived;
		this.interestOutstanding = interestOutstanding;
	
		this.chargesDue = BigDecimal.ZERO;
		this.chargesPaid = BigDecimal.ZERO;
		this.chargesOutstanding = BigDecimal.ZERO;
		
		this.totalOriginalDueForPeriod = totalDueForPeriod;
		this.totalDueForPeriod = totalDueForPeriod;
		this.totalPaidForPeriod = totalPaid;
		this.totalWaivedForPeriod = totalWaived;
		this.totalOutstandingForPeriod = totalOutstanding;
		
		if (dueDate.isBefore(new LocalDate())) {
			this.totalOverdue = this.totalOutstandingForPeriod;
		} else {
			this.totalOverdue = null;
		}
	}
	
	private BigDecimal defaultToZeroIfNull(final BigDecimal possibleNullValue) {
		BigDecimal value = BigDecimal.ZERO;
		if (possibleNullValue != null) {
			value = possibleNullValue;
		}
		return value;
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
	
	public Integer daysInPeriod() {
		return this.daysInPeriod;
	}
	
	public BigDecimal principalDisbursed() {
		return defaultToZeroIfNull(this.principalDisbursed);
	}

	public BigDecimal principalDue() {
		return defaultToZeroIfNull(this.principalDue);
	}

	public BigDecimal principalPaid() {
		return defaultToZeroIfNull(this.principalPaid);
	}
	
	public BigDecimal principalOutstanding() {
		return defaultToZeroIfNull(this.principalOutstanding);
	}
	
	public BigDecimal interestDue() {
		return defaultToZeroIfNull(this.interestDue);
	}

	public BigDecimal interestPaid() {
		return defaultToZeroIfNull(this.interestPaid);
	}

	public BigDecimal interestWaived() {
		return defaultToZeroIfNull(this.interestWaived);
	}

	public BigDecimal interestOutstanding() {
		return defaultToZeroIfNull(this.interestOutstanding);
	}

	public BigDecimal chargesDue() {
		return defaultToZeroIfNull(this.chargesDue);
	}

	public BigDecimal chargesPaid() {
		return defaultToZeroIfNull(this.chargesPaid);
	}

	public BigDecimal chargesOutstanding() {
		return defaultToZeroIfNull(this.chargesOutstanding);
	}

	public BigDecimal totalOverdue() {
		return defaultToZeroIfNull(this.totalOverdue);
	}
}