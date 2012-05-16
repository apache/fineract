package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

/**
 * Use for any of the loan state transitions eg. reject, withdrawn by client, approve, disburse
 */
@XmlRootElement
public class LoanStateTransitionCommand {

	private Long loanId;
	private String dateFormat;
	private String eventDateFormatted;
	private LocalDate eventDate;
	private String comment;

	protected LoanStateTransitionCommand() {
		//
	}

	public LoanStateTransitionCommand(final Long loanId, final LocalDate eventDate,
			final String comment) {
		this.loanId = loanId;
		this.eventDate = eventDate;
		this.comment = comment;
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

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getEventDateFormatted() {
		return eventDateFormatted;
	}

	public void setEventDateFormatted(String eventDateFormatted) {
		this.eventDateFormatted = eventDateFormatted;
	}
}