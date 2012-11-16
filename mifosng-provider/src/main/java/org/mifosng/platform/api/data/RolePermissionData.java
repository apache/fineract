package org.mifosng.platform.api.data;

import java.util.Collection;

public class RolePermissionData {

	private final Long id;
	private final String name;
	private final String description;

	private final Collection<PermissionUsageData> permissionUsageData;

	public RolePermissionData(final Long id, final String name,
			final String description,
			final Collection<PermissionUsageData> permissionUsageData) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.permissionUsageData = permissionUsageData;
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

	public Collection<PermissionUsageData> getPermissionUsageData() {
		return permissionUsageData;
	}

}