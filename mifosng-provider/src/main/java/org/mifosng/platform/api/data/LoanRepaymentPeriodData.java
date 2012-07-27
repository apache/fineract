package org.mifosng.platform.api.data;

import org.joda.time.LocalDate;

public class LoanRepaymentPeriodData {

	private Long loanId;
	private Integer period;
	private LocalDate date;
	private MoneyData principal;
	private MoneyData principalPaid;
	private MoneyData principalOutstanding;
	private MoneyData interest;
	private MoneyData interestPaid;
	private MoneyData interestWaived;
	private MoneyData interestOutstanding;
	private MoneyData total;
	private MoneyData totalPaid;
	private MoneyData totalWaived;
	private MoneyData totalOutstanding;

	public LoanRepaymentPeriodData() {
		//
	}

	public LoanRepaymentPeriodData(Long loanId, Integer period,
			LocalDate date, MoneyData principal, MoneyData principalPaid,
			MoneyData principalOutstanding, MoneyData interest,
			MoneyData interestPaid, MoneyData interestWaived, MoneyData interestOutstanding,
			MoneyData total, MoneyData totalPaid, MoneyData totalWaived, MoneyData totalOutstanding) {
		this.loanId = loanId;
		this.period = period;
		this.date = date;
		this.principal = principal;
		this.principalPaid = principalPaid;
		this.principalOutstanding = principalOutstanding;
		this.interest = interest;
		this.interestPaid = interestPaid;
		this.interestWaived = interestWaived;
		this.interestOutstanding = interestOutstanding;
		this.total = total;
		this.totalPaid = totalPaid;
		this.totalWaived = totalWaived;
		this.totalOutstanding = totalOutstanding;
	}

	public Long getLoanId() {
		return loanId;
	}

	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public MoneyData getPrincipal() {
		return principal;
	}

	public void setPrincipal(MoneyData principal) {
		this.principal = principal;
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

	public MoneyData getInterest() {
		return interest;
	}

	public void setInterest(MoneyData interest) {
		this.interest = interest;
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

	public MoneyData getTotal() {
		return total;
	}

	public void setTotal(MoneyData total) {
		this.total = total;
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

}