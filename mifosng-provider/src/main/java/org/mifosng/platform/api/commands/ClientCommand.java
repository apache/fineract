package org.mifosng.platform.api.commands;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class ClientCommand {

	private Long id;
	private String firstname;
	private String lastname;
	private String clientOrBusinessName;
	private Long officeId;
	private String externalId;
	
	private String dateFormat;
	private String joiningDate;
	private LocalDate joiningLocalDate;

	protected ClientCommand() {
		//
	}

	public ClientCommand(final String firstname, final String lastname, final String clientOrBusinessName, final Long officeId, final LocalDate joiningDate) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.clientOrBusinessName = clientOrBusinessName;
		this.officeId = officeId;
		this.joiningLocalDate = joiningDate;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(final String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(final String lastname) {
		this.lastname = lastname;
	}

	public Long getOfficeId() {
		return this.officeId;
	}

	public void setOfficeId(final Long officeId) {
		this.officeId = officeId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJoiningDate() {
		return joiningDate;
	}

	public void setJoiningDate(String joiningDate) {
		this.joiningDate = joiningDate;
	}

	public LocalDate getJoiningLocalDate() {
		return joiningLocalDate;
	}

	public void setJoiningLocalDate(LocalDate joiningLocalDate) {
		this.joiningLocalDate = joiningLocalDate;
	}

	public String getClientOrBusinessName() {
		return clientOrBusinessName;
	}

	public void setClientOrBusinessName(String clientOrBusinessName) {
		this.clientOrBusinessName = clientOrBusinessName;
	}
}