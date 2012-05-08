package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;
import org.mifosng.data.LoanSchedule;

@XmlRootElement
public class SubmitLoanApplicationCommand {

	private Long applicantId;
	private Long productId;
	
	private LocalDate expectedDisbursementDate;
	private LocalDate repaymentsStartingFromDate;
	private LocalDate interestCalculatedFromDate;
	
	private LocalDate submittedOnDate;
	private String submittedOnNote;
	private String externalId;
	
	private CommonLoanProperties commonLoanProperties;
	
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
			final Integer repaymentEvery, final Integer repaymentFrequency, final Integer numberOfRepayments, Integer amortizationMethod, final Number toleranceAmount,
			final boolean flexibleRepaymentSchedule,
			final boolean interestRebateAllowed) {
		this.applicantId = applicantId;
		this.productId = productId;
		this.submittedOnDate = submittedOnDate;
		this.submittedOnNote = submittedOnNote;
		
		commonLoanProperties = new CommonLoanProperties(currencyCode, digitsAfterDecimal, principal, interestRatePerPeriod, interestRateFrequencyMethod, 
				interestMethod, interestCalculationPeriodMethod,
				repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, toleranceAmount, flexibleRepaymentSchedule, interestRebateAllowed);
		
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
		this.interestCalculatedFromDate = interestCalculatedFromDate;
		this.loanSchedule = loanSchedule;
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

	public CommonLoanProperties getCommonLoanProperties() {
		return commonLoanProperties;
	}

	public void setCommonLoanProperties(CommonLoanProperties commonLoanProperties) {
		this.commonLoanProperties = commonLoanProperties;
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

//	public Boolean getFlexibleRepaymentSchedule() {
//		return this.commonLoanProperties.getFlexibleRepaymentSchedule();
//	}
//
//	public Boolean getInterestRebateAllowed() {
//		return this.commonLoanProperties.getInterestRebateAllowed();
//	}

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
	
	public BigDecimal getInArrearsToleranceAmount() {
		return this.commonLoanProperties.getInArrearsToleranceAmount();
	}

	public void setInArrearsToleranceAmount(BigDecimal inArrearsToleranceAmount) {
		this.commonLoanProperties.setInArrearsToleranceAmount(inArrearsToleranceAmount);
	}

	public LocalDate getInterestCalculatedFromDate() {
		return interestCalculatedFromDate;
	}

	public void setInterestCalculatedFromDate(LocalDate interestCalculatedFromDate) {
		this.interestCalculatedFromDate = interestCalculatedFromDate;
	}
	
	public Integer getInterestCalculationPeriodMethod() {
		return this.commonLoanProperties.getInterestCalculationPeriodMethod();
	}

	public void setInterestCalculationPeriodMethod(final Integer interestCalculationPeriodMethod) {
		this.commonLoanProperties.setInterestCalculationPeriodMethod(interestCalculationPeriodMethod);
	}
}