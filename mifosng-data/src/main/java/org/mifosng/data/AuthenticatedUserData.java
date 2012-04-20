package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthenticatedUserData {

	private String username;
	private Collection<String> permissions = new ArrayList<String>();

	protected AuthenticatedUserData() {
		//
	}

	public AuthenticatedUserData(final Collection<String> permissions) {
		this.permissions = permissions;
	}

	public Collection<String> getPermissions() {
		return this.permissions;
	}

	public void setPermissions(final Collection<String> permissions) {
		this.permissions = permissions;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}