package org.mifosng.ui.infrastructure;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.mifosng.data.AuthenticatedUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.RestTemplate;

/**
 * A {@link AbstractUserDetailsAuthenticationProvider} which is responsible for delegating authentication to the mifos platform API. 
 */
public class ApiDelegatingAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private final RestTemplate restTemplate;

	@Autowired
	public ApiDelegatingAuthenticationProvider(final RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		
		String password = (String) authentication.getCredentials();
		
		URI restUri = URI.create("http://localhost:8080/mifosng-provider/api/v1/authentication?username=" + username + "&password=" + password);
		ResponseEntity<AuthenticatedUserData> s = this.restTemplate.postForEntity(restUri, authRequest(), AuthenticatedUserData.class);
		
		UserDetails userDetails = null;
		AuthenticatedUserData authenticatedUserData = s.getBody();
		if (authenticatedUserData.isAuthenticated()) {
			
			Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
			for (String permission : authenticatedUserData.getPermissions()) {
				authorities.add(new SimpleGrantedAuthority(permission));
			}
			userDetails = new User(authenticatedUserData.getUsername(), "[not needed]", authorities);
		}
		
		return userDetails;
	}
	
	private HttpEntity<Object> authRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestHeaders);
		return requestEntity;
	}
}
