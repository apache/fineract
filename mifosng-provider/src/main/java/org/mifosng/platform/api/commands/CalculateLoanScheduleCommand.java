package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable command used when auto-calculating loan schedules based on loan terms.
 */
public class CalculateLoanScheduleCommand {

	private final Long productId;
	private final BigDecimal principal;
	
	private final Integer loanTermFrequency;
	private final Integer loanTermFrequencyType;
	
	private final Integer repaymentEvery;
	private final Integer repaymentFrequencyType;
	private final Integer numberOfRepayments;
	
	private final BigDecimal interestRatePerPeriod;
	private final Integer interestRateFrequencyType;
	private final Integer amortizationType;
	private final Integer interestType;
	private final Integer interestCalculationPeriodType;
	
	private final LocalDate expectedDisbursementDate;
	private final LocalDate repaymentsStartingFromDate;
	private final LocalDate interestChargedFromDate;
	private final LoanChargeCommand[] charges;

	public CalculateLoanScheduleCommand(
			final Long productId,
			final BigDecimal principal,
			final BigDecimal interestRatePerPeriod, Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod,
			final Integer loanTermFrequency, final Integer loanTermFrequencyType,
			final LocalDate expectedDisbursementDate,
			final LocalDate repaymentsStartingFromDate, final LocalDate interestCalculatedFromDate, final LoanChargeCommand[] charges) {
		
		this.productId = productId;
		this.principal = principal;
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.interestRateFrequencyType = interestRateFrequencyMethod;
		this.interestType = interestMethod;
		this.interestCalculationPeriodType = interestCalculationPeriodMethod;
		this.repaymentEvery = repaymentEvery;
		this.repaymentFrequencyType = repaymentFrequency;
		this.numberOfRepayments = numberOfRepayments;
		this.amortizationType = amortizationMethod;
		this.loanTermFrequency = loanTermFrequency;
		this.loanTermFrequencyType = loanTermFrequencyType;
		
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
		this.interestChargedFromDate= interestCalculatedFromDate;
		this.charges = charges;
	}

	public Long getProductId() {
		return productId;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}
	
	public Integer getLoanTermFrequency() {
		return loanTermFrequency;
	}

	public Integer getLoanTermFrequencyType() {
		return loanTermFrequencyType;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}

	public Integer getRepaymentFrequencyType() {
		return repaymentFrequencyType;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public Integer getInterestRateFrequencyType() {
		return interestRateFrequencyType;
	}

	public Integer getAmortizationType() {
		return amortizationType;
	}

	public Integer getInterestType() {
		return interestType;
	}

	public Integer getInterestCalculationPeriodType() {
		return interestCalculationPeriodType;
	}

	public LocalDate getExpectedDisbursementDate() {
		return expectedDisbursementDate;
	}

	public LocalDate getRepaymentsStartingFromDate() {
		return repaymentsStartingFromDate;
	}

	public LocalDate getInterestChargedFromDate() {
		return interestChargedFromDate;
	}

	public LoanChargeCommand[] getCharges() {
		return charges;
	}
}