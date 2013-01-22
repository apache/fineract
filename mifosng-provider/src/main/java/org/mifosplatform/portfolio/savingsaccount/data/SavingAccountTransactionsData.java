package org.mifosplatform.portfolio.savingsaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class SavingAccountTransactionsData {
	
	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final LocalDate transactionDate;
	@SuppressWarnings("unused")
	private final EnumOptionData transactionType;
	@SuppressWarnings("unused")
	private final BigDecimal amount;
	
	public SavingAccountTransactionsData() {
		this.id = null;
		this.transactionDate = null;
		this.transactionType = null;
		this.amount = BigDecimal.ZERO;
	}
	
	public SavingAccountTransactionsData(final Long id, final LocalDate transactionDate, EnumOptionData transactionType, BigDecimal amount){
		this.id = id;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.amount = amount;
	}

}
