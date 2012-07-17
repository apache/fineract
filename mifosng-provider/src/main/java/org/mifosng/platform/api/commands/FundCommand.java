package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a fund.
 */
public class FundCommand {

	private final Long id;
	private final String name;
	private final String externalId;
	
	private final Set<String> modifiedParameters;

	public FundCommand(final Set<String> modifiedParameters, final Long id, final String fundName, final String externalId) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.name = fundName;
		this.externalId = externalId;
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
	
	public boolean isNameChanged() {
		return this.modifiedParameters.contains("name");
	}

	public boolean isExternalIdChanged() {
		return this.modifiedParameters.contains("externalId");
	}
}