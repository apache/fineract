package org.mifosng.platform.api.data;

import java.math.BigDecimal;

public class LoanAccountSummaryData {

	private MoneyData originalPrincipal;
	private MoneyData originalInterest;
	private MoneyData originalTotal;
	private MoneyData principalPaid;
	private MoneyData principalOutstanding;
	private MoneyData interestPaid;
	private MoneyData interestWaived;
	private MoneyData interestOutstanding;
	private MoneyData totalPaid;
	private MoneyData totalWaived;
	private MoneyData totalOutstanding;
	private MoneyData totalInArrears;

	protected LoanAccountSummaryData() {
		//
	}

	public LoanAccountSummaryData(
			MoneyData originalPrincipal,
			MoneyData principalPaid, 
			MoneyData principalOutstanding,
			MoneyData originalInterest, 
			MoneyData interestPaid,
			MoneyData interestWaived,
			MoneyData interestOutstanding, 
			MoneyData originalTotal,
			MoneyData totalPaid, 
			MoneyData totalWaived,
			MoneyData totalOutstanding, 
			MoneyData totalInArrears) {
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

	public boolean isWaiveAllowed(final CurrencyData currency, final BigDecimal toleranceAmount) {
		MoneyData tolerance = MoneyData.of(currency, toleranceAmount);
		return this.totalOutstanding.isGreaterThanZero() && (tolerance.isGreaterThan(this.totalOutstanding) || tolerance.isEqualTo(this.totalOutstanding));
	}
	
	public MoneyData getOriginalPrincipal() {
		return originalPrincipal;
	}

	public void setOriginalPrincipal(MoneyData originalPrincipal) {
		this.originalPrincipal = originalPrincipal;
	}

	public MoneyData getOriginalInterest() {
		return originalInterest;
	}

	public void setOriginalInterest(MoneyData originalInterest) {
		this.originalInterest = originalInterest;
	}

	public MoneyData getOriginalTotal() {
		return originalTotal;
	}

	public void setOriginalTotal(MoneyData originalTotal) {
		this.originalTotal = originalTotal;
	}

	public MoneyData getPrincipalPaid() {
		return principalPaid;
	}

	public void setPrincipalPaid(MoneyData principalPaid) {
		this.principalPaid = principalPaid;
	}

	public MoneyData getPrincipalOutstanding() {
		return principalOutstanding;
	}

	public void setPrincipalOutstanding(MoneyData principalOutstanding) {
		this.principalOutstanding = principalOutstanding;
	}

	public MoneyData getInterestPaid() {
		return interestPaid;
	}

	public void setInterestPaid(MoneyData interestPaid) {
		this.interestPaid = interestPaid;
	}
	
	public MoneyData getInterestWaived() {
		return interestWaived;
	}

	public void setInterestWaived(MoneyData interestWaived) {
		this.interestWaived = interestWaived;
	}

	public MoneyData getInterestOutstanding() {
		return interestOutstanding;
	}

	public void setInterestOutstanding(MoneyData interestOutstanding) {
		this.interestOutstanding = interestOutstanding;
	}

	public MoneyData getTotalPaid() {
		return totalPaid;
	}

	public void setTotalPaid(MoneyData totalPaid) {
		this.totalPaid = totalPaid;
	}
	
	public MoneyData getTotalWaived() {
		return totalWaived;
	}

	public void setTotalWaived(MoneyData totalWaived) {
		this.totalWaived = totalWaived;
	}

	public MoneyData getTotalOutstanding() {
		return totalOutstanding;
	}

	public void setTotalOutstanding(MoneyData totalOutstanding) {
		this.totalOutstanding = totalOutstanding;
	}

	public MoneyData getTotalInArrears() {
		return totalInArrears;
	}

	public void setTotalInArrears(MoneyData totalInArrears) {
		this.totalInArrears = totalInArrears;
	}
}