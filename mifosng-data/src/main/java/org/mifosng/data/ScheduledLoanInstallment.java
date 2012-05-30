package org.mifosng.data;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement(name = "installment")
public class ScheduledLoanInstallment implements Serializable {

	private Integer installmentNumber;
	private LocalDate periodStart;
	private LocalDate periodEnd;
	private MoneyData principalDue;
	private MoneyData interestDue;
	private MoneyData totalInstallmentDue;
	private MoneyData outStandingBalance;

	public ScheduledLoanInstallment() {
		//
	}

	public ScheduledLoanInstallment(final Integer installmentNumber,
			final LocalDate periodStart, final LocalDate periodEnd,
			final MoneyData principalDue,
			final MoneyData interestDue, 
			final MoneyData totalInstallmentDue,
			final MoneyData outStandingBalance) {
		this.installmentNumber = installmentNumber;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.principalDue = principalDue;
		this.interestDue = interestDue;
		this.totalInstallmentDue = totalInstallmentDue;
		this.outStandingBalance = outStandingBalance;
	}

	public Integer getInstallmentNumber() {
		return this.installmentNumber;
	}

	public LocalDate getPeriodStart() {
		return this.periodStart;
	}

	public LocalDate getPeriodEnd() {
		return this.periodEnd;
	}

	// @DateTimeFormat(pattern = "yyyy-MM-dd")
	public void setDueDate(final LocalDate dueDate) {
		this.periodEnd = dueDate;
	}

	public void setPrincipal(final BigDecimal principal) {
		CurrencyData currencyData = currencyData(this.principalDue);
		this.principalDue = MoneyData.of(currencyData, principal);
		this.totalInstallmentDue = this.principalDue.plus(this.interestDue);
	}

	private CurrencyData currencyData(MoneyData moneyData) {
		String code = moneyData.getCurrencyCode();
		String name = moneyData.getDefaultName();
		int decimalPlaces = moneyData.getDigitsAfterDecimal();
		String displaySymbol = moneyData.getDisplaySymbol();
		String nameCode = moneyData.getNameCode();
		return new CurrencyData(code, name, decimalPlaces, displaySymbol, nameCode);
	}

	public void setInterest(final BigDecimal interest) {
		CurrencyData currencyData = currencyData(this.principalDue);
		this.interestDue = MoneyData.of(currencyData, interest);
		this.totalInstallmentDue = this.principalDue.plus(this.interestDue);
	}

	public void setOutstanding(final BigDecimal outstanding) {
		CurrencyData currencyData = currencyData(this.principalDue);
		this.outStandingBalance = MoneyData.of(currencyData, outstanding);
	}

	public MoneyData getPrincipalDue() {
		return this.principalDue;
	}

	public void setPrincipalDue(final MoneyData principalDue) {
		this.principalDue = principalDue;
	}

	public MoneyData getInterestDue() {
		return this.interestDue;
	}

	public void setInterestDue(final MoneyData interestDue) {
		this.interestDue = interestDue;
	}

	public MoneyData getTotalInstallmentDue() {
		return this.totalInstallmentDue;
	}

	public void setTotalInstallmentDue(final MoneyData totalInstallmentDue) {
		this.totalInstallmentDue = totalInstallmentDue;
	}

	public MoneyData getOutStandingBalance() {
		return this.outStandingBalance;
	}

	public void setOutStandingBalance(final MoneyData outStandingBalance) {
		this.outStandingBalance = outStandingBalance;
	}

	public void setInstallmentNumber(final Integer installmentNumber) {
		this.installmentNumber = installmentNumber;
	}

	public void setPeriodStart(final LocalDate periodStart) {
		this.periodStart = periodStart;
	}

	public void setPeriodEnd(final LocalDate periodEnd) {
		this.periodEnd = periodEnd;
	}
}