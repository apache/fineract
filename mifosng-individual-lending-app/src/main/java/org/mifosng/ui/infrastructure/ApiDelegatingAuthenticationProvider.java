package org.mifosng.ui.infrastructure;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.platform.api.data.AuthenticatedUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * A {@link AbstractUserDetailsAuthenticationProvider} which is responsible for delegating authentication to the mifos platform API.
 * 
 *  If successful, the permissions associated with the authenticated user are returned. As this class is used as a hook into spring security,
 *  a proper authentication object is persisted for this client applications {@link SecurityContext}.
 */
public class ApiDelegatingAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private final RestTemplate restTemplate;
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public ApiDelegatingAuthenticationProvider(final RestTemplate restTemplate, final ApplicationConfigurationService applicationConfigurationService) {
		this.restTemplate = restTemplate;
		this.applicationConfigurationService = applicationConfigurationService;
	}
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		
		try {
			String password = (String) authentication.getCredentials();
			
			String platformApiUrl = this.applicationConfigurationService.retrievePlatformApiUrl();
			String authUrlExtension = "authentication?username=" + username + "&password=" + password;
			StringBuilder authenticationApiUrl = new StringBuilder(platformApiUrl).append(authUrlExtension);
			
			URI restUri = URI.create(authenticationApiUrl.toString());
			ResponseEntity<AuthenticatedUserData> s = this.restTemplate.postForEntity(restUri, authRequest(), AuthenticatedUserData.class);
			
			UserDetails userDetails = null;
			AuthenticatedUserData authenticatedUserData = s.getBody();
			if (authenticatedUserData.isAuthenticated()) {
				
				Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
				for (String permission : authenticatedUserData.getPermissions()) {
					authorities.add(new SimpleGrantedAuthority(permission));
				}
				User user = new User(authenticatedUserData.getUsername(), "[not needed]", authorities);
				userDetails = new BasicAuthUserDetails(user, authenticatedUserData.getBase64EncodedAuthenticationKey(), platformApiUrl, authenticatedUserData.getUserId());
			}
			
			return userDetails;
		} catch (HttpClientErrorException clientErrorException) {
			if (clientErrorException.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
				throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
			}
			
			throw clientErrorException;
		}
	}
	
	private HttpEntity<Object> authRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/json");
		requestHeaders.set("Content-Type", "application/json");

		HttpEntity<Object> requestEntity = new HttpEntity<Object>(requestHeaders);
		return requestEntity;
	}
}