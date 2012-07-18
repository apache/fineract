package org.mifosng.platform.api.commands;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class ClientCommand {

	private final Long id;
	private final String externalId;
	private final String firstname;
	private final String lastname;
	private final String clientOrBusinessName;
	private final Long officeId;
	private final LocalDate joiningDate;

	public ClientCommand(final Long id, final String externalId, final String firstname, final String lastname, final String clientOrBusinessName, 
			final Long officeId, final LocalDate joiningDate) {
		this.id = id;
		this.externalId = externalId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.clientOrBusinessName = clientOrBusinessName;
		this.officeId = officeId;
		this.joiningDate = joiningDate;
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
}