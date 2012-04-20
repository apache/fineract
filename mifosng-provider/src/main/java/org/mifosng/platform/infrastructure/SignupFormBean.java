package org.mifosng.platform.infrastructure;

import org.joda.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SignupFormBean {
	private String contactEmail;
	private String contactName;
	private String organisationName;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate openingDate = new LocalDate();

	protected SignupFormBean() {
		// no arg constructor
	}

	public String getContactEmail() {
		return this.contactEmail;
	}

	public void setContactEmail(final String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactName() {
		return this.contactName;
	}

	public void setContactName(final String contactName) {
		this.contactName = contactName;
	}

	public String getOrganisationName() {
		return this.organisationName;
	}

	public void setOrganisationName(final String organisationName) {
		this.organisationName = organisationName;
	}

	public LocalDate getOpeningDate() {
		return this.openingDate;
	}

	public void setOpeningDate(final LocalDate openingDate) {
		this.openingDate = openingDate;
	}
}