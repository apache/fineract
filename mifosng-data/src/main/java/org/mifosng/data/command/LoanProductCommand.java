package org.mifosng.data.command;

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
	private Integer digitsAfterDecimal;
	private BigDecimal principal;
	private String principalFormatted;
	private BigDecimal inArrearsToleranceAmount;
	private String inArrearsToleranceAmountFormatted;
	
	private Integer repaymentEvery;
	private Integer repaymentFrequency;
	private Integer numberOfRepayments;
	
	private BigDecimal interestRatePerPeriod;
	private String interestRatePerPeriodFormatted;
	private Integer interestRateFrequencyMethod;
	private Integer amortizationMethod;
	private Integer interestMethod;
	private Integer interestCalculationPeriodMethod;
	
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

	public Integer getDigitsAfterDecimal() {
		return digitsAfterDecimal;
	}

	public void setDigitsAfterDecimal(Integer digitsAfterDecimal) {
		this.digitsAfterDecimal = digitsAfterDecimal;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public BigDecimal getInArrearsToleranceAmount() {
		return inArrearsToleranceAmount;
	}

	public void setInArrearsToleranceAmount(BigDecimal inArrearsToleranceAmount) {
		this.inArrearsToleranceAmount = inArrearsToleranceAmount;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}

	public void setRepaymentEvery(Integer repaymentEvery) {
		this.repaymentEvery = repaymentEvery;
	}

	public Integer getRepaymentFrequency() {
		return repaymentFrequency;
	}

	public void setRepaymentFrequency(Integer repaymentFrequency) {
		this.repaymentFrequency = repaymentFrequency;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.numberOfRepayments = numberOfRepayments;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.interestRatePerPeriod = interestRatePerPeriod;
	}

	public Integer getInterestRateFrequencyMethod() {
		return interestRateFrequencyMethod;
	}

	public void setInterestRateFrequencyMethod(Integer interestRateFrequencyMethod) {
		this.interestRateFrequencyMethod = interestRateFrequencyMethod;
	}

	public Integer getAmortizationMethod() {
		return amortizationMethod;
	}

	public void setAmortizationMethod(Integer amortizationMethod) {
		this.amortizationMethod = amortizationMethod;
	}

	public Integer getInterestMethod() {
		return interestMethod;
	}

	public void setInterestMethod(Integer interestMethod) {
		this.interestMethod = interestMethod;
	}

	public Integer getInterestCalculationPeriodMethod() {
		return interestCalculationPeriodMethod;
	}

	public void setInterestCalculationPeriodMethod(
			Integer interestCalculationPeriodMethod) {
		this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
	}

	public String getPrincipalFormatted() {
		return principalFormatted;
	}

	public void setPrincipalFormatted(String principalFormatted) {
		this.principalFormatted = principalFormatted;
	}

	public String getInterestRatePerPeriodFormatted() {
		return interestRatePerPeriodFormatted;
	}

	public void setInterestRatePerPeriodFormatted(
			String interestRatePerPeriodFormatted) {
		this.interestRatePerPeriodFormatted = interestRatePerPeriodFormatted;
	}

	public String getInArrearsToleranceAmountFormatted() {
		return inArrearsToleranceAmountFormatted;
	}

	public void setInArrearsToleranceAmountFormatted(
			String inArrearsToleranceAmountFormatted) {
		this.inArrearsToleranceAmountFormatted = inArrearsToleranceAmountFormatted;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}