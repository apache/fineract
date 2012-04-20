package org.mifosng.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanAccountSummaryData {

	private MoneyData originalPrincipal;
	private MoneyData originalInterest;
	private MoneyData originalTotal;
	private MoneyData principalPaid;
	private MoneyData principalOutstanding;
	private MoneyData interestPaid;
	private MoneyData interestOutstanding;
	private MoneyData totalPaid;
	private MoneyData totalOutstanding;
	private MoneyData totalInArrears;
	private MoneyData totalWaived;

	protected LoanAccountSummaryData() {
		//
	}

	public LoanAccountSummaryData(MoneyData originalPrincipal,
			MoneyData principalPaid, MoneyData principalOutstanding,
			MoneyData originalInterest, MoneyData interestPaid,
			MoneyData interestOutstanding, MoneyData originalTotal,
			MoneyData totalPaid, MoneyData totalOutstanding, MoneyData totalInArrears, MoneyData totalWaived) {
		this.originalPrincipal = originalPrincipal;
		this.principalPaid = principalPaid;
		this.principalOutstanding = principalOutstanding;
		this.originalInterest = originalInterest;
		this.interestPaid = interestPaid;
		this.interestOutstanding = interestOutstanding;
		this.originalTotal = originalTotal;
		this.totalPaid = totalPaid;
		this.totalOutstanding = totalOutstanding;
		this.totalInArrears = totalInArrears;
		this.totalWaived = totalWaived;
	}

	public boolean isWaiveAllowed(MoneyData tolerance) {
		return tolerance.isGreaterThan(this.totalOutstanding) || tolerance.isEqualTo(this.totalOutstanding);
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

	public MoneyData getTotalWaived() {
		return totalWaived;
	}

	public void setTotalWaived(MoneyData totalWaived) {
		this.totalWaived = totalWaived;
	}
}