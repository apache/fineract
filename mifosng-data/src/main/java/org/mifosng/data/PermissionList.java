package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PermissionList {

	private Collection<PermissionData> permissions = new ArrayList<PermissionData>();

	protected PermissionList() {
		//
	}

	public PermissionList(final Collection<PermissionData> permissions) {
		this.permissions = permissions;
	}

	public Collection<PermissionData> getPermissions() {
		return this.permissions;
	}

	public void setPermissions(final Collection<PermissionData> permissions) {
		this.permissions = permissions;
	}
}