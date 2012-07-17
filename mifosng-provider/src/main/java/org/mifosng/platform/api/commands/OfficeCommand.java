package org.mifosng.platform.api.commands;

import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command for creating or updating details of a office.
 */
public class OfficeCommand {

	private final Long id;
	private final String name;
	private final String externalId;
	private final LocalDate openingDate;
	private final Long parentId;

	private final Set<String> modifiedParameters;
	
	public OfficeCommand(Set<String> modifiedParameters, final Long id, final String officeName,
			final String externalId, final Long parentId,
			final LocalDate openingDate) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.name = officeName;
		this.externalId = externalId;
		this.parentId = parentId;
		this.openingDate = openingDate;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getExternalId() {
		return externalId;
	}

	public LocalDate getOpeningDate() {
		return openingDate;
	}

	public Long getParentId() {
		return parentId;
	}
	
	public boolean isNameChanged() {
		return this.modifiedParameters.contains("name");
	}

	public boolean isExternalIdChanged() {
		return this.modifiedParameters.contains("externalId");
	}

	public boolean isOpeningDateChanged() {
		return this.modifiedParameters.contains("openingDate");
	}

	public boolean isParentChanged() {
		return this.modifiedParameters.contains("parentId");
	}
}