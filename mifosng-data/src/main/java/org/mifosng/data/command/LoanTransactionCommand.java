package org.mifosng.data.command;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class LoanTransactionCommand {

	private Long loanId;
	private LocalDate paymentDate;
	private String comment;
	private BigDecimal paymentAmount;

	protected LoanTransactionCommand() {
		//
	}

	public LoanTransactionCommand(final Long loanId,
			final LocalDate paymentDate, final String comment,
			final BigDecimal paymentAmount) {
		this.loanId = loanId;
		this.paymentDate = paymentDate;
		this.comment = comment;
		this.paymentAmount = paymentAmount;
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

	public LocalDate getPaymentDate() {
		return this.paymentDate;
	}

	public void setPaymentDate(final LocalDate paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getPaymentAmount() {
		return this.paymentAmount;
	}

	public void setPaymentAmount(final BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
}