package org.mifosng.platform.api.commands;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable command for creating or updating details of a loan product.
 */
public class LoanProductCommand {

	private final Long id;
	private final String name;
	private final String description;
	
	private final Long fundId;
	private final Long transactionProcessingStrategyId;
	
	private final String currencyCode;
	private final Integer digitsAfterDecimal;
	private final BigDecimal principal;
	private final BigDecimal inArrearsTolerance;
	
	private final Integer numberOfRepayments;
	private final Integer repaymentEvery;
	
	private final BigDecimal interestRatePerPeriod;

	private final Integer repaymentFrequencyType;
	private final Integer interestRateFrequencyType;
	private final Integer amortizationType;
	private final Integer interestType;
	private final Integer interestCalculationPeriodType;
	
	private final Set<String> modifiedParameters;
	
	public LoanProductCommand(final Set<String> modifiedParameters, final Long id, final String name, final String description, 
			final Long fundId, final Long transactionProcessingStrategyId,
			final String currencyCode, final Integer digitsAfterDecimal, 
			final BigDecimal principal, final BigDecimal inArrearsTolerance, final Integer numberOfRepayments, 
			final Integer repaymentEvery, final BigDecimal interestRatePerPeriod,
			final Integer repaymentFrequencyType, final Integer interestRateFrequencyType, final Integer amortizationType, final Integer interestType, 
			final Integer interestCalculationPeriodType) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.name = name;
		this.description = description;
		this.fundId = fundId;
		this.transactionProcessingStrategyId = transactionProcessingStrategyId;
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		this.principal = principal;
		this.inArrearsTolerance = inArrearsTolerance;
		this.numberOfRepayments = numberOfRepayments;
		this.repaymentEvery = repaymentEvery;
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.repaymentFrequencyType = repaymentFrequencyType;
		this.interestRateFrequencyType = interestRateFrequencyType;
		this.amortizationType = amortizationType;
		this.interestType = interestType;
		this.interestCalculationPeriodType = interestCalculationPeriodType;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Long getFundId() {
		return fundId;
	}
	
	public Long getTransactionProcessingStrategyId() {
		return transactionProcessingStrategyId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public Integer getDigitsAfterDecimal() {
		return digitsAfterDecimal;
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

	public boolean isNameChanged() {
		return this.modifiedParameters.contains("name");
	}

	public boolean isDescriptionChanged() {
		return this.modifiedParameters.contains("description");
	}

	public boolean isFundChanged() {
		return this.modifiedParameters.contains("fundId");
	}
	
	public boolean isTransactionProcessingStrategyChanged() {
		return this.modifiedParameters.contains("transactionProcessingStrategyId");
	}

	public boolean isCurrencyCodeChanged() {
		return this.modifiedParameters.contains("currencyCode");
	}

	public boolean isDigitsAfterDecimalChanged() {
		return this.modifiedParameters.contains("digitsAfterDecimal");
	}

	public boolean isPrincipalChanged() {
		return this.modifiedParameters.contains("principal");
	}

	public boolean isInArrearsToleranceChanged() {
		return this.modifiedParameters.contains("inArrearsTolerance");
	}
	
	public boolean isNumberOfRepaymentsChanged() {
		return this.modifiedParameters.contains("numberOfRepayments");
	}
	
	public boolean isRepaymentEveryChanged() {
		return this.modifiedParameters.contains("repaymentEvery");
	}

	public boolean isRepaymentFrequencyTypeChanged() {
		return this.modifiedParameters.contains("repaymentFrequencyType");
	}

	public boolean isAmortizationTypeChanged() {
		return this.modifiedParameters.contains("amortizationType");
	}

	public boolean isInterestTypeChanged() {
		return this.modifiedParameters.contains("interestType");
	}
	
	public boolean isInterestRatePerPeriodChanged() {
		return this.modifiedParameters.contains("interestRatePerPeriod");
	}

	public boolean isInterestRateFrequencyTypeChanged() {
		return this.modifiedParameters.contains("interestRateFrequencyType");
	}

	public boolean isInterestCalculationPeriodTypeChanged() {
		return this.modifiedParameters.contains("interestCalculationPeriodType");
	}
}