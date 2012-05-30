package org.mifosng.platform.loan.service;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosng.data.MoneyData;

/**
 *
 */
public class LoanPayoffReadModel {

	private String reference;
	private String acutalDisbursementDate;
	private String expectedMaturityDate;
	private String projectedMaturityDate;
	private Integer expectedLoanTermInDays;
	private Integer projectedLoanTermInDays;
	private MoneyData totalPaidToDate;
	private MoneyData totalOutstandingBasedOnExpectedMaturityDate;
	private MoneyData totalOutstandingBasedOnPayoffDate;
	private MoneyData interestRebateOwed;

	protected LoanPayoffReadModel() {
		//
	}

	public LoanPayoffReadModel(
			final String reference,
			final LocalDate acutalDisbursementDate,
			final LocalDate expectedMaturityDate,
			final LocalDate projectedMaturityDate,
			final Integer expectedLoanTermInDays,
			final Integer projectedLoanTermInDays,
			final MoneyData totalPaidToDate,
			final MoneyData totalOutstandingBasedOnExpectedMaturityDate,
			final MoneyData totalOutstandingBasedOnPayoffDate,
			final MoneyData interestRebateOwed) {

		this.reference = reference;

		DateTimeFormatter formatter = org.joda.time.format.DateTimeFormat
				.forPattern("yyyy-MM-dd");
		this.acutalDisbursementDate = formatter.print(acutalDisbursementDate);
		this.expectedMaturityDate = formatter.print(expectedMaturityDate);
		this.projectedMaturityDate = formatter.print(projectedMaturityDate);
		this.expectedLoanTermInDays = expectedLoanTermInDays;
		this.projectedLoanTermInDays = projectedLoanTermInDays;

		this.totalPaidToDate = totalPaidToDate;
		this.totalOutstandingBasedOnExpectedMaturityDate = totalOutstandingBasedOnExpectedMaturityDate;
		this.totalOutstandingBasedOnPayoffDate = totalOutstandingBasedOnPayoffDate;
		this.interestRebateOwed = interestRebateOwed;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(final String reference) {
		this.reference = reference;
	}

	public String getAcutalDisbursementDate() {
		return this.acutalDisbursementDate;
	}

	public void setAcutalDisbursementDate(final String acutalDisbursementDate) {
		this.acutalDisbursementDate = acutalDisbursementDate;
	}

	public String getExpectedMaturityDate() {
		return this.expectedMaturityDate;
	}

	public void setExpectedMaturityDate(final String expectedMaturityDate) {
		this.expectedMaturityDate = expectedMaturityDate;
	}

	public String getProjectedMaturityDate() {
		return this.projectedMaturityDate;
	}

	public void setProjectedMaturityDate(final String projectedMaturityDate) {
		this.projectedMaturityDate = projectedMaturityDate;
	}

	public Integer getExpectedLoanTermInDays() {
		return this.expectedLoanTermInDays;
	}

	public void setExpectedLoanTermInDays(final Integer expectedLoanTermInDays) {
		this.expectedLoanTermInDays = expectedLoanTermInDays;
	}

	public Integer getProjectedLoanTermInDays() {
		return this.projectedLoanTermInDays;
	}

	public void setProjectedLoanTermInDays(final Integer projectedLoanTermInDays) {
		this.projectedLoanTermInDays = projectedLoanTermInDays;
	}

	public MoneyData getTotalPaidToDate() {
		return this.totalPaidToDate;
	}

	public void setTotalPaidToDate(final MoneyData totalPaidToDate) {
		this.totalPaidToDate = totalPaidToDate;
	}

	public MoneyData getInterestRebateOwed() {
		return this.interestRebateOwed;
	}

	public void setInterestRebateOwed(final MoneyData interestRebateOwed) {
		this.interestRebateOwed = interestRebateOwed;
	}

	public MoneyData getTotalOutstandingBasedOnExpectedMaturityDate() {
		return this.totalOutstandingBasedOnExpectedMaturityDate;
	}

	public void setTotalOutstandingBasedOnExpectedMaturityDate(
			final MoneyData totalOutstandingBasedOnExpectedMaturityDate) {
		this.totalOutstandingBasedOnExpectedMaturityDate = totalOutstandingBasedOnExpectedMaturityDate;
	}

	public MoneyData getTotalOutstandingBasedOnPayoffDate() {
		return this.totalOutstandingBasedOnPayoffDate;
	}

	public void setTotalOutstandingBasedOnPayoffDate(
			final MoneyData totalOutstandingBasedOnPayoffDate) {
		this.totalOutstandingBasedOnPayoffDate = totalOutstandingBasedOnPayoffDate;
	}
}