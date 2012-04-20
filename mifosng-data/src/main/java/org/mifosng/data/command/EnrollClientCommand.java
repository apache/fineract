package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class EnrollClientCommand {

	private String firstname;
	private String lastname;
	private String fullname;
	private Long officeId;
	private String externalId;
	private LocalDate joiningDate;

	protected EnrollClientCommand() {
		//
	}

	public EnrollClientCommand(final String firstname, final String lastname, String fullname, final Long officeId, final LocalDate joiningDate) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.fullname = fullname;
		this.officeId = officeId;
		this.joiningDate = joiningDate;
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

	public LocalDate getJoiningDate() {
		return this.joiningDate;
	}

	public void setJoiningDate(final LocalDate joiningDate) {
		this.joiningDate = joiningDate;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
}