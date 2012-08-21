package org.mifosng.platform.api.data;

import java.util.Collection;

/**
 * Immutable data object for authentication.
 */
public class AuthenticatedUserData {

	private final String username;
	private final Long userId;
	private final String base64EncodedAuthenticationKey;
	private final boolean authenticated;
	private final Collection<String> permissions;

	public AuthenticatedUserData(final String username, final Collection<String> permissions) {
		this.username = username;
		this.userId = null;
		this.base64EncodedAuthenticationKey = null;
		this.authenticated = false;
		this.permissions = permissions;
	}

	public AuthenticatedUserData(final String username, final Collection<String> permissions, final Long userId, final String base64EncodedAuthenticationKey) {
		this.username = username;
		this.userId = userId;
		this.base64EncodedAuthenticationKey = base64EncodedAuthenticationKey;
		this.authenticated = true;
		this.permissions = permissions;
	}

	public String getUsername() {
		return username;
	}

	public Long getUserId() {
		return userId;
	}

	public String getBase64EncodedAuthenticationKey() {
		return base64EncodedAuthenticationKey;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public Collection<String> getPermissions() {
		return permissions;
	}
}