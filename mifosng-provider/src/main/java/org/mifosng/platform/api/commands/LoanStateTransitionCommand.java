package org.mifosng.platform.api.commands;

import org.joda.time.LocalDate;

/**
 * Immutable command for any of the loan state transitions eg. reject, withdrawn by client, approve, disburse
 */
public class LoanStateTransitionCommand {

	private final Long loanId;
	private final LocalDate eventDate;
	private final String note;

	public LoanStateTransitionCommand(final Long loanId, final LocalDate eventDate, final String note) {
		this.loanId = loanId;
		this.eventDate = eventDate;
		this.note = note;
	}

	public Long getLoanId() {
		return loanId;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public String getNote() {
		return note;
	}
}