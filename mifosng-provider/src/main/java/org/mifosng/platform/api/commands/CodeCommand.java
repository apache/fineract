package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a Code in mifos.
 */
public class CodeCommand {

	private final Long id;
	private final String codeName;

	private final Set<String> modifiedParameters;

	public CodeCommand(final Set<String> modifiedParameters, final Long id, final String codeName) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.codeName = codeName;
	}

	public Long getId() {
		return id;
	}

	public String getCodeName() {
		return codeName;
	}
	
	public boolean isNameChanged() {
		return this.modifiedParameters.contains("codename");
	}

}