package org.mifosng.platform.api.commands;

import java.util.Set;

import org.joda.time.LocalDate;

/**
 *
 */
public class ClientCommand {

	private final String externalId;
	private final String firstname;
	private final String lastname;
	private final String clientOrBusinessName;
	private final Long officeId;
	private final LocalDate joiningDate;

	// made transient as dont want them to be serialized/converted to JSON
	private final transient Long id;
	private final transient boolean makerCheckerApproval;
	private final transient Set<String> parametersPassedInRequest;
	
	public ClientCommand(
			final Set<String> modifiedParameters, 
			final Long id, 
			final String externalId, 
			final String firstname, 
			final String lastname, 
			final String clientOrBusinessName, 
			final Long officeId, 
			final LocalDate joiningDate, 
			final boolean makerCheckerApproval) {
		this.parametersPassedInRequest = modifiedParameters;
		this.id = id;
		this.externalId = externalId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.clientOrBusinessName = clientOrBusinessName;
		this.officeId = officeId;
		this.joiningDate = joiningDate;
		this.makerCheckerApproval = makerCheckerApproval;
	}

	public Long getId() {
		return id;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getClientOrBusinessName() {
		return clientOrBusinessName;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public LocalDate getJoiningDate() {
		return joiningDate;
	}
	
	public boolean isFirstnameChanged() {
		return this.parametersPassedInRequest.contains("firstname");
	}
	
	public boolean isLastnameChanged() {
		return this.parametersPassedInRequest.contains("lastname");
	}
	
	public boolean isClientOrBusinessNameChanged() {
		return this.parametersPassedInRequest.contains("clientOrBusinessName");
	}
	
	public boolean isExternalIdChanged() {
		return this.parametersPassedInRequest.contains("externalId");
	}
	
	public boolean isJoiningDateChanged() {
		return this.parametersPassedInRequest.contains("joiningDate");
	}
	
	public boolean isOfficeChanged() {
		return this.parametersPassedInRequest.contains("officeId");
	}

	public boolean isApprovedByChecker() {
		return this.makerCheckerApproval;
	}
}