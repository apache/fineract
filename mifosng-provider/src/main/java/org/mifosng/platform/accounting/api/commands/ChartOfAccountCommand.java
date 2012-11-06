package org.mifosng.platform.accounting.api.commands;

import java.util.Set;

/**
 * Immutable command for adding an account to chart of accounts.
 */
public class ChartOfAccountCommand {

	private final Long id;
	private final String name;
	private final Long parentId;
	private final String glCode;
	private final Boolean disabled;
	private final Boolean manualEntriesAllowed;
	private final String category;
	private final String ledgerType;
	private final String description;
	
	private final Set<String> requestParameters;
	
	public ChartOfAccountCommand(final Set<String> modifiedParameters,
			final Long id, final String name,
			final Long parentId, final String glCode, final Boolean disabled,
			final Boolean manualEntriesAllowed, final String category,
			final String ledgerType, final String description) {
		this.requestParameters = modifiedParameters;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.glCode = glCode;
		this.disabled = disabled;
		this.manualEntriesAllowed = manualEntriesAllowed;
		this.category = category;
		this.ledgerType = ledgerType;
		this.description = description;
	}
	
	public boolean isNamePassedOnRequest() {
		return this.requestParameters.contains("name");
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getParentId() {
		return parentId;
	}

	public String getGlCode() {
		return glCode;
	}
	
	public Boolean getDisabled() {
		return disabled;
	}

	public Boolean getManualEntriesAllowed() {
		return manualEntriesAllowed;
	}

	public String getCategory() {
		return category;
	}

	public String getLedgerType() {
		return ledgerType;
	}

	public String getDescription() {
		return description;
	}
}