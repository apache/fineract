package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CommonLoanProperties {

	private String currencyCode;
	private Integer digitsAfterDecimal;
	
	private BigDecimal principal;
	
	private Integer repaymentEvery;
	private Integer repaymentFrequency;
	private Integer numberOfRepayments;
	private Integer amortizationMethod;
	private BigDecimal inArrearsToleranceAmount;
	
	private BigDecimal interestRatePerPeriod;
	private Integer interestRateFrequencyMethod;
	private Integer interestMethod;

	private boolean flexibleRepaymentSchedule = false;
	private boolean interestRebateAllowed = false;

	public CommonLoanProperties() {
		//
	}

	public CommonLoanProperties(final String currencyCode,
			final Integer digitsAfterDecimal, final Number principal,
			final Number interestRatePerPeriod, Integer interestRateFrequencyMethod, final Integer interestMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, final Number toleranceAmount,
			final boolean flexibleRepaymentSchedule,
			final boolean interestRebateAllowed) {
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		if (principal != null) {
			this.principal = BigDecimal.valueOf(Double.valueOf(principal.doubleValue()));
		}
		
		if (interestRatePerPeriod != null) {
			this.interestRatePerPeriod = BigDecimal.valueOf(Double.valueOf(interestRatePerPeriod.doubleValue()));
		}
		this.interestRateFrequencyMethod = interestRateFrequencyMethod;
		this.interestMethod = interestMethod;
		
		this.repaymentEvery = repaymentEvery;
		this.repaymentFrequency = repaymentFrequency;
		this.numberOfRepayments = numberOfRepayments;
		this.amortizationMethod = amortizationMethod;
		this.inArrearsToleranceAmount = BigDecimal.valueOf(Double.valueOf(toleranceAmount.toString()));
		
		this.flexibleRepaymentSchedule = flexibleRepaymentSchedule;
		this.interestRebateAllowed = interestRebateAllowed;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public Integer getDigitsAfterDecimal() {
		return this.digitsAfterDecimal;
	}

	public void setDigitsAfterDecimal(final Integer digitsAfterDecimal) {
		this.digitsAfterDecimal = digitsAfterDecimal;
	}

	public Integer getRepaymentEvery() {
		return this.repaymentEvery;
	}

	public void setRepaymentEvery(final Integer repaymentEvery) {
		this.repaymentEvery = repaymentEvery;
	}

	public Integer getRepaymentFrequency() {
		return this.repaymentFrequency;
	}

	public void setRepaymentFrequency(final Integer repaymentFrequency) {
		this.repaymentFrequency = repaymentFrequency;
	}

	public boolean isFlexibleRepaymentSchedule() {
		return this.flexibleRepaymentSchedule;
	}

	public void setFlexibleRepaymentSchedule(
			final boolean flexibleRepaymentSchedule) {
		this.flexibleRepaymentSchedule = flexibleRepaymentSchedule;
	}

	public boolean isInterestRebateAllowed() {
		return this.interestRebateAllowed;
	}

	public void setInterestRebateAllowed(final boolean interestRebateAllowed) {
		this.interestRebateAllowed = interestRebateAllowed;
	}

	public BigDecimal getPrincipal() {
		return this.principal;
	}

	public void setPrincipal(final BigDecimal principal) {
		this.principal = principal;
	}

	public Boolean getFlexibleRepaymentSchedule() {
		return this.flexibleRepaymentSchedule;
	}

	public Boolean getInterestRebateAllowed() {
		return this.interestRebateAllowed;
	}

	public Integer getInterestRateFrequencyMethod() {
		return interestRateFrequencyMethod;
	}

	public void setInterestRateFrequencyMethod(Integer interestRateFrequencyMethod) {
		this.interestRateFrequencyMethod = interestRateFrequencyMethod;
	}

	public Integer getInterestMethod() {
		return interestMethod;
	}

	public void setInterestMethod(Integer interestMethod) {
		this.interestMethod = interestMethod;
	}

	public Integer getAmortizationMethod() {
		return amortizationMethod;
	}

	public void setAmortizationMethod(Integer amortizationMethod) {
		this.amortizationMethod = amortizationMethod;
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

	public BigDecimal getInArrearsToleranceAmount() {
		return inArrearsToleranceAmount;
	}

	public void setInArrearsToleranceAmount(BigDecimal inArrearsToleranceAmount) {
		this.inArrearsToleranceAmount = inArrearsToleranceAmount;
	}
}