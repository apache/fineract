package org.mifosng.platform.infrastructure;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class BasicPasswordEncodablePlatformUser implements PlatformUser {

	private final Long id;
	private final String username;
	private final String password;

	public BasicPasswordEncodablePlatformUser(final Long id, final String username,
			final String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public Long getId() {
		return this.id;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isFirstTimeLoginRemaining() {
		return false;
	}

	@Override
	public void updateUsernamePasswordOnFirstTimeLogin(final String newUsername, final String newPasswordEncoded) {
		// dummy method
	}

	@Override
	public void updatePasswordOnFirstTimeLogin(final String newPasswordEncoded) {
		// dummy method
	}
}