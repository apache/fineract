package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanTransactionNewData {

	@SuppressWarnings("unused")
	private Long id;
	
	@SuppressWarnings("unused")
	private EnumOptionData type;
	
	@SuppressWarnings("unused")
	private LocalDate date;
	
	@SuppressWarnings("unused")
	private CurrencyData currency;
	
	@SuppressWarnings("unused")
	private BigDecimal amount;
	
	public LoanTransactionNewData(
			final EnumOptionData transactionsType, 
			final CurrencyData currency,
			final LocalDate date, 
			final BigDecimal amount) {
		this.type = transactionsType;
		this.currency = currency;
		this.date = date;
		this.amount = amount;
	}
}
