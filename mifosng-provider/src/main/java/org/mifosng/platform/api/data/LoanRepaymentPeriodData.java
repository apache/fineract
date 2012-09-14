package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing a repayment period of a loan schedule.
 */
public class LoanRepaymentPeriodData {

	@SuppressWarnings("unused")
	private final Long loanId;
	@SuppressWarnings("unused")
	private final Integer period;
	private final LocalDate date;
	private final BigDecimal principal;
	private final BigDecimal principalPaid;
	private final BigDecimal principalOutstanding;
	private final BigDecimal interest;
	private final BigDecimal interestPaid;
	private final BigDecimal interestWaived;
	private final BigDecimal interestOutstanding;
	private final BigDecimal total;
	private final BigDecimal totalPaid;
	private final BigDecimal totalWaived;
	private final BigDecimal totalOutstanding;

	public LoanRepaymentPeriodData(final Long loanId, final Integer period,
			final LocalDate date, final BigDecimal principal, final BigDecimal principalPaid,
			final BigDecimal principalOutstanding, final BigDecimal interest,
			final BigDecimal interestPaid, final BigDecimal interestWaived, final BigDecimal interestOutstanding,
			final BigDecimal total, final BigDecimal totalPaid, final BigDecimal totalWaived, final BigDecimal totalOutstanding) {
		this.loanId = loanId;
		this.period = period;
		this.date = date;
		this.principal = principal;
		this.principalPaid = principalPaid;
		this.principalOutstanding = principalOutstanding;
		this.interest = interest;
		this.interestPaid = interestPaid;
		this.interestWaived = interestWaived;
		this.interestOutstanding = interestOutstanding;
		this.total = total;
		this.totalPaid = totalPaid;
		this.totalWaived = totalWaived;
		this.totalOutstanding = totalOutstanding;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public BigDecimal getPrincipalPaid() {
		return principalPaid;
	}

	public BigDecimal getPrincipalOutstanding() {
		return principalOutstanding;
	}

	public BigDecimal getInterest() {
		return interest;
	}

	public BigDecimal getInterestPaid() {
		return interestPaid;
	}

	public BigDecimal getInterestWaived() {
		return interestWaived;
	}

	public BigDecimal getInterestOutstanding() {
		return interestOutstanding;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public BigDecimal getTotalPaid() {
		return totalPaid;
	}

	public BigDecimal getTotalWaived() {
		return totalWaived;
	}

	public BigDecimal getTotalOutstanding() {
		return totalOutstanding;
	}
}