package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedulePeriodData;

/**
 * Immutable data object to represent aspects of a loan schedule such as:
 * 
 * <ul>
 * 	   <li>summary information - the totals for each part of schedule monitored</li>
 *     <li>repayment schedule  - the principal due, outstanding balance and cost of loan items such as interest and charges</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class NewLoanScheduleData {

	private final Integer loanTermInDays;
	
	/**
	 * The currency associated with all monetary values in loan schedule.
	 */
	private final CurrencyData currency;
	
	/**
	 * The cumulative total of all money (principal) disbursed to the loan applicant.
	 */
	private final BigDecimal cumulativePrincipalDisbursed;
	
	/**
	 * The cumulative total of all <code>principalDue</code> for each period of loan schedule. (originalPrincipal)
	 */
	private final BigDecimal cumulativePrincipalDue;
	private final BigDecimal cumulativePrincipalPaid;
	private final BigDecimal cumulativePrincipalOutstanding;
	
	/**
	 * The cumulative total of interest expected on any principal disbursed. (originalInterest)
	 */
	private final BigDecimal cumulativeInterestExpected;
	private final BigDecimal cumulativeInterestPaid;
	private final BigDecimal cumulativeInterestWaived;
	private final BigDecimal cumulativeInterestOutstanding;
	
	/**
	 * The cumulative total of all charges applied on the loan to date.
	 */
	private final BigDecimal cumulativeChargesToDate;
	private final BigDecimal cumulativeChargesPaid;
	private final BigDecimal cumulativeChargesOutstanding;
	
	/**
	 * The cumulative total of all costs on the loan.
	 * Costs tracked are:
	 * <ul>
	 * 	   <li>Interest</li>
	 *     <li>Charges (fees and penalties)</li>
	 * </ul>
	 */
	private final BigDecimal totalCostOfLoan;
	
	/**
	 * The sum of <code>principalDisbursed</code> and <code>totalCostOfLoan</code>. (originalTotal)
	 */
	private final BigDecimal totalExpectedRepayment;
	
	/**
	 * The cumulative sum of all repayments to date. (totalPaid)
	 */
	private final BigDecimal totalPaidToDate;
	
	/**
	 * The cumulative sum of all waivers to date.
	 */
	private final BigDecimal totalWaivedToDate;
	
	/**
	 * The cumulative sum of all principal, interest & charges outstanding.
	 */
	private final BigDecimal totalOutstanding;
	
	/**
	 * The amount by which this loan is is arrears. 
	 * 
	 * Different to outstanding as arrears is based on whether a portion of the total outstanding is late and above any tolerance setting.
	 */
	private final BigDecimal totalInArrears;
	
	/**
	 * <code>periods</code> is collection of data objects containing specific
	 * information to each period of the loan schedule including disbursement and
	 * repayment information.
	 */
	private final Collection<LoanSchedulePeriodData> periods;

	public NewLoanScheduleData() {
		this.cumulativePrincipalDisbursed = null;
		this.cumulativePrincipalPaid = null;
		this.cumulativePrincipalDue = null;
		this.cumulativePrincipalOutstanding = null;
		
		this.cumulativeInterestExpected = null;
		this.cumulativeInterestPaid = null;
		this.cumulativeInterestWaived = null;
		this.cumulativeInterestOutstanding = null;
		
		this.cumulativeChargesToDate = null;
		this.cumulativeChargesPaid = null;
		this.cumulativeChargesOutstanding = null;
		
		this.totalCostOfLoan = null;
		
		this.totalExpectedRepayment = null;
		this.totalPaidToDate = null;
		this.totalWaivedToDate = null;
		this.totalOutstanding = null;
		this.totalInArrears = null;
		
		this.currency = null;
		this.periods = null;
		this.loanTermInDays = null;
	}

	public NewLoanScheduleData(
			final CurrencyData currency,
			final Collection<LoanSchedulePeriodData> periods, 
			final Integer loanTermInDays, 
			final BigDecimal cumulativePrincipalDisbursed, 
			final BigDecimal cumulativePrincipalDue, 
			final BigDecimal cumulativePrincipalOutstanding, 
			final BigDecimal cumulativeInterestExpected, 
			final BigDecimal cumulativeChargesToDate, 
			final BigDecimal totalExpectedRepayment) {
		this.currency = currency;
		this.periods = periods;
		this.loanTermInDays = loanTermInDays;
		
		this.cumulativePrincipalDisbursed = cumulativePrincipalDisbursed;
		this.cumulativePrincipalDue = cumulativePrincipalDue;
		this.cumulativePrincipalPaid = null;
		this.cumulativePrincipalOutstanding = cumulativePrincipalOutstanding;
		
		this.cumulativeInterestExpected = cumulativeInterestExpected;
		this.cumulativeInterestPaid = null;
		this.cumulativeInterestWaived = null;
		this.cumulativeInterestOutstanding = null;
		
		this.cumulativeChargesToDate = cumulativeChargesToDate;
		this.cumulativeChargesPaid = null;
		this.cumulativeChargesOutstanding = null;
		
		if (cumulativeChargesToDate != null) {
			this.totalCostOfLoan = cumulativeInterestExpected.add(cumulativeChargesToDate);
		} else if (cumulativeInterestExpected != null) {
			this.totalCostOfLoan = cumulativeInterestExpected;
		} else {
			this.totalCostOfLoan = null;
		}
		
		this.totalExpectedRepayment = totalExpectedRepayment;
		this.totalPaidToDate = BigDecimal.ZERO;
		this.totalWaivedToDate = BigDecimal.ZERO;
		this.totalOutstanding = totalExpectedRepayment;
		this.totalInArrears = BigDecimal.ZERO;
	}

	/*
	 * Used when fully populating
	 */
	public NewLoanScheduleData(final CurrencyData currency, final Collection<LoanSchedulePeriodData> periods,
			final Integer loanTermInDays,
			final BigDecimal cumulativePrincipalDisbursed,
			final BigDecimal cumulativePrincipalDue,
			final BigDecimal cumulativePrincipalPaid,
			final BigDecimal cumulativePrincipalOutstanding,
			final BigDecimal cumulativeInterestExpected,
			final BigDecimal cumulativeInterestPaid,
			final BigDecimal cumulativeInterestWaived,
			final BigDecimal cumulativeInterestOutstanding,
			final BigDecimal cumulativeChargesToDate,
			final BigDecimal cumulativeChargesPaid,
			final BigDecimal cumulativeChargesOutstanding,
			final BigDecimal totalCostOfLoan,
			final BigDecimal totalExpectedRepayment,
			final BigDecimal totalPaidToDate,
			final BigDecimal totalWaivedToDate,
			final BigDecimal totalOutstanding,
			final BigDecimal totalInArrears) {
		this.currency = currency;
		this.periods = periods;
		this.loanTermInDays = loanTermInDays;
		
		this.cumulativePrincipalDisbursed = cumulativePrincipalDisbursed;
		this.cumulativePrincipalDue = cumulativePrincipalDue;
		this.cumulativePrincipalPaid = cumulativePrincipalPaid;
		this.cumulativePrincipalOutstanding = cumulativePrincipalOutstanding;
		this.cumulativeInterestExpected = cumulativeInterestExpected;
		this.cumulativeInterestPaid = cumulativeInterestPaid;
		this.cumulativeInterestWaived = cumulativeInterestWaived;
		this.cumulativeInterestOutstanding = cumulativeInterestOutstanding;
		this.cumulativeChargesToDate = cumulativeChargesToDate;
		this.cumulativeChargesPaid = cumulativeChargesPaid;
		this.cumulativeChargesOutstanding = cumulativeChargesOutstanding;
		this.totalCostOfLoan = totalCostOfLoan;
		this.totalExpectedRepayment = totalExpectedRepayment;
		this.totalPaidToDate = totalPaidToDate;
		this.totalWaivedToDate = totalWaivedToDate;
		this.totalOutstanding = totalOutstanding;
		this.totalInArrears = totalInArrears;
	}

	public Collection<LoanSchedulePeriodData> getPeriods() {
		return periods;
	}

	public BigDecimal totalOutstanding() {
		return this.totalOutstanding;
	}
}