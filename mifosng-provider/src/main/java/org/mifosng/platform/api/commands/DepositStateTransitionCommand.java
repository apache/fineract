package org.mifosng.platform.api.commands;

import org.joda.time.LocalDate;

public class DepositStateTransitionCommand {
	
	private final Long accountId;
	private final LocalDate eventDate;
	
	public DepositStateTransitionCommand(final Long accountId, final LocalDate enentDate) {
		this.accountId=accountId;
		this.eventDate=enentDate;
	}

	public Long getAccountId() {
		return accountId;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}
	
}
