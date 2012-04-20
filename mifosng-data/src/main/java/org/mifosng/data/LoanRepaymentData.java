package org.mifosng.data;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateMidnight;
import org.joda.time.Days;
import org.joda.time.LocalDate;

@XmlRootElement
public class LoanRepaymentData {

	private Long id;
	private LocalDate date;
	private MoneyData principal;
	private MoneyData interest;
	private MoneyData total;
	private MoneyData totalWaived;
	private MoneyData overpaid;

	public LoanRepaymentData() {
		//
	}

	public LoanRepaymentData(
			Long id, final LocalDate date,
			final MoneyData principal,
			final MoneyData interest,
			final MoneyData total, final MoneyData overpaid) {
        this.id = id;
		this.date = date;
		this.principal = principal;
        this.interest = interest;
        this.total = total;
		this.overpaid = overpaid;
    }
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}
	
	public int getTransactionOffsetFromToday() {
		return Days.daysBetween(new DateMidnight().toDateTime(), this.date.toDateMidnight().toDateTime()).getDays();
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

	public MoneyData getOverpaid() {
		return overpaid;
	}

	public void setOverpaid(MoneyData overpaid) {
		this.overpaid = overpaid;
	}

	public boolean hasOverpaidComponent() {
		return this.overpaid.isGreaterThanZero();
	}

	public MoneyData getTotalWaived() {
		return totalWaived;
	}

	public void setTotalWaived(MoneyData totalWaived) {
		this.totalWaived = totalWaived;
	}
}