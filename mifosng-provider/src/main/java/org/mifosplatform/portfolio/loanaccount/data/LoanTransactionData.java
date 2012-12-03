package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

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
	@SuppressWarnings("unused")
	private final BigDecimal principalPortion;
	@SuppressWarnings("unused")
	private final BigDecimal interestPortion;
	@SuppressWarnings("unused")
	private final BigDecimal feeChargesPortion;
	@SuppressWarnings("unused")
	private final BigDecimal penaltyChargesPortion;
	
	public LoanTransactionData(
			final Long id,
			final EnumOptionData transactionType, 
			final CurrencyData currency,
			final LocalDate date, 
			final BigDecimal amount, 
			final BigDecimal principalPortion, 
			final BigDecimal interestPortion, 
			final BigDecimal feeChargesPortion, 
			final BigDecimal penaltyChargesPortion) {
		this.id = id;
		this.type = transactionType;
		this.currency = currency;
		this.date = date;
		this.amount = amount;
		this.principalPortion = principalPortion;
		this.interestPortion = interestPortion;
		this.feeChargesPortion = feeChargesPortion;
		this.penaltyChargesPortion = penaltyChargesPortion;
	}

	public LocalDate dateOf() {
		return this.date;
	}

	public boolean isNotDisbursement() {
		return type.getId().equals(Integer.valueOf(1));
	}
}