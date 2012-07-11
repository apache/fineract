package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

public class LoanBasicDetailsData {

	private Long id;
	private String externalId;
	private FundData fund;
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
	
	private String lifeCycleStatusText;
	private LocalDate lifeCycleStatusDate;
	
	protected LoanBasicDetailsData() {
		//
	}
	
	public LoanBasicDetailsData(Long id, String externalId,
			String loanProductName, FundData fund, LocalDate closedOnDate,
			LocalDate submittedOnDate, LocalDate approvedOnDate,
			LocalDate expectedDisbursedOnLocalDate,
			LocalDate acutalDisbursedOnLocalDate,
			LocalDate expectedMaturityDate,
			LocalDate expectedFirstRepaymentOnDate,
			LocalDate interestChargedFromDate, 
			MoneyData principal, MoneyData inArrearsTolerance, Integer numberOfRepayments, Integer repaymentEvery, 
			BigDecimal interestRatePerPeriod, BigDecimal annualInterestRate, 
			EnumOptionData repaymentFrequencyType, EnumOptionData interestRateFrequencyType, EnumOptionData amortizationType, 
			EnumOptionData interestType, EnumOptionData interestCalculationPeriodType, 
			String lifeCycleStatusText, LocalDate lifeCycleStatusDate) {
		this.id = id;
		this.externalId = externalId;
		this.loanProductName = loanProductName;
		this.fund = fund;
		this.closedOnDate = closedOnDate;
		this.submittedOnDate = submittedOnDate;
		this.approvedOnDate = approvedOnDate;
		this.expectedDisbursementDate = expectedDisbursedOnLocalDate;
		actualDisbursementDate = acutalDisbursedOnLocalDate;
		this.expectedMaturityDate = expectedMaturityDate;
		this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
		this.interestChargedFromDate = interestChargedFromDate;
		this.principal = principal;
		this.inArrearsTolerance = inArrearsTolerance;
		this.numberOfRepayments = numberOfRepayments;
		this.repaymentEvery = repaymentEvery;
		this.interestRatePerPeriod = interestRatePerPeriod;
		this.annualInterestRate = annualInterestRate;
		this.repaymentFrequencyType = repaymentFrequencyType;
		this.interestRateFrequencyType = interestRateFrequencyType;
		this.amortizationType = amortizationType;
		this.interestType = interestType;
		this.interestCalculationPeriodType = interestCalculationPeriodType;
		this.lifeCycleStatusText = lifeCycleStatusText;
		this.lifeCycleStatusDate = lifeCycleStatusDate;
	}

	public int getMaxSubmittedOnOffsetFromToday() {
		return Days.daysBetween(new DateTime(),
				this.getSubmittedOnDate().toDateMidnight().toDateTime())
				.getDays();
	}

	public int getMaxApprovedOnOffsetFromToday() {
		
		int offset = 0;
		if (this.getApprovedOnDate() != null) {
			offset =  Days.daysBetween(new DateTime(),
					this.getApprovedOnDate().toDateMidnight().toDateTime())
					.getDays();
		}
		
		return offset;
	}

	public int getMaxDisbursedOnOffsetFromToday() {
		
		int offset = 0;
		if (this.getActualDisbursementDate() != null) {
			offset = Days.daysBetween(new DateTime(),
					this.getActualDisbursementDate().toDateMidnight().toDateTime())
					.getDays();
		}
		
		return offset;
	}
	
	public int getActualLoanTermInDays() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		if (getClosedOnDate() != null) {
			closingDateToUse = getClosedOnDate();
		}
		
		return  Days.daysBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getDays();
	}
	
	public int getActualLoanTermInMonths() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		if (getClosedOnDate() != null) {
			closingDateToUse = getClosedOnDate();
		}
		
		return Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getMonths();
	}
	
	public int getLoanTermInDays() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		
		return  Days.daysBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getDays();
	}
	
	public int getLoanTermInMonths() {
		
		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}
		
		LocalDate closingDateToUse = getExpectedMaturityDate();
		
		return Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(), closingDateToUse.toDateMidnight().toDateTime()).getMonths();
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
	
	public FundData getFund() {
		return fund;
	}

	public void setFund(FundData fund) {
		this.fund = fund;
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
}