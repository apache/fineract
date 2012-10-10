package org.mifosng.platform.loan.domain;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.Money;

@Deprecated
public class LoanPayoffSummary {

	private final Long reference;
	private final LocalDate acutalDisbursementDate;
	private final LocalDate expectedMaturityDate;
	private final LocalDate projectedMaturityDate;
	private final Money totalPaidToDate;
	private final Money totalOutstandingBasedOnExpectedMaturityDate;
	private final Money totalOutstandingBasedOnPayoffDate;
	private final Money rebateOwed;

	public LoanPayoffSummary(final Long reference,
			final LocalDate acutalDisbursementDate,
			final LocalDate expectedMaturityDate,
			final LocalDate projectedMaturityDate, final Money totalPaidToDate,
			final Money totalOutstandingBasedOnExpectedMaturityDate,
			final Money totalOutstandingBasedOnPayoffDate,
			final Money rebateOwed) {
		this.reference = reference;
		this.acutalDisbursementDate = acutalDisbursementDate;
		this.expectedMaturityDate = expectedMaturityDate;
		this.projectedMaturityDate = projectedMaturityDate;
		this.totalPaidToDate = totalPaidToDate;
		this.totalOutstandingBasedOnExpectedMaturityDate = totalOutstandingBasedOnExpectedMaturityDate;
		this.totalOutstandingBasedOnPayoffDate = totalOutstandingBasedOnPayoffDate;
		this.rebateOwed = rebateOwed;
	}

	public Long getReference() {
		return this.reference;
	}

	public LocalDate getAcutalDisbursementDate() {
		return this.acutalDisbursementDate;
	}

	public LocalDate getExpectedMaturityDate() {
		return this.expectedMaturityDate;
	}

	public LocalDate getProjectedMaturityDate() {
		return this.projectedMaturityDate;
	}

	public Money getTotalPaidToDate() {
		return this.totalPaidToDate;
	}

	public Money getRebateOwed() {
		return this.rebateOwed;
	}

	public Integer getExpectedLoanTermInDays() {
		return Days.daysBetween(this.acutalDisbursementDate,
				this.expectedMaturityDate).getDays();
	}

	public Integer getProjectedLoanTermInDays() {
		return Days.daysBetween(this.acutalDisbursementDate,
				this.projectedMaturityDate).getDays();
	}

	public Money getTotalOutstandingBasedOnExpectedMaturityDate() {
		return this.totalOutstandingBasedOnExpectedMaturityDate;
	}

	public Money getTotalOutstandingBasedOnPayoffDate() {
		return this.totalOutstandingBasedOnPayoffDate;
	}
}