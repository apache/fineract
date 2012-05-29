package org.mifosng.platform.api.commands;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

/**
 * Use for any of the loan state transitions eg. reject, withdrawn by client, approve, disburse
 */
@XmlRootElement
public class LoanStateTransitionCommand {

	private Long loanId;
	private String dateFormat;
	private String eventDate;
	private LocalDate eventLocalDate;
	private String note;

	protected LoanStateTransitionCommand() {
		//
	}

	public LoanStateTransitionCommand(final Long loanId, final LocalDate eventDate, final String comment) {
		this.loanId = loanId;
		this.eventLocalDate = eventDate;
		this.note = comment;
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

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	public LocalDate getEventLocalDate() {
		return eventLocalDate;
	}

	public void setEventLocalDate(LocalDate eventLocalDate) {
		this.eventLocalDate = eventLocalDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}