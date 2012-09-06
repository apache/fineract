package org.mifosng.platform.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class DepositAccountTransactionData {
	
	private final Long transactionId;
	private final EnumOptionData transactionType;
	private final LocalDate transactionDate;
	private final BigDecimal transactionAmount;
	private final Long accountId;
	
	public DepositAccountTransactionData() {
		
		this.transactionId = null;
		this.accountId = null;
		this.transactionType = null;
		this.transactionDate = null;
		this.transactionAmount = null;
		
	}
	
	public DepositAccountTransactionData(final Long transactionId, final Long accountId, final EnumOptionData transactionType, final LocalDate transactionDate, final BigDecimal transactionAmount ){
		
		this.transactionId = transactionId;
		this.accountId = accountId;
		this.transactionType = transactionType;
		this.transactionDate = transactionDate;
		this.transactionAmount = transactionAmount;
	}

	public Long getAccountId() {
		return accountId;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public EnumOptionData getTransactionType() {
		return transactionType;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}
	
	

}
