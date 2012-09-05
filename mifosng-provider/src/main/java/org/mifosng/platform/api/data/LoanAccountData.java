package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing loan account data.
 */
public class LoanAccountData {

	private final Long id;
	private final String externalId;
	private final Long clientId;
	private final String clientName;
	private final Long loanProductId;
	private final String loanProductName;
	private final String loanProductDescription;
	private final Integer lifeCycleStatusId;
	private final String lifeCycleStatusText;
	private final LocalDate lifeCycleStatusDate;
	
	private final Long fundId;
	private final String fundName;

	private final LocalDate submittedOnDate;
	private final LocalDate approvedOnDate;
	private final LocalDate expectedDisbursementDate;
	private final LocalDate actualDisbursementDate;
	private final LocalDate expectedFirstRepaymentOnDate;
	private final LocalDate interestChargedFromDate;
	private final LocalDate closedOnDate;
	private final LocalDate expectedMaturityDate;

	private final CurrencyData currency;
	private final MoneyData principal;
	private final MoneyData inArrearsTolerance;

	private final Integer numberOfRepayments;
	private final Integer repaymentEvery;
	private final BigDecimal interestRatePerPeriod;
	private final BigDecimal annualInterestRate;

	private final EnumOptionData repaymentFrequencyType;
	private final EnumOptionData interestRateFrequencyType;
	private final EnumOptionData amortizationType;
	private final EnumOptionData interestType;
	private final EnumOptionData interestCalculationPeriodType;

	private final LoanAccountSummaryData summary;
	private final Collection<LoanRepaymentPeriodData> repaymentSchedule;
	private final Collection<LoanRepaymentTransactionData> loanRepayments;

	private final LoanPermissionData permissions;

	private final LoanConvenienceData convenienceData;
	private Collection<ChargeData> charges;

	public LoanAccountData(
			final LoanBasicDetailsData basicDetails,
			final LoanAccountSummaryData summary,
			final Collection<LoanRepaymentPeriodData> repaymentSchedule,
			final Collection<LoanRepaymentTransactionData> loanRepayments,
			final LoanPermissionData permissions, 
			final Collection<ChargeData> charges) {
		this.summary = summary;
		this.repaymentSchedule = repaymentSchedule;
		this.loanRepayments = loanRepayments;
		this.permissions = permissions;
        this.charges = charges;

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
		this.clientId = basicDetails.getClientId();
		this.clientName = basicDetails.getClientName();
		this.loanProductId = basicDetails.getLoanProductId();
		this.loanProductName = basicDetails.getLoanProductName();
		this.loanProductDescription = basicDetails.getLoanProductDescription();
		this.fundId = basicDetails.getFundId();
		this.fundName = basicDetails.getFundName();
		
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
		
		this.currency = basicDetails.getCurrency();
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

	public Long getId() {
		return id;
	}

	public String getExternalId() {
		return externalId;
	}

	public Long getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public Long getLoanProductId() {
		return loanProductId;
	}

	public String getLoanProductName() {
		return loanProductName;
	}

	public String getLoanProductDescription() {
		return loanProductDescription;
	}

	public Long getFundId() {
		return fundId;
	}

	public String getFundName() {
		return fundName;
	}
	
	public LocalDate getSubmittedOnDate() {
		return submittedOnDate;
	}

	public LocalDate getApprovedOnDate() {
		return approvedOnDate;
	}

	public LocalDate getExpectedDisbursementDate() {
		return expectedDisbursementDate;
	}

	public LocalDate getActualDisbursementDate() {
		return actualDisbursementDate;
	}

	public LocalDate getExpectedFirstRepaymentOnDate() {
		return expectedFirstRepaymentOnDate;
	}

	public LocalDate getInterestChargedFromDate() {
		return interestChargedFromDate;
	}

	public LocalDate getClosedOnDate() {
		return closedOnDate;
	}

	public LocalDate getExpectedMaturityDate() {
		return expectedMaturityDate;
	}
	
	public CurrencyData getCurrency() {
		return currency;
	}

	public MoneyData getPrincipal() {
		return principal;
	}

	public MoneyData getInArrearsTolerance() {
		return inArrearsTolerance;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}

	public BigDecimal getInterestRatePerPeriod() {
		return interestRatePerPeriod;
	}

	public BigDecimal getAnnualInterestRate() {
		return annualInterestRate;
	}

	public EnumOptionData getRepaymentFrequencyType() {
		return repaymentFrequencyType;
	}

	public EnumOptionData getInterestRateFrequencyType() {
		return interestRateFrequencyType;
	}

	public EnumOptionData getAmortizationType() {
		return amortizationType;
	}

	public EnumOptionData getInterestType() {
		return interestType;
	}

	public EnumOptionData getInterestCalculationPeriodType() {
		return interestCalculationPeriodType;
	}

	public Integer getLifeCycleStatusId() {
		return lifeCycleStatusId;
	}

	public String getLifeCycleStatusText() {
		return lifeCycleStatusText;
	}

	public LocalDate getLifeCycleStatusDate() {
		return lifeCycleStatusDate;
	}

	public LoanAccountSummaryData getSummary() {
		return summary;
	}

	public Collection<LoanRepaymentPeriodData> getRepaymentSchedule() {
		return repaymentSchedule;
	}

	public Collection<LoanRepaymentTransactionData> getLoanRepayments() {
		return loanRepayments;
	}

	public LoanPermissionData getPermissions() {
		return permissions;
	}

	public LoanConvenienceData getConvenienceData() {
		return convenienceData;
	}

    public Collection<ChargeData> getCharges() {
        return this.charges;
    }

    public void setCharges(Collection<ChargeData> charges) {
        this.charges = charges;
    }
}