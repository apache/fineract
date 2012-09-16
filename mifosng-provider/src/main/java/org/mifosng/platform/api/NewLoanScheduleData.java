package org.mifosng.platform.api;

import java.math.BigDecimal;

/**
 * Immutable data object to represent aspects of a loan schedule such as:
 * 
 * <ul>
 * 	   <li>summary information - the totals for each part of schedule monitored</li>
 *     <li>repayment schedule  - the principal due, outstanding balance and cost of loan items such as interest and charges</li>
 * </ul>
 */
public class NewLoanScheduleData {

	/**
	 * The cumulative total of all money (principal) disbursed to the loan applicant.
	 */
	private final BigDecimal principalDisbursed;
	private final BigDecimal principalPaid;
	private final BigDecimal principalOutstanding;
	
	/**
	 * The total interest expected on any principal disbursed.
	 */
	private final BigDecimal interestExpected;
	private final BigDecimal interestPaid;
	private final BigDecimal interestWaived;
	private final BigDecimal interestOutstanding;
	
	/**
	 * The cumulative total of all charges applied on the loan to date.
	 */
	private final BigDecimal chargesToDate;
	private final BigDecimal chargesPaid;
	private final BigDecimal chargesOutstanding;
	
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

	public NewLoanScheduleData() {
		this.principalDisbursed = null;
		this.principalPaid = null;
		this.principalOutstanding = null;
		
		this.interestExpected = null;
		this.interestPaid = null;
		this.interestWaived = null;
		this.interestOutstanding = null;
		
		this.chargesToDate = null;
		this.chargesPaid = null;
		this.chargesOutstanding = null;
		
		this.totalCostOfLoan = null;
		
		this.totalExpectedRepayment = null;
		this.totalInArrears = null;
	}
}