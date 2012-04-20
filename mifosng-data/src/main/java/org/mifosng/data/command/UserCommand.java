package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * create or update user details command.
 */
@XmlRootElement
public class UserCommand {

	private Long id;
	private String username;
	private String firstname;
	private String lastname;
	private String password;
	private String email;
	private String[] roleIds;
	private Long officeId;

	public UserCommand() {
		//
	}
	
	public UserCommand(final String username, final String firstname, final String lastname, final String password,
			final String email, final String[] roleIds, final Long officeId) {
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.password = password;
		this.email = email;
		this.roleIds = roleIds;
		this.officeId = officeId;
	}
	
	public UserCommand(final String username, final String firstname, final String lastname, final String email) {
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public UserCommand(final String username, final String password,
			final String email, final String[] roleIds, final Long officeId) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.roleIds = roleIds;
		this.officeId = officeId;
	}
	
	public String getDisplayName() {
		return new StringBuilder(this.lastname).append(", ").append(this.firstname).toString();
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String[] getRoleIds() {
		return this.roleIds;
	}

	public void setRoleIds(final String[] roleIds) {
		this.roleIds = roleIds;
	}

	public Long getOfficeId() {
		return this.officeId;
	}

	public void setOfficeId(final Long officeId) {
		this.officeId = officeId;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}