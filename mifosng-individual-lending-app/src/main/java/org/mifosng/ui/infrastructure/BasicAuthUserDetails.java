package org.mifosng.ui.infrastructure;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class BasicAuthUserDetails implements UserDetails {

	private final User user;
	private final String basicAuthenticationKey;
	private final String fullApiUrl;
	private final Long userId;

	public BasicAuthUserDetails(User user, String basicAuthenticationKey, String fullApiUrl, Long userId) {
		this.user = user;
		this.basicAuthenticationKey = basicAuthenticationKey;
		this.fullApiUrl = fullApiUrl;
		this.userId = userId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.user.getAuthorities();
	}

	@Override
	public String getPassword() {
		return this.getPassword();
	}

	@Override
	public String getUsername() {
		return this.user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return this.user.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.user.isAccountNonExpired();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.user.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return this.user.isEnabled();
	}

	public String getBasicAuthenticationKey() {
		return basicAuthenticationKey;
	}

	public String getFullApiUrl() {
		return this.fullApiUrl;
	}

	public Long getUserId() {
		return userId;
	}
}