package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable command for loan transactions.
 */
public class LoanTransactionCommand {

	private final Long loanId;
	private final LocalDate transactionDate;
	private final BigDecimal transactionAmount;
	private final String note;

	public LoanTransactionCommand(final Long loanId, final LocalDate paymentDate, final BigDecimal paymentAmount,final String note) {
		this.loanId = loanId;
		this.transactionDate = paymentDate;
		this.transactionAmount = paymentAmount;
		this.note = note;
	}

	public Long getLoanId() {
		return loanId;
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