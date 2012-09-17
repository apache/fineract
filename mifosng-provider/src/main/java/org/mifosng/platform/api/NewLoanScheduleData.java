package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Collection;

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

	/**
	 * The cumulative total of all money (principal) disbursed to the loan applicant.
	 */
	private final BigDecimal cumulativePrincipalDisbursed;
	private final BigDecimal cumulativePrincipalPaid;
	private final BigDecimal cumulativePrincipalOutstanding;
	
	/**
	 * The total interest expected on any principal disbursed.
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
	 * The sum of <code>principalDisbursed</code> and <code>totalCostOfLoan</code>.
	 */
	private final BigDecimal totalExpectedRepayment;
	private final BigDecimal totalInArrears;
	
	/**
	 * <code>periods</code> 
	 */
	private final Collection<LoanSchedulePeriodData> periods;

	public NewLoanScheduleData() {
		this.cumulativePrincipalDisbursed = null;
		this.cumulativePrincipalPaid = null;
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
		this.totalInArrears = null;
		
		this.periods = null;
	}

	public NewLoanScheduleData(final Collection<LoanSchedulePeriodData> periods) {
		this.periods = periods;
		
		this.cumulativePrincipalDisbursed = null;
		this.cumulativePrincipalPaid = null;
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
		this.totalInArrears = null;
	}
}