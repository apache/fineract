package org.mifosng.oauth;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ConsumerUserDetails implements UserDetails {

	private final String username;
	private final Collection<GrantedAuthority> authorities;

	public ConsumerUserDetails(String username, Collection<GrantedAuthority> authorities) {
		this.username = username;
		this.authorities = authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public boolean hasNoReportingAuthority() {
		return !hasReportingAuthority();
	}

	private boolean hasReportingAuthority() {
		SimpleGrantedAuthority reportingAuthority = new SimpleGrantedAuthority("REPORTING_SUPER_USER_ROLE");
		return this.authorities.contains(reportingAuthority);
	}

	public boolean hasNoAuthorityToSumitLoanApplication() {
		return !hasAuthorityToSumitLoanApplication();
	}
	
	private boolean hasAuthorityToSumitLoanApplication() {
		return containsAnyOf(portfolioAllAuthority(), sumbitLoanApplicationAuthority(), sumbitHistoricLoanApplicationAuthority());
	}

	private boolean containsAnyOf(SimpleGrantedAuthority... authorities) {
		boolean match = false;
		for (SimpleGrantedAuthority authority : authorities) {
			match = this.authorities.contains(authority);
			if (match) {
				break;
			}
		}
		return match;
	}

	private SimpleGrantedAuthority portfolioAllAuthority() {
		return new SimpleGrantedAuthority("PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}
	
	private SimpleGrantedAuthority sumbitLoanApplicationAuthority() {
		return new SimpleGrantedAuthority("CAN_SUBMIT_NEW_LOAN_APPLICATION_ROLE");
	}
	
	private SimpleGrantedAuthority sumbitHistoricLoanApplicationAuthority() {
		return new SimpleGrantedAuthority("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE");
	}
}
