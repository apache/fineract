package org.mifosplatform.audit.api;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing client data.
 */
final public class AuditData {

	private final Long id;
	private final String apiOperation;
	private final String resource;
	private final Long resourceId;
	private final String maker;
	private final LocalDate madeOnDate;
	private final String checker;
	private final LocalDate checkedOnDate;
	private final String commandAsJson;

	public AuditData(final Long id, final String apiOperation,
			final String resource, final Long resourceId, final String maker,
			final LocalDate madeOnDate, final String checker,
			final LocalDate checkedOnDate, final String commandAsJson) {

		this.id = id;
		this.apiOperation = apiOperation;
		this.resource = resource;
		this.resourceId = resourceId;
		this.maker = maker;
		this.madeOnDate = madeOnDate;
		this.checker = checker;
		this.checkedOnDate = checkedOnDate;
		this.commandAsJson = commandAsJson;
	}

	public Long getId() {
		return this.id;
	}

	public String getApiOperation() {
		return this.apiOperation;
	}

	public String getResource() {
		return this.resource;
	}

	public Long getResourceId() {
		return this.resourceId;
	}

	public String getMaker() {
		return this.maker;
	}

	public LocalDate getMadeOnDate() {
		return this.madeOnDate;
	}

	public String getChecker() {
		return this.checker;
	}

	public LocalDate getCheckedOnDate() {
		return this.checkedOnDate;
	}

	public String getCommandAsJson() {
		return this.commandAsJson;
	}

}