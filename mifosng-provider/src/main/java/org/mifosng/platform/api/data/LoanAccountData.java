package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.LocalDate;

@JsonFilter("loanFilter")
public class LoanAccountData {

	private Long id;
	private String externalId;
	private Long fundId;
	private String fundName;
	private Long loanProductId;
	private String loanProductName;

	private LocalDate submittedOnDate;
	private LocalDate approvedOnDate;
	private LocalDate expectedDisbursementDate;
	private LocalDate actualDisbursementDate;
	private LocalDate expectedFirstRepaymentOnDate;
	private LocalDate interestChargedFromDate;
	private LocalDate closedOnDate;
	private LocalDate expectedMaturityDate;

	private MoneyData principal;
	private MoneyData inArrearsTolerance;

	private Integer numberOfRepayments;
	private Integer repaymentEvery;
	private BigDecimal interestRatePerPeriod;
	private BigDecimal annualInterestRate;

	private EnumOptionData repaymentFrequencyType;
	private EnumOptionData interestRateFrequencyType;
	private EnumOptionData amortizationType;
	private EnumOptionData interestType;
	private EnumOptionData interestCalculationPeriodType;

	private Integer lifeCycleStatusId;
	private String lifeCycleStatusText;
	private LocalDate lifeCycleStatusDate;

	private LoanAccountSummaryData summary;
	private LoanRepaymentScheduleData repaymentSchedule;
	private List<LoanTransactionData> loanRepayments;

	private LoanPermissionData permissions;

	private LoanConvenienceData convenienceData;

	protected LoanAccountData() {
		//
	}

	public LoanAccountData(LoanBasicDetailsData basicDetails,
			LoanAccountSummaryData summary,
			LoanRepaymentScheduleData repaymentSchedule,
			List<LoanTransactionData> loanRepayments,
			LoanPermissionData permissions) {
		this.summary = summary;
		this.repaymentSchedule = repaymentSchedule;
		this.loanRepayments = loanRepayments;
		this.permissions = permissions;

		int maxSubmittedOnOffsetFromToday = basicDetails
				.getMaxSubmittedOnOffsetFromToday();
		int maxApprovedOnOffsetFromToday = basicDetails
				.getMaxApprovedOnOffsetFromToday();
		int maxDisbursedOnOffsetFromToday = basicDetails
				.getMaxDisbursedOnOffsetFromToday();
		int expectedLoanTermInDays = basicDetails.getLoanTermInDays();
		int expectedLoanTermInMonths = basicDetails.getLoanTermInMonths();
		int actualLoanTermInDays = basicDetails.getActualLoanTermInDays();
		int actualLoanTermInMonths = basicDetails.getActualLoanTermInMonths();

		this.convenienceData = new LoanConvenienceData(
				maxSubmittedOnOffsetFromToday, maxApprovedOnOffsetFromToday,
				maxDisbursedOnOffsetFromToday, expectedLoanTermInDays,
				actualLoanTermInDays, expectedLoanTermInMonths,
				actualLoanTermInMonths);

		this.id = basicDetails.getId();
		this.externalId = basicDetails.getExternalId();
		this.fundId = basicDetails.getFundId();
		this.fundName = basicDetails.getFundName();
		this.loanProductId = basicDetails.getLoanProductId();
		this.loanProductName = basicDetails.getLoanProductName();
		this.submittedOnDate = basicDetails.getSubmittedOnDate();
		this.approvedOnDate = basicDetails.getApprovedOnDate();
		this.expectedDisbursementDate = basicDetails
				.getExpectedDisbursementDate();
		this.actualDisbursementDate = basicDetails.getActualDisbursementDate();
		this.closedOnDate = basicDetails.getClosedOnDate();
		this.expectedMaturityDate = basicDetails.getExpectedMaturityDate();
		this.expectedFirstRepaymentOnDate = basicDetails
				.getExpectedFirstRepaymentOnDate();
		this.interestChargedFromDate = basicDetails
				.getInterestChargedFromDate();
		this.principal = basicDetails.getPrincipal();
		this.inArrearsTolerance = basicDetails.getInArrearsTolerance();
		this.numberOfRepayments = basicDetails.getNumberOfRepayments();
		this.repaymentEvery = basicDetails.getRepaymentEvery();
		this.interestRatePerPeriod = basicDetails.getInterestRatePerPeriod();
		this.annualInterestRate = basicDetails.getAnnualInterestRate();
		this.repaymentFrequencyType = basicDetails.getRepaymentFrequencyType();
		this.interestRateFrequencyType = basicDetails
				.getInterestRateFrequencyType();
		this.amortizationType = basicDetails.getAmortizationType();
		this.interestType = basicDetails.getInterestType();
		this.interestCalculationPeriodType = basicDetails
				.getInterestCalculationPeriodType();
		this.lifeCycleStatusText = basicDetails.getLifeCycleStatusText();
		this.lifeCycleStatusId = basicDetails.getLifeCycleStatusId();
		this.lifeCycleStatusDate = basicDetails.getLifeCycleStatusDate();
	}

	public LoanConvenienceData getConvenienceData() {
		return convenienceData;
	}

	public void setConvenienceData(LoanConvenienceData convenienceData) {
		this.convenienceData = convenienceData;
	}

	public LoanPermissionData getPermissions() {
		return permissions;
	}

	public void setPermissions(LoanPermissionData permissions) {
		this.permissions = permissions;
	}

	public LoanAccountSummaryData getSummary() {
		return summary;
	}

	public void setSummary(LoanAccountSummaryData summary) {
		this.summary = summary;
	}

	public LoanRepaymentScheduleData getRepaymentSchedule() {
		return repaymentSchedule;
	}

	public void setRepaymentSchedule(LoanRepaymentScheduleData repaymentSchedule) {
		this.repaymentSchedule = repaymentSchedule;
	}

	public List<LoanTransactionData> getLoanRepayments() {
		return loanRepayments;
	}

	public void setLoanRepayments(List<LoanTransactionData> loanRepayments) {
		this.loanRepayments = loanRepayments;
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

	public String getLoanProductName() {
		return loanProductName;
	}

	public void setLoanProductName(String loanProductName) {
		this.loanProductName = loanProductName;
	}

	public LocalDate getSubmittedOnDate() {
		return submittedOnDate;
	}

	public void setSubmittedOnDate(LocalDate submittedOnDate) {
		this.submittedOnDate = submittedOnDate;
	}

	public LocalDate getApprovedOnDate() {
		return approvedOnDate;
	}

	public void setApprovedOnDate(LocalDate approvedOnDate) {
		this.approvedOnDate = approvedOnDate;
	}

	public LocalDate getExpectedDisbursementDate() {
		return expectedDisbursementDate;
	}

	public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) {
		this.expectedDisbursementDate = expectedDisbursementDate;
	}

	public LocalDate getActualDisbursementDate() {
		return actualDisbursementDate;
	}

	public void setActualDisbursementDate(LocalDate actualDisbursementDate) {
		this.actualDisbursementDate = actualDisbursementDate;
	}

	public LocalDate getExpectedFirstRepaymentOnDate() {
		return expectedFirstRepaymentOnDate;
	}

	public void setExpectedFirstRepaymentOnDate(
			LocalDate expectedFirstRepaymentOnDate) {
		this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
	}

	public LocalDate getInterestChargedFromDate() {
		return interestChargedFromDate;
	}

	public void setInterestChargedFromDate(LocalDate interestChargedFromDate) {
		this.interestChargedFromDate = interestChargedFromDate;
	}

	public LocalDate getClosedOnDate() {
		return closedOnDate;
	}

	public void setClosedOnDate(LocalDate closedOnDate) {
		this.closedOnDate = closedOnDate;
	}

	public LocalDate getExpectedMaturityDate() {
		return expectedMaturityDate;
	}

	public void setExpectedMaturityDate(LocalDate expectedMaturityDate) {
		this.expectedMaturityDate = expectedMaturityDate;
	}

	public MoneyData getPrincipal() {
		return principal;
	}

	public void setPrincipal(MoneyData principal) {
		this.principal = principal;
	}

	public MoneyData getInArrearsTolerance() {
		return inArrearsTolerance;
	}

	public void setInArrearsTolerance(MoneyData inArrearsTolerance) {
		this.inArrearsTolerance = inArrearsTolerance;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public void setNumberOfRepayments(Integer numberOfRepayments) {
		this.numberOfRepayments = numberOfRepayments;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}

	public void setRepaymentEvery(Integer repaymentEvery) {
		this.repaymentEvery = repaymentEvery;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public void setInterestRatePerPeriod(BigDecimal interestRatePerPeriod) {
		this.interestRatePerPeriod = interestRatePerPeriod;
	}

	public BigDecimal getAnnualInterestRate() {
		return annualInterestRate;
	}

	public void setAnnualInterestRate(BigDecimal annualInterestRate) {
		this.annualInterestRate = annualInterestRate;
	}

	public EnumOptionData getRepaymentFrequencyType() {
		return repaymentFrequencyType;
	}

	public void setRepaymentFrequencyType(EnumOptionData repaymentFrequencyType) {
		this.repaymentFrequencyType = repaymentFrequencyType;
	}

	public EnumOptionData getInterestRateFrequencyType() {
		return interestRateFrequencyType;
	}

	public void setInterestRateFrequencyType(
			EnumOptionData interestRateFrequencyType) {
		this.interestRateFrequencyType = interestRateFrequencyType;
	}

	public EnumOptionData getAmortizationType() {
		return amortizationType;
	}

	public void setAmortizationType(EnumOptionData amortizationType) {
		this.amortizationType = amortizationType;
	}

	public EnumOptionData getInterestType() {
		return interestType;
	}

	public void setInterestType(EnumOptionData interestType) {
		this.interestType = interestType;
	}

	public EnumOptionData getInterestCalculationPeriodType() {
		return interestCalculationPeriodType;
	}

	public void setInterestCalculationPeriodType(
			EnumOptionData interestCalculationPeriodType) {
		this.interestCalculationPeriodType = interestCalculationPeriodType;
	}

	public String getLifeCycleStatusText() {
		return lifeCycleStatusText;
	}

	public void setLifeCycleStatusText(String lifeCycleStatusText) {
		this.lifeCycleStatusText = lifeCycleStatusText;
	}

	public LocalDate getLifeCycleStatusDate() {
		return lifeCycleStatusDate;
	}

	public void setLifeCycleStatusDate(LocalDate lifeCycleStatusDate) {
		this.lifeCycleStatusDate = lifeCycleStatusDate;
	}

	public Long getFundId() {
		return fundId;
	}

	public void setFundId(Long fundId) {
		this.fundId = fundId;
	}

	public String getFundName() {
		return fundName;
	}

	public void setFundName(String fundName) {
		this.fundName = fundName;
	}

	public Long getLoanProductId() {
		return loanProductId;
	}

	public void setLoanProductId(Long loanProductId) {
		this.loanProductId = loanProductId;
	}

	public Integer getLifeCycleStatusId() {
		return lifeCycleStatusId;
	}

	public void setLifeCycleStatusId(Integer lifeCycleStatusId) {
		this.lifeCycleStatusId = lifeCycleStatusId;
	}

}