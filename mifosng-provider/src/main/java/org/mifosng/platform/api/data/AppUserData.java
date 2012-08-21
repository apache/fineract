package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable data object for application user data.
 */
public class AppUserData {

	private final Long id;
	private final String username;
	private final Long officeId;
	private final String officeName;
	private final String firstname;
	private final String lastname;
	private final String email;

	private final List<OfficeLookup> allowedOffices;
	private final List<RoleData> availableRoles;
	private final List<RoleData> selectedRoles;

	public AppUserData(final Long id, final String username,
			final String email, final Long officeId,
			final String officeName, final String firstname, final String lastname, final List<RoleData> availableRoles, final List<RoleData> selectedRoles) {
		this.id = id;
		this.username = username;
		this.officeId = officeId;
		this.officeName = officeName;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		
		this.allowedOffices = new ArrayList<OfficeLookup>();
		this.availableRoles = availableRoles;
		this.selectedRoles = selectedRoles;
	}
	
	public AppUserData(AppUserData user, List<OfficeLookup> allowedOffices) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.officeId = user.getOfficeId();
		this.officeName = user.getOfficeName();
		this.firstname = user.getFirstname();
		this.lastname = user.getLastname();
		this.email = user.getEmail();
		
		this.allowedOffices = allowedOffices;
		this.availableRoles = user.getAvailableRoles();
		this.selectedRoles = user.getSelectedRoles();
	}

	public AppUserData(final List<OfficeLookup> allowedOffices, final List<RoleData> availableRoles) {
		this.id = null;
		this.username = null;
		this.officeId = null;
		this.officeName = null;
		this.firstname = null;
		this.lastname = null;
		this.email = null;
		
		this.allowedOffices = allowedOffices;
		this.availableRoles = availableRoles;
		this.selectedRoles = new ArrayList<RoleData>();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
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

	public String getEmail() {
		return email;
	}

	public List<OfficeLookup> getAllowedOffices() {
		return allowedOffices;
	}

	public List<RoleData> getAvailableRoles() {
		return availableRoles;
	}

	public List<RoleData> getSelectedRoles() {
		return selectedRoles;
	}
}