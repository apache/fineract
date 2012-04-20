package org.mifosng.oauth;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mifosng.configuration.OAuthProviderDetails;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.security.oauth.consumer.OAuthSecurityContextImpl;
import org.springframework.security.oauth.consumer.rememberme.OAuthRememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * Logout Filter process before oauth filters are applied so need create OAuthSercurityContext and populate with 'remembered tokens' first.
 */
public class OAuthLogoutHandler implements LogoutHandler {

	private final SecurityContextLogoutHandler delegate;
	private final MifosNgProtectedResourceDetailsService protectedResourceDetailsService;
	private final OAuthRememberMeServices oAuthRememberMeServices;

	public OAuthLogoutHandler(final SecurityContextLogoutHandler delegate, final MifosNgProtectedResourceDetailsService protectedResourceDetailsService,
			final OAuthRememberMeServices oAuthRememberMeServices) {
		this.delegate = delegate;
		this.protectedResourceDetailsService = protectedResourceDetailsService;
		this.oAuthRememberMeServices = oAuthRememberMeServices;
	}
	
	@Override
	public void logout(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) {

		this.delegate.logout(request, response, authentication);
		
		OAuthSecurityContextImpl context = new OAuthSecurityContextImpl();
		context.setDetails(request);

		Map<String, OAuthConsumerToken> rememberedTokens = this.oAuthRememberMeServices.loadRememberedTokens(request, response);
		Map<String, OAuthConsumerToken> accessTokens = new TreeMap<String, OAuthConsumerToken>();
		if (rememberedTokens != null) {
			for (Map.Entry<String, OAuthConsumerToken> tokenEntry : rememberedTokens.entrySet()) {
				OAuthConsumerToken token = tokenEntry.getValue();
				if (token != null) {
					if (token.isAccessToken()) {
						accessTokens.put(tokenEntry.getKey(), token);
					}
				}
			}
		}

		context.setAccessTokens(accessTokens);
		OAuthSecurityContextHolder.setContext(context);

		if (!accessTokens.isEmpty()) {
			MifosNgOauthRestTemplate oauthRestTemplate = new MifosNgOauthRestTemplate(
					protectedResourceDetailsService);

			OAuthProviderDetails providerDetails = this.protectedResourceDetailsService
					.getApplicationConfigurationService()
					.retrieveOAuthProviderDetails();

			URI restUri = URI.create(providerDetails.getProviderBaseUrl()
					.concat("api/protected/user/signout"));

			oauthRestTemplate.exchange(restUri, HttpMethod.GET, emptyRequest(),
					null);
		}

		OAuthSecurityContextHolder.setContext(null);
	}
	
	
	private HttpEntity<Object> emptyRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<Object> requestEntity = new HttpEntity<Object>(
				requestHeaders);
		return requestEntity;
	}
}