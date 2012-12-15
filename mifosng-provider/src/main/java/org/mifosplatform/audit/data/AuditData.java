package org.mifosplatform.audit.data;

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
	private final String apiOperation;
	@SuppressWarnings("unused")
	private final String resource;
	@SuppressWarnings("unused")
	private final Long resourceId;
	@SuppressWarnings("unused")
	private final String subResource;
	@SuppressWarnings("unused")
	private final Long subResourceId;
	@SuppressWarnings("unused")
	private final String maker;
	@SuppressWarnings("unused")
	private final DateTime madeOnDate;
	@SuppressWarnings("unused")
	private final String checker;
	@SuppressWarnings("unused")
	private final DateTime checkedOnDate;
	@SuppressWarnings("unused")
	private final String commandAsJson;

	public AuditData(final Long id, final String actionName,
			final String entityName, final String apiOperation,
			final String resource, final Long resourceId,
			final String subResource, final Long subResourceId,
			final String maker, final DateTime madeOnDate,
			final String checker, final DateTime checkedOnDate,
			final String commandAsJson) {

		this.id = id;
		this.actionName = actionName;
		this.entityName = entityName;
		this.apiOperation = apiOperation;
		this.resource = resource;
		this.resourceId = resourceId;
		this.subResource = subResource;
		this.subResourceId = subResourceId;
		this.maker = maker;
		this.madeOnDate = madeOnDate;
		this.checker = checker;
		this.checkedOnDate = checkedOnDate;
		this.commandAsJson = commandAsJson;
	}
}