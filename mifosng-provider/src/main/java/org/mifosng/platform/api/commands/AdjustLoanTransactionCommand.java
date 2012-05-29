package org.mifosng.platform.api.commands;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class AdjustLoanTransactionCommand {

	private Long loanId;
	private Long transactionId;
	private String locale;
	private String dateFormat;
	private String transactionDate;
	private LocalDate transactionLocalDate;
	private String note;
	private String transactionAmount;
	private BigDecimal transactionAmountValue;

	protected AdjustLoanTransactionCommand() {
		//
	}

	public AdjustLoanTransactionCommand(final Long loanId, final Long transactionId,
			final LocalDate transactionLocalDate, final String note,
			final BigDecimal transactionAmountValue) {
		this.loanId = loanId;
		this.transactionId = transactionId;
		this.transactionLocalDate = transactionLocalDate;
		this.note = note;
		this.transactionAmountValue = transactionAmountValue;
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

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
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
}