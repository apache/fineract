package org.mifosng.platform.api.data;

import java.math.BigDecimal;

/**
 * Immutable data object representing summary information of loan.
 * 
 * <code>currency</code> is provided in summary even though its also passed with basic loan information so its possible to just request summary only.
 * 
 * Note: getter/setter not added on purpose - google-gson only requires fields to generate json.
 */
@SuppressWarnings("unused")
public class LoanAccountSummaryData {

	private final CurrencyData currency;
	private final BigDecimal originalPrincipal;
	private final BigDecimal originalInterest;
	private final BigDecimal originalTotal;
	private final BigDecimal principalPaid;
	private final BigDecimal principalOutstanding;
	private final BigDecimal interestPaid;
	private final BigDecimal interestWaived;
	private final BigDecimal interestOutstanding;
	private final BigDecimal totalPaid;
	private final BigDecimal totalWaived;
	private final BigDecimal totalOutstanding;
	private final BigDecimal totalInArrears;

	public LoanAccountSummaryData(
			final CurrencyData currency,
			final BigDecimal originalPrincipal,
			final BigDecimal principalPaid, 
			final BigDecimal principalOutstanding,
			final BigDecimal originalInterest, 
			final BigDecimal interestPaid,
			final BigDecimal interestWaived,
			final BigDecimal interestOutstanding, 
			final BigDecimal originalTotal,
			final BigDecimal totalPaid, 
			final BigDecimal totalWaived,
			final BigDecimal totalOutstanding, 
			final BigDecimal totalInArrears) {
		this.currency = currency;
		this.originalPrincipal = originalPrincipal;
		this.principalPaid = principalPaid;
		this.principalOutstanding = principalOutstanding;
		this.originalInterest = originalInterest;
		this.interestPaid = interestPaid;
		this.interestWaived = interestWaived;
		this.interestOutstanding = interestOutstanding;
		this.originalTotal = originalTotal;
		this.totalPaid = totalPaid;
		this.totalWaived = totalWaived;
		this.totalOutstanding = totalOutstanding;
		this.totalInArrears = totalInArrears;
	}

	public BigDecimal totalOutstanding() {
		return this.totalOutstanding;
	}
}