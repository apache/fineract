package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.LoanSchedule;

/**
 * Immutable command for submitting new loan application.
 */
public class SubmitLoanApplicationCommand {
	
	private final Long clientId;
	private final Long productId;
	private final String externalId;
	
	private final Long fundId;
	private final Long transactionProcessingStrategyId;
	
	private final BigDecimal principal;
	private final BigDecimal inArrearsTolerance;
	
	private final Integer loanTermFrequency;
	private final Integer loanTermFrequencyType;
	
	private final Integer numberOfRepayments;
	private final Integer repaymentEvery;
	
	private final BigDecimal interestRatePerPeriod;
	private final Integer repaymentFrequencyType;
	private final Integer interestRateFrequencyType;
	private final Integer amortizationType;
	private final Integer interestType;
	private final Integer interestCalculationPeriodType;
	
	private final LocalDate expectedDisbursementDate;
	private final LocalDate repaymentsStartingFromDate;
	private final LocalDate interestChargedFromDate;
	private final LocalDate submittedOnDate;
	private final String submittedOnNote;
	
	private LoanSchedule loanSchedule;

	public SubmitLoanApplicationCommand(
			final Long clientId, final Long productId, final String externalId, 
			final Long fundId, final Long transactionProcessingStrategyId,
			final LocalDate submittedOnDate, final String submittedOnNote,
			final LocalDate expectedDisbursementDate,
			final LocalDate repaymentsStartingFromDate,
			final LocalDate interestChargedFromLocalDate,
			final BigDecimal principal,
			final BigDecimal interestRatePerPeriod, 
			final Integer interestRateFrequencyMethod, 
			final Integer interestMethod, 
			final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, 
			final Integer loanTermFrequency, final Integer loanTermFrequencyType,
			final BigDecimal toleranceAmount) {
		this.clientId = clientId;
		this.productId = productId;
		this.externalId = externalId;
		this.fundId = fundId;
		this.transactionProcessingStrategyId = transactionProcessingStrategyId;
		
		this.submittedOnDate = submittedOnDate;
		this.submittedOnNote = submittedOnNote;
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
		this.interestChargedFromDate = interestChargedFromLocalDate;
		
		this.principal = principal;
		this.loanTermFrequency = loanTermFrequency;
		this.loanTermFrequencyType = loanTermFrequencyType;
		this.inArrearsTolerance = toleranceAmount;
		
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.interestRateFrequencyType = interestRateFrequencyMethod;
		this.interestType = interestMethod;
		this.interestCalculationPeriodType = interestCalculationPeriodMethod;
		this.repaymentEvery = repaymentEvery;
		this.repaymentFrequencyType = repaymentFrequency;
		this.numberOfRepayments = numberOfRepayments;
		this.amortizationType = amortizationMethod;
	}
	
	public CalculateLoanScheduleCommand toCalculateLoanScheduleCommand() {
		return new CalculateLoanScheduleCommand(productId,
				principal, interestRatePerPeriod, interestRateFrequencyType, interestType, interestCalculationPeriodType, repaymentEvery, repaymentFrequencyType, numberOfRepayments, amortizationType,
				this.loanTermFrequency, this.loanTermFrequencyType,
				expectedDisbursementDate, repaymentsStartingFromDate, interestChargedFromDate);
	}

	public LoanSchedule getLoanSchedule() {
		return loanSchedule;
	}

	public void setLoanSchedule(LoanSchedule loanSchedule) {
		this.loanSchedule = loanSchedule;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getProductId() {
		return productId;
	}

	public String getExternalId() {
		return externalId;
	}

	public Long getFundId() {
		return fundId;
	}
	
	public Long getTransactionProcessingStrategyId() {
		return transactionProcessingStrategyId;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public BigDecimal getInArrearsTolerance() {
		return inArrearsTolerance;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}
	
	public Integer getLoanTermFrequency() {
		return loanTermFrequency;
	}

	public Integer getLoanTermFrequencyType() {
		return loanTermFrequencyType;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public Integer getRepaymentFrequencyType() {
		return repaymentFrequencyType;
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

	public LocalDate getSubmittedOnDate() {
		return submittedOnDate;
	}

	public String getSubmittedOnNote() {
		return submittedOnNote;
	}
}