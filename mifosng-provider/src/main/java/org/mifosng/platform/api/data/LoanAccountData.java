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
	private final EnumOptionData status;
	private final Long fundId;
	private final String fundName;
	private final CurrencyData currency;
	private final BigDecimal principal;
	private final BigDecimal inArrearsTolerance;
	
	private final Integer termFrequency;
	private final EnumOptionData termPeriodFrequencyType;
	private final Integer numberOfRepayments;
	private final Integer repaymentEvery;
	private final EnumOptionData repaymentFrequencyType;
	private final Integer transactionStrategyId;
	private final EnumOptionData amortizationType;
	private final BigDecimal interestRatePerPeriod;
	private final EnumOptionData interestRateFrequencyType;
	private final BigDecimal annualInterestRate;
	private final EnumOptionData interestType;
	private final EnumOptionData interestCalculationPeriodType;
	
	private Collection<ChargeData> charges;
	
	private final LocalDate submittedOnDate;
	private final LocalDate approvedOnDate;
	private final LocalDate expectedDisbursementDate;
	private final LocalDate actualDisbursementDate;
	private final LocalDate repaymentsStartingFromDate;
	private final LocalDate interestChargedFromDate;
	private final LocalDate closedOnDate;
	private final LocalDate expectedMaturityDate;
	private final LocalDate lifeCycleStatusDate;
	
	// template
	private final Collection<LoanProductLookup> productOptions;
	private final Collection<EnumOptionData> termFrequencyTypeOptions;
	private final Collection<EnumOptionData> repaymentFrequencyTypeOptions;
	private final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions;
	private final Collection<EnumOptionData> interestRateFrequencyTypeOptions;
	private final Collection<EnumOptionData> amortizationTypeOptions;
	private final Collection<EnumOptionData> interestTypeOptions;
	private final Collection<EnumOptionData> interestCalculationPeriodTypeOptions;
	private final Collection<FundData> fundOptions;
	
	// associations
	private final LoanAccountSummaryData summary;
	private final Collection<LoanRepaymentPeriodData> repaymentSchedule;
	private final Collection<LoanRepaymentTransactionData> loanRepayments;

	private final LoanPermissionData permissions;
	private final LoanConvenienceData convenienceData;
	
	public LoanAccountData(
			final LoanBasicDetailsData basicDetails,
			final boolean convenienceDataRequired, 
			final LoanAccountSummaryData summary,
			final Collection<LoanRepaymentPeriodData> repaymentSchedule,
			final Collection<LoanRepaymentTransactionData> loanRepayments,
			final LoanPermissionData permissions, 
			final Collection<ChargeData> charges, 
			final Collection<LoanProductLookup> productOptions, 
			final Collection<EnumOptionData> termFrequencyTypeOptions, 
			final Collection<EnumOptionData> repaymentFrequencyTypeOptions, 
			final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions, 
			final Collection<EnumOptionData> interestRateFrequencyTypeOptions, 
			final Collection<EnumOptionData> amortizationTypeOptions, 
			final Collection<EnumOptionData> interestTypeOptions, 
			final Collection<EnumOptionData> interestCalculationPeriodTypeOptions, 
			final Collection<FundData> fundOptions) {
		this.summary = summary;
		this.repaymentSchedule = repaymentSchedule;
		this.loanRepayments = loanRepayments;
		this.permissions = permissions;
        this.charges = charges;
		this.productOptions = productOptions;
		this.termFrequencyTypeOptions = termFrequencyTypeOptions;
		this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
		this.repaymentStrategyOptions = transactionProcessingStrategyOptions;
		this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;
		this.amortizationTypeOptions = amortizationTypeOptions;
		this.interestTypeOptions = interestTypeOptions;
		this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
		this.fundOptions = fundOptions;

		if (convenienceDataRequired) {
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
		} else {
			this.convenienceData = null;
		}

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
		this.repaymentsStartingFromDate = basicDetails.getRepaymentsStartingFromDate();
		this.interestChargedFromDate = basicDetails.getInterestChargedFromDate();
		
		this.currency = basicDetails.getCurrency();
		this.principal = basicDetails.getPrincipal();
		this.inArrearsTolerance = basicDetails.getInArrearsTolerance();
		
		this.termFrequency = basicDetails.getTermFrequency();
		this.termPeriodFrequencyType = basicDetails.getTermPeriodFrequencyType();
		this.numberOfRepayments = basicDetails.getNumberOfRepayments();
		this.repaymentEvery = basicDetails.getRepaymentEvery();
		this.transactionStrategyId = basicDetails.getTransactionStrategyId();
		this.interestRatePerPeriod = basicDetails.getInterestRatePerPeriod();
		this.annualInterestRate = basicDetails.getAnnualInterestRate();
		this.repaymentFrequencyType = basicDetails.getRepaymentFrequencyType();
		this.interestRateFrequencyType = basicDetails
				.getInterestRateFrequencyType();
		this.amortizationType = basicDetails.getAmortizationType();
		this.interestType = basicDetails.getInterestType();
		this.interestCalculationPeriodType = basicDetails
				.getInterestCalculationPeriodType();
		
		this.status = basicDetails.getStatus();
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
	
	public EnumOptionData getStatus() {
		return status;
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
	
	public LocalDate getRepaymentsStartingFromDate() {
		return repaymentsStartingFromDate;
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

	public BigDecimal getPrincipal() {
		return this.principal;
	}

	public BigDecimal getInArrearsTolerance() {
		return this.inArrearsTolerance;
	}
	
	public Integer getTermFrequency() {
		return termFrequency;
	}

	public EnumOptionData getTermPeriodFrequencyType() {
		return termPeriodFrequencyType;
	}

	public Integer getNumberOfRepayments() {
		return numberOfRepayments;
	}

	public Integer getRepaymentEvery() {
		return repaymentEvery;
	}
	
	public Integer getTransactionStrategyId() {
		return transactionStrategyId;
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

	public Collection<LoanProductLookup> getProductOptions() {
		return productOptions;
	}

	public Collection<EnumOptionData> getTermFrequencyTypeOptions() {
		return termFrequencyTypeOptions;
	}

	public Collection<EnumOptionData> getRepaymentFrequencyTypeOptions() {
		return repaymentFrequencyTypeOptions;
	}

	public Collection<TransactionProcessingStrategyData> getRepaymentStrategyOptions() {
		return repaymentStrategyOptions;
	}

	public Collection<EnumOptionData> getInterestRateFrequencyTypeOptions() {
		return interestRateFrequencyTypeOptions;
	}

	public Collection<EnumOptionData> getAmortizationTypeOptions() {
		return amortizationTypeOptions;
	}

	public Collection<EnumOptionData> getInterestTypeOptions() {
		return interestTypeOptions;
	}

	public Collection<EnumOptionData> getInterestCalculationPeriodTypeOptions() {
		return interestCalculationPeriodTypeOptions;
	}

	public Collection<FundData> getFundOptions() {
		return fundOptions;
	}
}