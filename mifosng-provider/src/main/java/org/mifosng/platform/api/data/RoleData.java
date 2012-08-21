package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Immutable data object for role data.
 */
public class RoleData {

	private final Long id;
	private final String name;
	private final String description;
	
	private final Collection<PermissionData> availablePermissions;
	private final Collection<PermissionData> selectedPermissions;

	public RoleData(final Long id, final String name, final String description, final Collection<PermissionData> selectedPermissions) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.availablePermissions = new ArrayList<PermissionData>();
		this.selectedPermissions = new ArrayList<PermissionData>(selectedPermissions);
	}
	
	public RoleData(final Collection<PermissionData> availablePermissions, final Collection<PermissionData> selectedPermissions) {
		this.id = null;
		this.name = null;
		this.description = null;
		this.availablePermissions = new ArrayList<PermissionData>(availablePermissions);
		this.selectedPermissions = new ArrayList<PermissionData>(selectedPermissions);
	}

	public RoleData(RoleData role, final Collection<PermissionData> availablePermissions) {
		this.id = role.getId();
		this.name = role.getName();
		this.description = role.getDescription();
		this.availablePermissions = new ArrayList<PermissionData>(availablePermissions);
		this.selectedPermissions = role.getSelectedPermissions();
	}

	@Override
	public boolean equals(final Object obj) {
		RoleData role = (RoleData) obj;
		return this.id.equals(role.getId());
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Collection<PermissionData> getAvailablePermissions() {
		return availablePermissions;
	}

	public Collection<PermissionData> getSelectedPermissions() {
		return selectedPermissions;
	}
}