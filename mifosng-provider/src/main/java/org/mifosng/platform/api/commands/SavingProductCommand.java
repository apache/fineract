package org.mifosng.platform.api.commands;

import java.util.Set;

public class SavingProductCommand {
	private final Long id;
	private final String name;
	private final String description;

	private final Set<String> modifiedParameters;

	public SavingProductCommand(final Set<String> modifiedParameters, final Long id, final String name, final String description) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.modifiedParameters = modifiedParameters;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isNameChanged() {
		return this.modifiedParameters.contains("name");
	}

	public boolean isDescriptionChanged() {
		return this.modifiedParameters.contains("description");
	}

}
