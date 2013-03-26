/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.exception;

import org.mifosplatform.commands.domain.CommandSource;

public class RollbackTransactionAsCommandIsNotApprovedByCheckerException extends
		RuntimeException {

	/**
	 * When maker-checker is configured globally and also for the current
	 * transaction.
	 * 
	 * An initial save determines if there are any integrity rule or data
	 * problems.
	 * 
	 * If there isn't... and the transaction is from a maker... then this roll
	 * back is issued and the commandSourceResult is used to write the audit
	 * entry.
	 */
	private final CommandSource commandSourceResult;

	public RollbackTransactionAsCommandIsNotApprovedByCheckerException(
			final CommandSource commandSourceResult) {
		this.commandSourceResult = commandSourceResult;
	}

	public CommandSource getCommandSourceResult() {
		return this.commandSourceResult;
	}
}
