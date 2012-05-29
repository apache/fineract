package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanProductCommand {

	private Long id;
	private String name;
	private String description;
	private String externalId;
	
	private String locale;
	private String currencyCode;
	private String digitsAfterDecimal;
	private Integer digitsAfterDecimalValue;
	private BigDecimal principalValue;
	private String principal;
	private BigDecimal inArrearsToleranceValue;
	private String inArrearsTolerance;
	
	private String numberOfRepayments;
	private Integer numberOfRepaymentsValue;
	private String repaymentEvery;
	private Integer repaymentEveryValue;
	
	private String interestRatePerPeriod;
	private BigDecimal interestRatePerPeriodValue;

	private Integer repaymentFrequencyType;
	private Integer interestRateFrequencyType;
	private Integer amortizationType;
	private Integer interestType;
	private Integer interestCalculationPeriodType;
	
	protected LoanProductCommand() {
		//
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getDigitsAfterDecimal() {
		return digitsAfterDecimal;
	}

	public void setDigitsAfterDecimal(String digitsAfterDecimal) {
		this.digitsAfterDecimal = digitsAfterDecimal;
	}

	public Integer getDigitsAfterDecimalValue() {
		return digitsAfterDecimalValue;
	}

	public void setDigitsAfterDecimalValue(Integer digitsAfterDecimalValue) {
		this.digitsAfterDecimalValue = digitsAfterDecimalValue;
	}

	public BigDecimal getPrincipalValue() {
		return principalValue;
	}

	public void setPrincipalValue(BigDecimal principalValue) {
		this.principalValue = principalValue;
	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public BigDecimal getInArrearsToleranceValue() {
		return inArrearsToleranceValue;
	}

	public void setInArrearsToleranceValue(BigDecimal inArrearsToleranceValue) {
		this.inArrearsToleranceValue = inArrearsToleranceValue;
	}

	public String getInArrearsTolerance() {
		return inArrearsTolerance;
	}

	public void setInArrearsTolerance(String inArrearsTolerance) {
		this.inArrearsTolerance = inArrearsTolerance;
	}

	public String getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public void setNumberOfRepayments(String numberOfRepayments) {
		this.numberOfRepayments = numberOfRepayments;
	}

	public Integer getNumberOfRepaymentsValue() {
		return numberOfRepaymentsValue;
	}

	public void setNumberOfRepaymentsValue(Integer numberOfRepaymentsValue) {
		this.numberOfRepaymentsValue = numberOfRepaymentsValue;
	}

	public String getRepaymentEvery() {
		return repaymentEvery;
	}

	public void setRepaymentEvery(String repaymentEvery) {
		this.repaymentEvery = repaymentEvery;
	}

	public Integer getRepaymentEveryValue() {
		return repaymentEveryValue;
	}

	public void setRepaymentEveryValue(Integer repaymentEveryValue) {
		this.repaymentEveryValue = repaymentEveryValue;
	}

	public String getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public void setInterestRatePerPeriod(String interestRatePerPeriod) {
		this.interestRatePerPeriod = interestRatePerPeriod;
	}

	public BigDecimal getInterestRatePerPeriodValue() {
		return interestRatePerPeriodValue;
	}

	public void setInterestRatePerPeriodValue(BigDecimal interestRatePerPeriodValue) {
		this.interestRatePerPeriodValue = interestRatePerPeriodValue;
	}

	public Integer getRepaymentFrequencyType() {
		return repaymentFrequencyType;
	}

	public void setRepaymentFrequencyType(Integer repaymentFrequencyType) {
		this.repaymentFrequencyType = repaymentFrequencyType;
	}

	public Integer getInterestRateFrequencyType() {
		return interestRateFrequencyType;
	}

	public void setInterestRateFrequencyType(Integer interestRateFrequencyType) {
		this.interestRateFrequencyType = interestRateFrequencyType;
	}

	public Integer getAmortizationType() {
		return amortizationType;
	}

	public void setAmortizationType(Integer amortizationType) {
		this.amortizationType = amortizationType;
	}

	public Integer getInterestType() {
		return interestType;
	}

	public void setInterestType(Integer interestType) {
		this.interestType = interestType;
	}

	public Integer getInterestCalculationPeriodType() {
		return interestCalculationPeriodType;
	}

	public void setInterestCalculationPeriodType(
			Integer interestCalculationPeriodType) {
		this.interestCalculationPeriodType = interestCalculationPeriodType;
	}
}