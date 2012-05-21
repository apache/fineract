package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class CalculateLoanScheduleCommand {

	private String currencyCode;
	private Integer digitsAfterDecimal;
	private BigDecimal principal;
	private BigDecimal inArrearsToleranceAmount;
	
	private Integer repaymentEvery;
	private Integer repaymentFrequency;
	private Integer numberOfRepayments;
	
	private BigDecimal interestRatePerPeriod;
	private Integer interestRateFrequencyMethod;
	private Integer amortizationMethod;
	private Integer interestMethod;
	private Integer interestCalculationPeriodMethod;
	
	private LocalDate expectedDisbursementDate;
	private LocalDate repaymentsStartingFromDate;
	private LocalDate interestCalculatedFromDate;

	protected CalculateLoanScheduleCommand() {
		//
	}

	public CalculateLoanScheduleCommand(final String currencyCode,
			final Integer digitsAfterDecimal, final Number principal,
			final Number interestRatePerPeriod, Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod,
			final LocalDate expectedDisbursementDate,
			final LocalDate repaymentsStartingFromDate, final LocalDate interestCalculatedFromDate) {
		
		this.currencyCode = currencyCode;
		this.digitsAfterDecimal = digitsAfterDecimal;
		this.principal = BigDecimal.valueOf(principal.doubleValue());
		this.interestRatePerPeriod = BigDecimal.valueOf(interestRatePerPeriod.doubleValue());
		this.interestRateFrequencyMethod = interestRateFrequencyMethod;
		this.interestMethod = interestMethod;
		this.interestCalculationPeriodMethod = interestCalculationPeriodMethod;
		this.repaymentEvery = repaymentEvery;
		this.repaymentFrequency = repaymentFrequency;
		this.numberOfRepayments = numberOfRepayments;
		this.amortizationMethod = amortizationMethod;
		
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
		this.interestCalculatedFromDate = interestCalculatedFromDate;
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

	public LocalDate getExpectedDisbursementDate() {
		return expectedDisbursementDate;
	}

	public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) {
		this.expectedDisbursementDate = expectedDisbursementDate;
	}

	public LocalDate getRepaymentsStartingFromDate() {
		return repaymentsStartingFromDate;
	}

	public void setRepaymentsStartingFromDate(LocalDate repaymentsStartingFromDate) {
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
	}

	public LocalDate getInterestCalculatedFromDate() {
		return interestCalculatedFromDate;
	}

	public void setInterestCalculatedFromDate(LocalDate interestCalculatedFromDate) {
		this.interestCalculatedFromDate = interestCalculatedFromDate;
	}
}