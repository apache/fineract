package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a staff member.
 */
public class StaffCommand {

	private final Long id;
	private final String firstName;
	private final String lastName;
	private final Long officeId;
	private final boolean loanOfficerFlag;

	private final Set<String> modifiedParameters;

	public StaffCommand(final Set<String> modifiedParameters, final Long id,
			final Long officeId, final String firstName, final String lastName,
			final boolean loanOfficerFlag) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.officeId = officeId;
		this.loanOfficerFlag = loanOfficerFlag;
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

	public Long getOfficeId() {
		return officeId;
	}

	public boolean isLoanOfficerFlag() {
		return loanOfficerFlag;
	}

	public boolean isFirstNameChanged() {
		return this.modifiedParameters.contains("firstname");
	}

	public boolean isLastNameChanged() {
		return this.modifiedParameters.contains("lastname");
	}

	public boolean isLoanOfficerFlagChanged() {
		return this.modifiedParameters.contains("loanOfficerFlag");
	}

	public boolean isOfficeChanged() {
		return this.modifiedParameters.contains("officeId");
	}

}