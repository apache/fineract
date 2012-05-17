package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class AdjustLoanTransactionCommand {

	private Long loanId;
	private Long repaymentId;
	private String dateFormat;
	private String transactionDateFormatted;
	private LocalDate transactionDate;
	private String comment;
	private String transactionAmountFormatted;
	private BigDecimal transactionAmount;

	protected AdjustLoanTransactionCommand() {
		//
	}

	public AdjustLoanTransactionCommand(final Long loanId, final Long repaymentId,
			final LocalDate paymentDate, final String comment,
			final BigDecimal paymentAmount) {
		this.loanId = loanId;
		this.repaymentId = repaymentId;
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

	public Long getRepaymentId() {
		return repaymentId;
	}

	public void setRepaymentId(Long repaymentId) {
		this.repaymentId = repaymentId;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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