package org.mifosng.platform.api.data;

import java.io.Serializable;

public class StaffData implements Serializable {

	private Long id;
	private String firstname;
	private String lastname;
	private String displayName;

	public StaffData() {
		//
	}

	public StaffData(final Long id, final String firstname,
			final String lastname,final String displayName) {
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.displayName= displayName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	
}