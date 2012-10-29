package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

/**
 * FIXME - KW - make ClientData immutable without needs for getter/setters as is mostly intended for conversion back to JSON.
 */
public class ClientData {

	private final Long id;
	private final String firstname;
	private final String lastname;
	private final String clientOrBusinessName;
	private final String displayName;
	private final Long officeId;
	private final String officeName;
	private final String externalId;
	private final LocalDate joinedDate;
	private final String imageKey;
	private boolean imagePresent;

	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();
	
	public static ClientData clientIdentifier(
			final Long id, 
			final String firstname,
			final String lastname, 
			final Long officeId, 
			final String officeName) {
		
		StringBuilder displayNameBuilder = new StringBuilder(firstname);
		if (StringUtils.isNotBlank(displayNameBuilder.toString())) {
			displayNameBuilder.append(' ');
		}
		displayNameBuilder.append(lastname);
		
		return new ClientData(officeId, officeName, id, firstname, lastname, displayNameBuilder.toString(), null, null, null);
	}

	public ClientData(final Long officeId, final String officeName,
			final Long id, final String firstname, final String lastname,
			final String displayName, final String externalId,
			final LocalDate joinedDate,
			final String imageKey) {
		this.officeId = officeId;
		this.officeName = officeName;
		this.id = id;
		this.firstname = firstname;
		this.displayName = displayName;

		/*** unset last name for business name **/
		if (StringUtils.isBlank(this.firstname)) {
			this.lastname = null;
			this.clientOrBusinessName = lastname;
		} else {
			this.lastname = lastname;
			this.clientOrBusinessName = null;
		}

		this.externalId = externalId;
		this.joinedDate = joinedDate;
		this.imageKey = imageKey;
		if (imageKey == null) {
			imagePresent = false;
		}else{
			imagePresent = true;
		}
		
	}

	public ClientData(final Long officeId, final LocalDate joinedDate,
			final List<OfficeLookup> offices) {
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
		this.imageKey = null;
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

	public String displayName() {
		return this.displayName;
	}

	public String officeName() {
		return this.officeName;
	}

	public String getImageKey() {
		return imageKey;
	}

	public boolean isImagePresent() {
		return imagePresent;
	}

	public void setImagePresent(boolean imagePresent) {
		this.imagePresent = imagePresent;
	}
	
	
	
	
}