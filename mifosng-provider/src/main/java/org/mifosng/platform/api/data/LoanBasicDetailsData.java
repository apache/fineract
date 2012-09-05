package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;

public class LoanBasicDetailsData {

	private final Long id;
	private final String externalId;
	private final Long clientId;
	private final String clientName;
	private final Long loanProductId;
	private final String loanProductName;
	private final String loanProductDescription;
	private final Long fundId;
	private final String fundName;
	private final EnumOptionData status;
	
	private final LocalDate submittedOnDate;
	private final LocalDate approvedOnDate;
	private final LocalDate expectedDisbursementDate;
	private final LocalDate actualDisbursementDate;
	private final LocalDate expectedFirstRepaymentOnDate;
	private final LocalDate interestChargedFromDate;
	private final LocalDate closedOnDate;
	private final LocalDate expectedMaturityDate;

	private final CurrencyData currency;
	private final BigDecimal principal;
	private final BigDecimal inArrearsTolerance;

	private final Integer termFrequency;
	private final EnumOptionData termPeriodFrequencyType;
	private final Integer numberOfRepayments;
	private final Integer repaymentEvery;
	private final Integer transactionStrategyId;
	private final BigDecimal interestRatePerPeriod;
	private final BigDecimal annualInterestRate;

	private final EnumOptionData repaymentFrequencyType;
	private final EnumOptionData interestRateFrequencyType;
	private final EnumOptionData amortizationType;
	private final EnumOptionData interestType;
	private final EnumOptionData interestCalculationPeriodType;

	private final LocalDate lifeCycleStatusDate;

	public LoanBasicDetailsData(
			final Long id, 
			final String externalId,
			final Long clientId, final String clientName,
			final Long loanProductId,
			final String loanProductName,
			final String loanProductDescription,
			final Long fundId, String fundName,
			final LocalDate closedOnDate, 
			final LocalDate submittedOnDate,
			final LocalDate approvedOnDate, 
			final LocalDate expectedDisbursementDate,
			final LocalDate actualDisbursementDate, 
			final LocalDate expectedMaturityDate,
			final LocalDate expectedFirstRepaymentOnDate,
			final LocalDate interestChargedFromDate, 
			final CurrencyData currency,
			final BigDecimal principal,
			final BigDecimal inArrearsTolerance, 
			final Integer numberOfRepayments,
			final Integer repaymentEvery, 
			final BigDecimal interestRatePerPeriod,
			final BigDecimal annualInterestRate,
			final EnumOptionData repaymentFrequencyType,
			final EnumOptionData interestRateFrequencyType,
			final EnumOptionData amortizationType, 
			final EnumOptionData interestType,
			final EnumOptionData interestCalculationPeriodType,
			final EnumOptionData status,
			final LocalDate lifeCycleStatusDate, 
			final Integer termFrequency, 
			final EnumOptionData termPeriodFrequencyType, 
			final Integer transactionStrategyId) {
		this.id = id;
		this.externalId = externalId;
		this.clientId = clientId;
		this.clientName = clientName;
		this.loanProductId = loanProductId;
		this.loanProductName = loanProductName;
		this.loanProductDescription = loanProductDescription;
		this.fundId = fundId;
		this.fundName = fundName;
		this.closedOnDate = closedOnDate;
		this.submittedOnDate = submittedOnDate;
		this.approvedOnDate = approvedOnDate;
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.actualDisbursementDate = actualDisbursementDate;
		this.expectedMaturityDate = expectedMaturityDate;
		this.expectedFirstRepaymentOnDate = expectedFirstRepaymentOnDate;
		this.interestChargedFromDate = interestChargedFromDate;
		this.currency = currency;
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
		this.status = status;
		this.lifeCycleStatusDate = lifeCycleStatusDate;
		this.termFrequency = termFrequency;
		this.termPeriodFrequencyType = termPeriodFrequencyType;
		this.transactionStrategyId = transactionStrategyId;
	}

	public int getMaxSubmittedOnOffsetFromToday() {
		return Days.daysBetween(new DateTime(),
				this.getSubmittedOnDate().toDateMidnight().toDateTime())
				.getDays();
	}

	public int getMaxApprovedOnOffsetFromToday() {

		int offset = 0;
		if (this.getApprovedOnDate() != null) {
			offset = Days.daysBetween(new DateTime(),
					this.getApprovedOnDate().toDateMidnight().toDateTime())
					.getDays();
		}

		return offset;
	}

	public int getMaxDisbursedOnOffsetFromToday() {

		int offset = 0;
		if (this.getActualDisbursementDate() != null) {
			offset = Days.daysBetween(
					new DateTime(),
					this.getActualDisbursementDate().toDateMidnight()
							.toDateTime()).getDays();
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

		return Days.daysBetween(dateToUse.toDateMidnight().toDateTime(),
				closingDateToUse.toDateMidnight().toDateTime()).getDays();
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

		return Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(),
				closingDateToUse.toDateMidnight().toDateTime()).getMonths();
	}

	public int getLoanTermInDays() {

		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}

		LocalDate closingDateToUse = getExpectedMaturityDate();

		return Days.daysBetween(dateToUse.toDateMidnight().toDateTime(),
				closingDateToUse.toDateMidnight().toDateTime()).getDays();
	}

	public int getLoanTermInMonths() {

		LocalDate dateToUse = getExpectedDisbursementDate();
		if (getActualDisbursementDate() != null) {
			dateToUse = getActualDisbursementDate();
		}

		LocalDate closingDateToUse = getExpectedMaturityDate();

		return Months.monthsBetween(dateToUse.toDateMidnight().toDateTime(),
				closingDateToUse.toDateMidnight().toDateTime()).getMonths();
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

	public Long getFundId() {
		return fundId;
	}

	public String getFundName() {
		return fundName;
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
	
	public Integer getTermFrequency() {
		return termFrequency;
	}

	public EnumOptionData getTermPeriodFrequencyType() {
		return termPeriodFrequencyType;
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

	public BigDecimal getPrincipal() {
		return principal;
	}

	public BigDecimal getInArrearsTolerance() {
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

	public LocalDate getLifeCycleStatusDate() {
		return lifeCycleStatusDate;
	}

	public Integer getTransactionStrategyId() {
		return transactionStrategyId;
	}
}