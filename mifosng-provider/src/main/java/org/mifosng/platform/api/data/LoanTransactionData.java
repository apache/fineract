package org.mifosng.platform.api.data;

import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.LocalDate;

/**
 * TODO - KW - Think this is deprecated now.
 */
@Deprecated
@JsonFilter("loanRepaymentFilter")
public class LoanTransactionData {

	private Long id;
	private EnumOptionData transactionType;
	private LocalDate date;
	private MoneyData principal;
	private MoneyData interest;
	private MoneyData total;
	private MoneyData totalWaived;
	private MoneyData overpaid;

	public LoanTransactionData() {
		//
	}

	public LoanTransactionData(
			Long id, EnumOptionData transactionType, final LocalDate date,
			final MoneyData principal,
			final MoneyData interest,
			final MoneyData total, final MoneyData overpaid) {
        this.id = id;
		this.transactionType = transactionType;
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
	
	public EnumOptionData getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(EnumOptionData transactionType) {
		this.transactionType = transactionType;
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