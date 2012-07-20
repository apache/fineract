package org.mifosng.platform.api.data;

import org.joda.time.LocalDate;

public class LoanTransactionDataDTO {

	private Long id;
	private EnumOptionData transactionType;
	private LocalDate date;
	private MoneyData total;

	public LoanTransactionDataDTO() {
		//
	}

	public LoanTransactionDataDTO(Long id, EnumOptionData transactionType,
			final LocalDate date, final MoneyData total) {
		this.id = id;
		this.transactionType = transactionType;
		this.date = date;
		this.total = total;
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

	public MoneyData getTotal() {
		return total;
	}

	public void setTotal(MoneyData total) {
		this.total = total;
	}

}