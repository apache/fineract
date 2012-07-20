package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

public class AppUserData {

	private Long id;
	private Long officeId;
	private String officeName;
	private String username;
	private String firstname;
	private String lastname;
	private String email;

	private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

	private List<RoleData> availableRoles = new ArrayList<RoleData>();
	private List<RoleData> selectedRoles = new ArrayList<RoleData>();

	public AppUserData() {
		//
	}

	public AppUserData(final Long id, final String username,
			final String email, final Long officeId,
			final String officeName) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.officeId = officeId;
		this.officeName = officeName;
	}

	public Long getId() {
		return this.id;
	}

	public String getUsername() {
		return this.username;
	}

	public String getEmail() {
		return this.email;
	}

	public Long getOfficeId() {
		return this.officeId;
	}

	public String getOfficeName() {
		return this.officeName;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public void setOfficeId(final Long officeId) {
		this.officeId = officeId;
	}

	public void setOfficeName(final String officeName) {
		this.officeName = officeName;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}

	public void setAllowedOffices(List<OfficeLookup> allowedOffices) {
		this.allowedOffices = allowedOffices;
	}

	public List<RoleData> getAvailableRoles() {
		return availableRoles;
	}

	public void setAvailableRoles(List<RoleData> availableRoles) {
		this.availableRoles = availableRoles;
	}

	public List<RoleData> getSelectedRoles() {
		return selectedRoles;
	}

	public void setSelectedRoles(List<RoleData> selectedRoles) {
		this.selectedRoles = selectedRoles;
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
}