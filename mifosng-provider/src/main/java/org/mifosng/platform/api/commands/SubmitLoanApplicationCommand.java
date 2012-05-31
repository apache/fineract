package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.LoanSchedule;

@XmlRootElement
public class SubmitLoanApplicationCommand {
	
	private Long clientId;
	private Long productId;
	
	// product 
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
	// end of product related fields
	
	private String expectedDisbursementDate;
	private String repaymentsStartingFromDate;
	private String interestChargedFromDate;
	private LocalDate expectedDisbursementLocalDate;
	private LocalDate repaymentsStartingFromLocalDate;
	private LocalDate interestChargedFromLocalDate;
	
	private String dateFormat;
	private String submittedOnDate;
	private LocalDate submittedOnLocalDate;
	private String submittedOnNote;
	private String externalId;
	
	private LoanSchedule loanSchedule;

	protected SubmitLoanApplicationCommand() {
		//
	}

	public SubmitLoanApplicationCommand(final Long clientId,
			final Long productId, final LocalDate submittedOnDate,
			final String submittedOnNote,
			final LocalDate expectedDisbursementDate,
			final LocalDate repaymentsStartingFromDate,
			final LocalDate interestChargedFromLocalDate,
			final LoanSchedule loanSchedule, final String currencyCode,
			final Integer digitsAfterDecimal, final Number principal,
			final Number interestRatePerPeriod, Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, final Number toleranceAmount) {
		this.clientId = clientId;
		this.productId = productId;
		this.submittedOnLocalDate = submittedOnDate;
		this.submittedOnNote = submittedOnNote;
		this.expectedDisbursementLocalDate = expectedDisbursementDate;
		this.repaymentsStartingFromLocalDate = repaymentsStartingFromDate;
		this.interestChargedFromLocalDate = interestChargedFromLocalDate;
		this.loanSchedule = loanSchedule;
		this.inArrearsToleranceValue = BigDecimal.valueOf(toleranceAmount.doubleValue());
		
		this.currencyCode = currencyCode;
		this.digitsAfterDecimalValue = digitsAfterDecimal;
		this.principalValue = BigDecimal.valueOf(principal.doubleValue());
		this.interestRatePerPeriodValue = BigDecimal.valueOf(interestRatePerPeriod.doubleValue());
		this.interestRateFrequencyType = interestRateFrequencyMethod;
		this.interestType = interestMethod;
		this.interestCalculationPeriodType = interestCalculationPeriodMethod;
		this.repaymentEveryValue = repaymentEvery;
		this.repaymentFrequencyType = repaymentFrequency;
		this.numberOfRepaymentsValue = numberOfRepayments;
		this.amortizationType = amortizationMethod;
	}
	
	public CalculateLoanScheduleCommand toCalculateLoanScheduleCommand() {
		return new CalculateLoanScheduleCommand(currencyCode, digitsAfterDecimalValue, principalValue, interestRatePerPeriodValue, interestRateFrequencyType, interestType, interestCalculationPeriodType, repaymentEveryValue, repaymentFrequencyType, numberOfRepaymentsValue, amortizationType, 
				expectedDisbursementLocalDate, repaymentsStartingFromLocalDate, interestChargedFromLocalDate);
	}

	public Long getProductId() {
		return this.productId;
	}

	public void setProductId(final Long productId) {
		this.productId = productId;
	}

	public String getSubmittedOnNote() {
		return this.submittedOnNote;
	}

	public void setSubmittedOnNote(final String submittedOnNote) {
		this.submittedOnNote = submittedOnNote;
	}

	public LoanSchedule getLoanSchedule() {
		return this.loanSchedule;
	}

	public void setLoanSchedule(final LoanSchedule loanSchedule) {
		this.loanSchedule = loanSchedule;
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

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
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

	public String getExpectedDisbursementDate() {
		return expectedDisbursementDate;
	}

	public void setExpectedDisbursementDate(String expectedDisbursementDate) {
		this.expectedDisbursementDate = expectedDisbursementDate;
	}

	public String getRepaymentsStartingFromDate() {
		return repaymentsStartingFromDate;
	}

	public void setRepaymentsStartingFromDate(String repaymentsStartingFromDate) {
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
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

	public String getSubmittedOnDate() {
		return submittedOnDate;
	}

	public void setSubmittedOnDate(String submittedOnDate) {
		this.submittedOnDate = submittedOnDate;
	}

	public LocalDate getSubmittedOnLocalDate() {
		return submittedOnLocalDate;
	}

	public void setSubmittedOnLocalDate(LocalDate submittedOnLocalDate) {
		this.submittedOnLocalDate = submittedOnLocalDate;
	}

	public String getInterestChargedFromDate() {
		return interestChargedFromDate;
	}

	public void setInterestChargedFromDate(String interestChargedFromDate) {
		this.interestChargedFromDate = interestChargedFromDate;
	}
}