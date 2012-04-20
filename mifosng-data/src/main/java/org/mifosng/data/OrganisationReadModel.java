package org.mifosng.data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement(name = "organisation")
public class OrganisationReadModel implements Serializable {

	private Long id;
	private String name;
	private String contactName;
	private LocalDate openingDate;

	public OrganisationReadModel() {
		//
	}

	public OrganisationReadModel(final Long id, final String name,
			final String contactName, final LocalDate openingDate) {
		this.id = id;
		this.name = name;
		this.contactName = contactName;
		this.openingDate = openingDate;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getContactName() {
		return this.contactName;
	}

	public LocalDate getOpeningDate() {
		return this.openingDate;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setContactName(final String contactName) {
		this.contactName = contactName;
	}

	public void setOpeningDate(final LocalDate openingDate) {
		this.openingDate = openingDate;
	}
}