package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement
public class SignupCommand {

    private String          organisationName;
    private String          contactEmail;
    private String          contactName;

	// @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate openingDate = new LocalDate();

    protected SignupCommand() {
        //
    }

	public SignupCommand(final String organisationName,
			final String contactEmail, final String contactName,
			final LocalDate openingDate) {
		this.organisationName = organisationName;
		this.contactEmail = contactEmail;
		this.contactName = contactName;
		this.openingDate = openingDate;
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