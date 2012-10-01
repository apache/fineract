package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command for submitting new loan application.
 */
public class LoanApplicationCommand {
	
	private final Long loanId;
	private final Long clientId;
	private final Long groupId;
	private final Long productId;
	private final String externalId;
	
	private final Long fundId;
	private final Long loanOfficerId;
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

    private final LoanChargeCommand[] charges;

	private final Set<String> modifiedParameters;

	public LoanApplicationCommand(
			final Set<String> modifiedParameters,
			final Long loanId,
			final Long clientId, final Long groupId, final Long productId, final String externalId,
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
			final BigDecimal toleranceAmount, final LoanChargeCommand[] charges,
			final Long loanOfficerId) {
		this.modifiedParameters = modifiedParameters;
		this.loanId = loanId;
		this.clientId = clientId;
		this.groupId = groupId;
		this.productId = productId;
		this.externalId = externalId;
		this.fundId = fundId;
		this.loanOfficerId = loanOfficerId;
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

        this.charges = charges;
	}
	
	public CalculateLoanScheduleCommand toCalculateLoanScheduleCommand() {
		return new CalculateLoanScheduleCommand(this.productId,
				this.principal, this.interestRatePerPeriod, this.interestRateFrequencyType, 
				this.interestType, this.interestCalculationPeriodType, this.repaymentEvery, 
				this.repaymentFrequencyType, this.numberOfRepayments, this.amortizationType,
				this.loanTermFrequency, this.loanTermFrequencyType,
				this.expectedDisbursementDate, this.repaymentsStartingFromDate, this.interestChargedFromDate, this.charges);
	}
	
	public Long getLoanId() {
		return loanId;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getGroupId() {
		return groupId;
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

    public LoanChargeCommand[] getCharges() {
        return charges;
    }

	public LoanProductCommand toLoanProductCommand() {
		
		// FIXME - KW - CHARGE - FIX UP FOR CHARGES - take look at loan product charges also and align usage with other loan areas.
		return new LoanProductCommand(modifiedParameters, this.productId, null, null, 
				this.fundId, this.transactionProcessingStrategyId, 
				null, null, 
				this.principal, this.inArrearsTolerance, 
				this.numberOfRepayments, this.repaymentEvery, this.interestRatePerPeriod, 
				this.repaymentFrequencyType, this.interestRateFrequencyType, 
				this.amortizationType, this.interestType, 
				this.interestCalculationPeriodType,
				null);
	}

	public boolean isClientChanged() {
		return this.modifiedParameters.contains("clientId");
	}

	public boolean isProductChanged() {
		return this.modifiedParameters.contains("productId");
	}

	public boolean isFundChanged() {
		return this.modifiedParameters.contains("fundId");
	}
	
	public boolean isLoanOfficerChanged() {
		return this.modifiedParameters.contains("loanOfficerId");
	}

	public boolean isTransactionStrategyChanged() {
		return this.modifiedParameters.contains("transactionProcessingStrategyId");
	}

	public boolean isTermFrequencyChanged() {
		return this.modifiedParameters.contains("loanTermFrequency");
	}

	public boolean isTermFrequencyTypeChanged() {
		return this.modifiedParameters.contains("loanTermFrequencyType");
	}

	public boolean isSubmittedOnDateChanged() {
		return this.modifiedParameters.contains("submittedOnDate");
	}

	public boolean isExpectedDisbursementDateChanged() {
		return this.modifiedParameters.contains("expectedDisbursementDate");
	}

	public boolean isRepaymentsStartingFromDateChanged() {
		return this.modifiedParameters.contains("repaymentsStartingFromDate");
	}

	public boolean isInterestChargedFromDateChanged() {
		return this.modifiedParameters.contains("interestChargedFromDate");
	}

	public boolean isChargesChanged() {
		return this.modifiedParameters.contains("charges");
	}

	public Long getLoanOfficerId() {
		return loanOfficerId;
	}
}