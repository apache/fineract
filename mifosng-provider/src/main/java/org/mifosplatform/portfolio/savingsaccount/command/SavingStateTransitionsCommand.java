/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.command;

import org.joda.time.LocalDate;

public class SavingStateTransitionsCommand {
	
	private final Long accountId;
	private final LocalDate eventDate;
	private final String note;
	
	public SavingStateTransitionsCommand( final Long accountId, final LocalDate eventDate, final String note) {
		this.accountId = accountId;
		this.eventDate = eventDate;
		this.note = note;
	}

	public Long getAccountId() {
		return this.accountId;
	}

	public LocalDate getEventDate() {
		return this.eventDate;
	}

	public String getNote() {
		return this.note;
	}
}
