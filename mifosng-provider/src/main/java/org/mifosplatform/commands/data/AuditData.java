/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.data;

import org.joda.time.DateTime;

/**
 * Immutable data object representing client data.
 */
public final class AuditData {

	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String actionName;
	@SuppressWarnings("unused")
	private final String entityName;
	@SuppressWarnings("unused")
	private final Long resourceId;
	@SuppressWarnings("unused")
	private final Long subresourceId;
	@SuppressWarnings("unused")
	private final String maker;
	@SuppressWarnings("unused")
	private final DateTime madeOnDate;
	@SuppressWarnings("unused")
	private final String checker;
	@SuppressWarnings("unused")
	private final DateTime checkedOnDate;
	@SuppressWarnings("unused")
	private final String processingResult;
	@SuppressWarnings("unused")
	private final String commandAsJson;
	@SuppressWarnings("unused")
	private final String officeName;
	@SuppressWarnings("unused")
	private final String groupLevelName;
	@SuppressWarnings("unused")
	private final String groupName;
	@SuppressWarnings("unused")
	private final String clientName;
	@SuppressWarnings("unused")
	private final String loanAccountNo;
	@SuppressWarnings("unused")
	private final String savingsAccountNo;

	public AuditData(final Long id, final String actionName,
			final String entityName, final Long resourceId,
			final Long subresourceId, final String maker,
			final DateTime madeOnDate, final String checker,
			final DateTime checkedOnDate, final String processingResult,
			final String commandAsJson, final String officeName,
			final String groupLevelName, final String groupName,
			final String clientName, final String loanAccountNo,
			final String savingsAccountNo) {

		this.id = id;
		this.actionName = actionName;
		this.entityName = entityName;
		this.resourceId = resourceId;
		this.subresourceId = subresourceId;
		this.maker = maker;
		this.madeOnDate = madeOnDate;
		this.checker = checker;
		this.checkedOnDate = checkedOnDate;
		this.commandAsJson = commandAsJson;
		this.processingResult = processingResult;
		this.officeName = officeName;
		this.groupLevelName = groupLevelName;
		this.groupName = groupName;
		this.clientName = clientName;
		this.loanAccountNo = loanAccountNo;
		this.savingsAccountNo = savingsAccountNo;
	}
}