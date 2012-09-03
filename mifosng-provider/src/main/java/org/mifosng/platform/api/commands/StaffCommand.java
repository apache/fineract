package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a staff member.
 */
public class StaffCommand {

	private final Long id;
	private final String firstName;
	private final String lastName;

	private final Set<String> modifiedParameters;

	public StaffCommand(final Set<String> modifiedParameters, final Long id,
			final String firstName, final String lastName) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;

	}

	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public boolean isFirstNameChanged() {
		return this.modifiedParameters.contains("firstname");
	}

	public boolean isLastNameChanged() {
		return this.modifiedParameters.contains("lastname");
	}

}