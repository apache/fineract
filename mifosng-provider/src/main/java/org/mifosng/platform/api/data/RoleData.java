package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonFilter;

@JsonIgnoreProperties({ "orgId" })
@JsonFilter("roleFilter")
public class RoleData {

	private Long id;
	private String name;
	private String description;
	
	private Collection<PermissionData> availablePermissions = new ArrayList<PermissionData>();
	private Collection<PermissionData> selectedPermissions = new ArrayList<PermissionData>();

	public RoleData() {
		//
	}

	public RoleData(final Long id, final String name, final String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	@Override
	public boolean equals(Object obj) {
		RoleData role = (RoleData) obj;
		return this.id.equals(role.getId());
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Collection<PermissionData> getAvailablePermissions() {
		return availablePermissions;
	}

	public void setAvailablePermissions(
			Collection<PermissionData> availablePermissions) {
		this.availablePermissions = availablePermissions;
	}

	public Collection<PermissionData> getSelectedPermissions() {
		return selectedPermissions;
	}

	public void setSelectedPermissions(
			Collection<PermissionData> selectedPermissions) {
		this.selectedPermissions = selectedPermissions;
	}
}