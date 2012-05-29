package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class CalculateLoanScheduleCommand {

	private String currencyCode;
	private Integer digitsAfterDecimalValue;
	private BigDecimal principalValue;
	private BigDecimal inArrearsToleranceValue;
	
	private Integer repaymentEveryValue;
	private Integer repaymentFrequencyType;
	private Integer numberOfRepaymentsValue;
	
	private BigDecimal interestRatePerPeriodValue;
	private Integer interestRateFrequencyType;
	private Integer amortizationType;
	private Integer interestType;
	private Integer interestCalculationPeriodType;
	
	private LocalDate expectedDisbursementLocalDate;
	private LocalDate repaymentsStartingFromLocalDate;
	private LocalDate interestChargedFromLocalDate;

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
		this.digitsAfterDecimalValue = digitsAfterDecimal;
		if (principal != null) {
			this.principalValue = BigDecimal.valueOf(principal.doubleValue());
		}
		if (interestRatePerPeriod != null) {
			this.interestRatePerPeriodValue = BigDecimal.valueOf(interestRatePerPeriod.doubleValue());
		}
		this.interestRateFrequencyType = interestRateFrequencyMethod;
		this.interestType = interestMethod;
		this.interestCalculationPeriodType = interestCalculationPeriodMethod;
		this.repaymentEveryValue = repaymentEvery;
		this.repaymentFrequencyType = repaymentFrequency;
		this.numberOfRepaymentsValue = numberOfRepayments;
		this.amortizationType = amortizationMethod;
		
		this.expectedDisbursementLocalDate = expectedDisbursementDate;
		this.repaymentsStartingFromLocalDate = repaymentsStartingFromDate;
		this.interestChargedFromLocalDate= interestCalculatedFromDate;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
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

	public BigDecimal getInArrearsToleranceValue() {
		return inArrearsToleranceValue;
	}

	public void setInArrearsToleranceValue(BigDecimal inArrearsToleranceValue) {
		this.inArrearsToleranceValue = inArrearsToleranceValue;
	}

	public Integer getRepaymentEveryValue() {
		return repaymentEveryValue;
	}

	public void setRepaymentEveryValue(Integer repaymentEveryValue) {
		this.repaymentEveryValue = repaymentEveryValue;
	}

	public Integer getRepaymentFrequencyType() {
		return repaymentFrequencyType;
	}

	public void setRepaymentFrequencyType(Integer repaymentFrequencyType) {
		this.repaymentFrequencyType = repaymentFrequencyType;
	}

	public Integer getNumberOfRepaymentsValue() {
		return numberOfRepaymentsValue;
	}

	public void setNumberOfRepaymentsValue(Integer numberOfRepaymentsValue) {
		this.numberOfRepaymentsValue = numberOfRepaymentsValue;
	}

	public BigDecimal getInterestRatePerPeriodValue() {
		return interestRatePerPeriodValue;
	}

	public void setInterestRatePerPeriodValue(BigDecimal interestRatePerPeriodValue) {
		this.interestRatePerPeriodValue = interestRatePerPeriodValue;
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

	public LocalDate getExpectedDisbursementLocalDate() {
		return expectedDisbursementLocalDate;
	}

	public void setExpectedDisbursementLocalDate(
			LocalDate expectedDisbursementLocalDate) {
		this.expectedDisbursementLocalDate = expectedDisbursementLocalDate;
	}

	public LocalDate getRepaymentsStartingFromLocalDate() {
		return repaymentsStartingFromLocalDate;
	}

	public void setRepaymentsStartingFromLocalDate(
			LocalDate repaymentsStartingFromLocalDate) {
		this.repaymentsStartingFromLocalDate = repaymentsStartingFromLocalDate;
	}

	public LocalDate getInterestChargedFromLocalDate() {
		return interestChargedFromLocalDate;
	}

	public void setInterestChargedFromLocalDate(
			LocalDate interestChargedFromLocalDate) {
		this.interestChargedFromLocalDate = interestChargedFromLocalDate;
	}
}