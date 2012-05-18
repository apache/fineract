package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthenticatedUserData {

	private String username;
	private boolean authenticated = false;
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

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
}