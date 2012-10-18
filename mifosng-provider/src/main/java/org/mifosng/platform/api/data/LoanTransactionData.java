package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanTransactionData {

	@SuppressWarnings("unused")
	private Long id;
	
	private EnumOptionData type;
	
	private LocalDate date;
	
	@SuppressWarnings("unused")
	private CurrencyData currency;
	
	@SuppressWarnings("unused")
	private BigDecimal amount;
	
	public LoanTransactionData(
			final Long id,
			final EnumOptionData transactionType, 
			final CurrencyData currency,
			final LocalDate date, 
			final BigDecimal amount) {
		this.id = id;
		this.type = transactionType;
		this.currency = currency;
		this.date = date;
		this.amount = amount;
	}

	public LocalDate dateOf() {
		return this.date;
	}

	public boolean isNotDisbursement() {
		return type.getId().equals(Integer.valueOf(1));
	}
}