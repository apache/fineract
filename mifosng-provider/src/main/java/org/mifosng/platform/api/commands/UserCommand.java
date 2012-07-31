package org.mifosng.platform.api.commands;

import java.io.Serializable;
import java.util.Set;

/**
 * Immutable command for creating or updating details of a User.
 */
public class UserCommand implements Serializable {

	private final Long id;
	private final String username;
	private final String firstname;
	private final String lastname;
	private final String password;
	private final String repeatPassword;
	private final String email;
	private final Long officeId;
	
	private final String[] notSelectedRoles;
	private final String[] roles;
	
	private final Set<String> modifiedParameters;

	public UserCommand(final Set<String> modifiedParameters, final Long id, 
			final String username, final String firstname, final String lastname, 
			final String password, final String repeatPassword, final String email, 
			final Long officeId, final String[] notSelectedRoles, final String[] roles) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.password = password;
		this.repeatPassword = repeatPassword;
		this.email = email;
		this.officeId = officeId;
		this.notSelectedRoles = notSelectedRoles;
		this.roles = roles;
	}
	
	public String getDisplayName() {
		return new StringBuilder(this.lastname).append(", ").append(this.firstname).toString();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getPassword() {
		return password;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public String getEmail() {
		return email;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public String[] getNotSelectedRoles() {
		return notSelectedRoles;
	}

	public String[] getRoles() {
		return roles;
	}

	public boolean isUsernameChanged() {
		return this.modifiedParameters.contains("username");
	}
	
	public boolean isFirstnameChanged() {
		return this.modifiedParameters.contains("firstname");
	}
	
	public boolean isLastnameChanged() {
		return this.modifiedParameters.contains("lastname");
	}
	
	public boolean isEmailChanged() {
		return this.modifiedParameters.contains("email");
	}
	
	public boolean isPasswordChanged() {
		return this.modifiedParameters.contains("password") && this.modifiedParameters.contains("repeatPassword");
	}
	
	public boolean isOfficeChanged() {
		return this.modifiedParameters.contains("officeId");
	}
	
	public boolean isRolesChanged() {
		return this.modifiedParameters.contains("roles");
	}
}