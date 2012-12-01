package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable command for adjusting loan transactions.
 */
public class AdjustLoanTransactionCommand {

	private final Long loanId;
	private final Long transactionId;
	private final LocalDate transactionDate;
	private final BigDecimal transactionAmount;
	private final String note;
	
	public AdjustLoanTransactionCommand(final Long loanId, final Long transactionId,
			final LocalDate transactionLocalDate, final String note,
			final BigDecimal transactionAmountValue) {
		this.loanId = loanId;
		this.transactionId = transactionId;
		this.transactionDate = transactionLocalDate;
		this.transactionAmount = transactionAmountValue;
		this.note = note;
	}

	public Long getLoanId() {
		return loanId;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public String getNote() {
		return note;
	}
}