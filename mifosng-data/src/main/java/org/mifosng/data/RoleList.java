package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RoleList {

	private Collection<RoleData> roles = new ArrayList<RoleData>();

	protected RoleList() {
		//
	}

	public RoleList(final Collection<RoleData> roles) {
		this.roles = roles;
	}

	public Collection<RoleData> getRoles() {
		return this.roles;
	}

	public void setRoles(final Collection<RoleData> roles) {
		this.roles = roles;
	}
}