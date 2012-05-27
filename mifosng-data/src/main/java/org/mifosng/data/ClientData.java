package org.mifosng.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

@JsonIgnoreProperties({ "organisationId","organisationName"})
@JsonFilter("myFilter")
public class ClientData implements Serializable {

	private Long organisationId;
	private String organisationName;
	private Long officeId;
	private String officeName;
	private Long id;
	private String firstname;
	private String lastname;
	private String displayName;
	private String externalId;
	private LocalDate joinedDate;

	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

	public ClientData() {
		//
	}

	public ClientData(final Long organisationId, final String organisationName,
			final Long officeId, final String officeName, final Long id,
			final String firstname, final String lastname,
			final String externalId, final LocalDate joinedDate) {
		this.organisationId = organisationId;
		this.organisationName = organisationName;
		this.officeId = officeId;
		this.officeName = officeName;
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.displayName = new StringBuilder(this.firstname).append(' ')
				.append(this.lastname).toString();
		this.externalId = externalId;
		this.joinedDate = joinedDate;
	}

	public int getMaxJoinedOnOffsetFromToday() {
		return Days.daysBetween(new DateTime(),
				this.getJoinedDate().toDateMidnight().toDateTime()).getDays();
	}

	public Long getId() {
		return this.id;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public Long getOrganisationId() {
		return this.organisationId;
	}

	public String getOrganisationName() {
		return this.organisationName;
	}

	public Long getOfficeId() {
		return this.officeId;
	}

	public String getOfficeName() {
		return this.officeName;
	}

	public LocalDate getJoinedDate() {
		return this.joinedDate;
	}

	public void setOrganisationId(final Long organisationId) {
		this.organisationId = organisationId;
	}

	public void setOrganisationName(final String organisationName) {
		this.organisationName = organisationName;
	}

	public void setOfficeId(final Long officeId) {
		this.officeId = officeId;
	}

	public void setOfficeName(final String officeName) {
		this.officeName = officeName;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setFirstname(final String firstname) {
		this.firstname = firstname;
	}

	public void setLastname(final String lastname) {
		this.lastname = lastname;
	}

	public void setExternalId(final String externalId) {
		this.externalId = externalId;
	}

	public void setJoinedDate(final LocalDate joinedDate) {
		this.joinedDate = joinedDate;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}

	public void setAllowedOffices(List<OfficeLookup> allowedOffices) {
		this.allowedOffices = allowedOffices;
	}
}