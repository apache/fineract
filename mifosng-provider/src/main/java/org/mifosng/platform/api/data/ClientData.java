package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

public class ClientData {

	private final Long id;
	private final Long officeId;
	private final String officeName;
	private final String firstname;
	private final String lastname;
	private final String clientOrBusinessName;
	private final String displayName;
	private final String externalId;
	private final LocalDate joinedDate;

	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

	public ClientData(final Long officeId, final String officeName,
			final Long id, final String firstname, final String lastname,
			final String externalId, final LocalDate joinedDate) {
		this.officeId = officeId;
		this.officeName = officeName;
		this.id = id;
		this.firstname = firstname;

		StringBuilder nameBuilder = new StringBuilder(this.firstname);
		if (StringUtils.isNotBlank(nameBuilder.toString())) {
			nameBuilder.append(' ');
		}
		nameBuilder.append(lastname);

		this.displayName = nameBuilder.toString();
		if (StringUtils.isBlank(this.firstname)) {
			this.lastname = null;
			this.clientOrBusinessName = nameBuilder.toString();
		} else {
			this.lastname = lastname;
			this.clientOrBusinessName = null;
		}
		
		this.externalId = externalId;
		this.joinedDate = joinedDate;
	}

	public ClientData(final Long officeId, final LocalDate joinedDate, final List<OfficeLookup> offices) {
		this.id = null;
		this.officeId = officeId;
		this.officeName = null;
		this.firstname = null;
		this.lastname = null;
		this.clientOrBusinessName = null;
		this.displayName = null;
		this.externalId = null;
		this.joinedDate = joinedDate;
		this.allowedOffices = offices;
	}

	public Long getId() {
		return id;
	}
	
	public Long getOfficeId() {
		return officeId;
	}

	public String getOfficeName() {
		return officeName;
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

	public String getDisplayName() {
		return displayName;
	}

	public String getExternalId() {
		return externalId;
	}

	public LocalDate getJoinedDate() {
		return joinedDate;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}

	public void setAllowedOffices(List<OfficeLookup> allowedOffices) {
		this.allowedOffices = allowedOffices;
	}
}