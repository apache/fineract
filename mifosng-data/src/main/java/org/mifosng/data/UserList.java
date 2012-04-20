package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserList {

	private Collection<AppUserData> users = new ArrayList<AppUserData>();

	protected UserList() {
		//
	}

	public UserList(final Collection<AppUserData> users) {
		this.users = users;
	}

	public Collection<AppUserData> getUsers() {
		return this.users;
	}

	public void setUsers(final Collection<AppUserData> users) {
		this.users = users;
	}
}