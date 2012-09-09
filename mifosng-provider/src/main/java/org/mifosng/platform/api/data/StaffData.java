package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StaffData implements Serializable {

	private Long id;
	private String firstname;
	private String lastname;
	private String displayName;
	private final Long officeId;
	private final String officeName;
	private final Boolean loanOfficerFlag;

	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

	public StaffData(final Long id, final String firstname,
			final String lastname, final String displayName,
			final Long officeId, final String officeName,
			final Boolean loanOfficerFlag) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.displayName = displayName;
		this.officeName = officeName;
		this.loanOfficerFlag = loanOfficerFlag;
		this.officeId = officeId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public String getOfficeName() {
		return officeName;
	}

	public Boolean getLoanOfficerFlag() {
		return loanOfficerFlag;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}

	public void setAllowedOffices(List<OfficeLookup> allowedOffices) {
		this.allowedOffices = allowedOffices;
	}

}