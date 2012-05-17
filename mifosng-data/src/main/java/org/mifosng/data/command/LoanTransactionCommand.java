package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class LoanTransactionCommand {

	private Long loanId;
	private String dateFormat;
	private String transactionDateFormatted;
	private LocalDate transactionDate;
	private String comment;
	private String transactionAmountFormatted;
	private BigDecimal transactionAmount;

	protected LoanTransactionCommand() {
		//
	}

	public LoanTransactionCommand(final Long loanId,
			final LocalDate paymentDate, final String comment,
			final BigDecimal paymentAmount) {
		this.loanId = loanId;
		this.transactionDate = paymentDate;
		this.comment = comment;
		this.transactionAmount = paymentAmount;
	}

	public Long getLoanId() {
		return this.loanId;
	}

	public void setLoanId(final Long loanId) {
		this.loanId = loanId;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getTransactionDateFormatted() {
		return transactionDateFormatted;
	}

	public void setTransactionDateFormatted(String transactionDateFormatted) {
		this.transactionDateFormatted = transactionDateFormatted;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransactionAmountFormatted() {
		return transactionAmountFormatted;
	}

	public void setTransactionAmountFormatted(String transactionAmountFormatted) {
		this.transactionAmountFormatted = transactionAmountFormatted;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}
}