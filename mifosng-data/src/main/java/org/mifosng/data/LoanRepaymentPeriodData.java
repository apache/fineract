package org.mifosng.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class LoanRepaymentPeriodData {

	private Integer period;
	private LocalDate date;
	private MoneyData disbursed;
	private MoneyData principal;
	private MoneyData principalPaid;
	private MoneyData principalOutstanding;
	private MoneyData interest;
	private MoneyData interestPaid;
	private MoneyData interestOutstanding;
	private MoneyData total;
	private MoneyData totalPaid;
	private MoneyData totalOutstanding;
	private MoneyData totalWaived;
	private MoneyData totalArrears;
	private LocalDate arrearsFrom;
	private LocalDate arrearsTo;
	private LocalDate paidInFullOn;
	private LocalDate lastAffectingPaymentOn;
	
	private static CurrencyData currencyData(MoneyData moneyData) {
		String code = moneyData.getCurrencyCode();
		String name = moneyData.getDefaultName();
		int decimalPlaces = moneyData.getDigitsAfterDecimal();
		String displaySymbol = moneyData.getDisplaySymbol();
		String nameCode = moneyData.getNameCode();
		return new CurrencyData(code, name, decimalPlaces, displaySymbol, nameCode);
	}

	public LoanRepaymentPeriodData() {
		//
	}
	
	public LoanRepaymentPeriodData(Integer period, LocalDate date,
			MoneyData disbursed, MoneyData total) {
		this.period = period;
		this.date = date;
		this.disbursed = disbursed;
		this.principal = null;
		this.principalPaid = null;
		this.principalOutstanding = null;
		this.interest = null;
		this.interestPaid = null;
		this.interestOutstanding = null;
		this.total = total;
		this.totalPaid = null;
		this.totalOutstanding = total;
	}

	public LoanRepaymentPeriodData(Integer period, LocalDate date,
			MoneyData disbursed, MoneyData principal,
			MoneyData interest, MoneyData total,
			MoneyData totalOutstanding) {
		this.period = period;
		this.date = date;
		this.disbursed = disbursed;
		this.principal = principal;
		this.principalPaid = MoneyData.zero(currencyData(principal));
		this.principalOutstanding = principal;
		this.interest = interest;
		this.interestPaid = MoneyData.zero(currencyData(principal));
		this.interestOutstanding = interest;
		this.total = total;
		this.totalPaid = MoneyData.zero(currencyData(principal));
		this.totalOutstanding = totalOutstanding;
	}
	
	public boolean isInArrearsWithToleranceOf(MoneyData arrearsTolerance, LocalDate asOf) {
		
		boolean inArrears = false;
		if (arrearsTolerance.isGreaterThanZero()) {
			inArrears = this.totalOutstanding.isGreaterThan(arrearsTolerance);
		} else {
			inArrears = this.totalOutstanding.isGreaterThanZero();
		}
		
		return inArrears && this.date.isBefore(asOf);
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

	public MoneyData getDisbursed() {
		return disbursed;
	}

	public void setDisbursed(MoneyData disbursed) {
		this.disbursed = disbursed;
	}

	public MoneyData getPrincipal() {
		return principal;
	}

	public void setPrincipal(MoneyData principal) {
		this.principal = principal;
	}

	public MoneyData getInterest() {
		return interest;
	}

	public void setInterest(MoneyData interest) {
		this.interest = interest;
	}

	public MoneyData getTotal() {
		return total;
	}

	public void setTotal(MoneyData total) {
		this.total = total;
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

	public boolean isFullyPaid() {
		return this.totalOutstanding.isZero();
	}

	public MoneyData getTotalArrears() {
		return totalArrears;
	}

	public void setTotalArrears(MoneyData totalArrears) {
		this.totalArrears = totalArrears;
	}

	public LocalDate getArrearsFrom() {
		return arrearsFrom;
	}

	public void setArrearsFrom(LocalDate arrearsFrom) {
		this.arrearsFrom = arrearsFrom;
	}

	public LocalDate getArrearsTo() {
		return arrearsTo;
	}

	public void setArrearsTo(LocalDate arrearsTo) {
		this.arrearsTo = arrearsTo;
	}

	public LocalDate getPaidInFullOn() {
		return paidInFullOn;
	}

	public void setPaidInFullOn(LocalDate paidInFullOn) {
		this.paidInFullOn = paidInFullOn;
	}

	public LocalDate getLastAffectingPaymentOn() {
		return lastAffectingPaymentOn;
	}

	public void setLastAffectingPaymentOn(LocalDate lastAffectingPaymentOn) {
		this.lastAffectingPaymentOn = lastAffectingPaymentOn;
	}

	public MoneyData getTotalWaived() {
		return totalWaived;
	}

	public void setTotalWaived(MoneyData totalWaived) {
		this.totalWaived = totalWaived;
	}
}