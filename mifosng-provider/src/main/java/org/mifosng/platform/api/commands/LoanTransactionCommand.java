package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class LoanTransactionCommand {

	private Long loanId;
	private String locale;
	private String dateFormat;
	private String transactionDate;
	private LocalDate transactionLocalDate;
	private String note;
	private String transactionAmount;
	private BigDecimal transactionAmountValue;

	protected LoanTransactionCommand() {
		//
	}

	public LoanTransactionCommand(final Long loanId,
			final LocalDate paymentDate, final String note,
			final BigDecimal paymentAmount) {
		this.loanId = loanId;
		this.transactionLocalDate = paymentDate;
		this.note = note;
		this.transactionAmountValue = paymentAmount;
	}

	public Long getLoanId() {
		return this.loanId;
	}

	public void setLoanId(final Long loanId) {
		this.loanId = loanId;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	public LocalDate getTransactionLocalDate() {
		return transactionLocalDate;
	}

	public void setTransactionLocalDate(LocalDate transactionLocalDate) {
		this.transactionLocalDate = transactionLocalDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(String transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public BigDecimal getTransactionAmountValue() {
		return transactionAmountValue;
	}

	public void setTransactionAmountValue(BigDecimal transactionAmountValue) {
		this.transactionAmountValue = transactionAmountValue;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}