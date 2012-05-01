package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UpdateLoanProductCommand implements LoanProductCommandData {

	private Long id;
	private String name;
	private String description;
	private String externalId;
	
	private CommonLoanProperties commonLoanProperties;
	
	public UpdateLoanProductCommand() {
		this.commonLoanProperties = new CommonLoanProperties();
	}

	public UpdateLoanProductCommand(final String currencyCode,
			final Integer digitsAfterDecimal, final Number principal,
			final String name, final String description,
			final Number interestRatePerPeriod, Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, final Number toleranceAmount,
			final boolean flexibleRepaymentSchedule,
			final boolean interestRebateAllowed) {
		this.name = name;
		this.description = description;
		
		commonLoanProperties = new CommonLoanProperties(currencyCode, digitsAfterDecimal, principal, interestRatePerPeriod, interestRateFrequencyMethod, 
				interestMethod, interestCalculationPeriodMethod,
				repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, toleranceAmount, flexibleRepaymentSchedule, interestRebateAllowed);
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getCurrencyCode() {
		return this.commonLoanProperties.getCurrencyCode();
	}

	public void setCurrencyCode(final String currencyCode) {
		this.commonLoanProperties.setCurrencyCode(currencyCode);
	}

	public Integer getDigitsAfterDecimal() {
		return this.commonLoanProperties.getDigitsAfterDecimal();
	}

	public void setDigitsAfterDecimal(final Integer digitsAfterDecimal) {
		this.commonLoanProperties.setDigitsAfterDecimal(digitsAfterDecimal);
	}

	public Integer getRepaymentEvery() {
		return this.commonLoanProperties.getRepaymentEvery();
	}

	public void setRepaymentEvery(final Integer repaymentEvery) {
		this.commonLoanProperties.setRepaymentEvery(repaymentEvery);
	}

	public Integer getRepaymentFrequency() {
		return this.commonLoanProperties.getRepaymentFrequency();
	}

	public void setRepaymentFrequency(final Integer repaymentFrequency) {
		this.commonLoanProperties.setRepaymentFrequency(repaymentFrequency);
	}

	public Boolean isFlexibleRepaymentSchedule() {
		return this.commonLoanProperties.isFlexibleRepaymentSchedule();
	}

	public void setFlexibleRepaymentSchedule(
			final Boolean flexibleRepaymentSchedule) {
		this.commonLoanProperties.setFlexibleRepaymentSchedule(flexibleRepaymentSchedule);
	}

	public Boolean isInterestRebateAllowed() {
		return this.commonLoanProperties.isInterestRebateAllowed();
	}

	public void setInterestRebateAllowed(final Boolean interestRebateAllowed) {
		this.commonLoanProperties.setInterestRebateAllowed(interestRebateAllowed);
	}

	public BigDecimal getPrincipal() {
		return this.commonLoanProperties.getPrincipal();
	}

	public void setPrincipal(final BigDecimal principal) {
		this.commonLoanProperties.setPrincipal(principal);
	}

	public Boolean getFlexibleRepaymentSchedule() {
		return this.commonLoanProperties.getFlexibleRepaymentSchedule();
	}

	public Boolean getInterestRebateAllowed() {
		return this.commonLoanProperties.getInterestRebateAllowed();
	}

	public Integer getInterestRateFrequencyMethod() {
		return commonLoanProperties.getInterestRateFrequencyMethod();
	}

	public void setInterestRateFrequencyMethod(Integer interestRateFrequencyMethod) {
		this.commonLoanProperties.setInterestRateFrequencyMethod(interestRateFrequencyMethod);
	}

	public Integer getInterestMethod() {
		return commonLoanProperties.getInterestMethod();
	}

	public void setInterestMethod(Integer interestMethod) {
		this.commonLoanProperties.setInterestMethod(interestMethod);
	}

	public Integer getAmortizationMethod() {
		return commonLoanProperties.getAmortizationMethod();
	}

	public void setAmortizationMethod(Integer amortizationMethod) {
		this.commonLoanProperties.setAmortizationMethod(amortizationMethod);
	}

	public Integer getNumberOfRepayments() {
		return commonLoanProperties.getNumberOfRepayments();
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.commonLoanProperties.setNumberOfRepayments(numberOfRepayments);
	}

	public BigDecimal getInterestRatePerPeriod() {
		return commonLoanProperties.getInterestRatePerPeriod();
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.commonLoanProperties.setInterestRatePerPeriod(interestRatePerPeriod);
	}

	public CommonLoanProperties getCommonLoanProperties() {
		return commonLoanProperties;
	}

	public void setCommonLoanProperties(CommonLoanProperties commonLoanProperties) {
		this.commonLoanProperties = commonLoanProperties;
	}
	
	public BigDecimal getInArrearsToleranceAmount() {
		return this.commonLoanProperties.getInArrearsToleranceAmount();
	}

	public void setInArrearsToleranceAmount(BigDecimal inArrearsToleranceAmount) {
		this.commonLoanProperties.setInArrearsToleranceAmount(inArrearsToleranceAmount);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public Integer getInterestCalculationPeriodMethod() {
		return this.commonLoanProperties.getInterestCalculationPeriodMethod();
	}

	public void setInterestCalculationPeriodMethod(final Integer interestCalculationPeriodMethod) {
		this.commonLoanProperties.setInterestCalculationPeriodMethod(interestCalculationPeriodMethod);
	}
}