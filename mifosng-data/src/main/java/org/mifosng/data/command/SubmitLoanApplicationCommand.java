package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;
import org.mifosng.data.LoanSchedule;

@XmlRootElement
public class SubmitLoanApplicationCommand {
	
	private Long applicantId;
	private Long productId;
	
	// product 
	private String currencyCode;
	private Integer digitsAfterDecimal;
	private String principalFormatted;
	private BigDecimal principal;
	private String inArrearsToleranceAmountFormatted;
	private BigDecimal inArrearsToleranceAmount;
	
	private Integer repaymentEvery;
	private Integer repaymentFrequency;
	private Integer numberOfRepayments;
	
	private String interestRatePerPeriodFormatted;
	private BigDecimal interestRatePerPeriod;
	private Integer interestRateFrequencyMethod;
	private Integer amortizationMethod;
	private Integer interestMethod;
	private Integer interestCalculationPeriodMethod;
	private boolean flexibleRepaymentSchedule = false;
	private boolean interestRebateAllowed = false;
	//
	
	private String expectedDisbursementDateFormatted;
	private String repaymentsStartingFromDateFormatted;
	private String interestCalculatedFromDateFormatted;
	private LocalDate expectedDisbursementDate;
	private LocalDate repaymentsStartingFromDate;
	private LocalDate interestCalculatedFromDate;
	
	private String dateFormat;
	private String submittedOnDateFormatted;
	private LocalDate submittedOnDate;
	private String submittedOnNote;
	private String externalId;
	
	private LoanSchedule loanSchedule;

	protected SubmitLoanApplicationCommand() {
		//
	}

	public SubmitLoanApplicationCommand(final Long applicantId,
			final Long productId, final LocalDate submittedOnDate,
			final String submittedOnNote,
			final LocalDate expectedDisbursementDate,
			final LocalDate repaymentsStartingFromDate,
			final LocalDate interestCalculatedFromDate,
			final LoanSchedule loanSchedule, final String currencyCode,
			final Integer digitsAfterDecimal, final Number principal,
			final Number interestRatePerPeriod, Integer interestRateFrequencyMethod, final Integer interestMethod, final Integer interestCalculationPeriodMethod,
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, final Number toleranceAmount) {
		this.applicantId = applicantId;
		this.productId = productId;
		this.submittedOnDate = submittedOnDate;
		this.submittedOnNote = submittedOnNote;
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
		this.interestCalculatedFromDate = interestCalculatedFromDate;
		this.loanSchedule = loanSchedule;
		this.inArrearsToleranceAmount = BigDecimal.valueOf(toleranceAmount.doubleValue());
		
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
		this.flexibleRepaymentSchedule = false;
		this.interestRebateAllowed = false;
	}
	
	public CalculateLoanScheduleCommand toCalculateLoanScheduleCommand() {
		return new CalculateLoanScheduleCommand(currencyCode, digitsAfterDecimal, principal, interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestCalculationPeriodMethod, repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, expectedDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);
	}

	public Long getApplicantId() {
		return this.applicantId;
	}

	public void setApplicantId(final Long applicantId) {
		this.applicantId = applicantId;
	}

	public Long getProductId() {
		return this.productId;
	}

	public void setProductId(final Long productId) {
		this.productId = productId;
	}

	public LocalDate getSubmittedOnDate() {
		return this.submittedOnDate;
	}

	public void setSubmittedOnDate(final LocalDate submittedOnDate) {
		this.submittedOnDate = submittedOnDate;
	}

	public String getSubmittedOnNote() {
		return this.submittedOnNote;
	}

	public void setSubmittedOnNote(final String submittedOnNote) {
		this.submittedOnNote = submittedOnNote;
	}

	public LocalDate getExpectedDisbursementDate() {
		return this.expectedDisbursementDate;
	}

	public void setExpectedDisbursementDate(
			final LocalDate expectedDisbursementDate) {
		this.expectedDisbursementDate = expectedDisbursementDate;
	}

	public LocalDate getRepaymentsStartingFromDate() {
		return this.repaymentsStartingFromDate;
	}

	public void setRepaymentsStartingFromDate(
			final LocalDate repaymentsStartingFromDate) {
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
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

	public boolean isFlexibleRepaymentSchedule() {
		return flexibleRepaymentSchedule;
	}

	public void setFlexibleRepaymentSchedule(boolean flexibleRepaymentSchedule) {
		this.flexibleRepaymentSchedule = flexibleRepaymentSchedule;
	}

	public boolean isInterestRebateAllowed() {
		return interestRebateAllowed;
	}

	public void setInterestRebateAllowed(boolean interestRebateAllowed) {
		this.interestRebateAllowed = interestRebateAllowed;
	}

	public LocalDate getInterestCalculatedFromDate() {
		return interestCalculatedFromDate;
	}

	public void setInterestCalculatedFromDate(LocalDate interestCalculatedFromDate) {
		this.interestCalculatedFromDate = interestCalculatedFromDate;
	}

	public String getExpectedDisbursementDateFormatted() {
		return expectedDisbursementDateFormatted;
	}

	public void setExpectedDisbursementDateFormatted(
			String expectedDisbursementDateFormatted) {
		this.expectedDisbursementDateFormatted = expectedDisbursementDateFormatted;
	}

	public String getRepaymentsStartingFromDateFormatted() {
		return repaymentsStartingFromDateFormatted;
	}

	public void setRepaymentsStartingFromDateFormatted(
			String repaymentsStartingFromDateFormatted) {
		this.repaymentsStartingFromDateFormatted = repaymentsStartingFromDateFormatted;
	}

	public String getInterestCalculatedFromDateFormatted() {
		return interestCalculatedFromDateFormatted;
	}

	public void setInterestCalculatedFromDateFormatted(
			String interestCalculatedFromDateFormatted) {
		this.interestCalculatedFromDateFormatted = interestCalculatedFromDateFormatted;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getSubmittedOnDateFormatted() {
		return submittedOnDateFormatted;
	}

	public void setSubmittedOnDateFormatted(String submittedOnDateFormatted) {
		this.submittedOnDateFormatted = submittedOnDateFormatted;
	}

	public String getPrincipalFormatted() {
		return principalFormatted;
	}

	public void setPrincipalFormatted(String principalFormatted) {
		this.principalFormatted = principalFormatted;
	}

	public String getInArrearsToleranceAmountFormatted() {
		return inArrearsToleranceAmountFormatted;
	}

	public void setInArrearsToleranceAmountFormatted(
			String inArrearsToleranceAmountFormatted) {
		this.inArrearsToleranceAmountFormatted = inArrearsToleranceAmountFormatted;
	}

	public String getInterestRatePerPeriodFormatted() {
		return interestRatePerPeriodFormatted;
	}

	public void setInterestRatePerPeriodFormatted(
			String interestRatePerPeriodFormatted) {
		this.interestRatePerPeriodFormatted = interestRatePerPeriodFormatted;
	}
}