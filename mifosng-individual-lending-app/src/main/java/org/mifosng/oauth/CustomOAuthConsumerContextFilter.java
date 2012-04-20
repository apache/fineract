package org.mifosng.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.data.AuthenticatedUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth.common.OAuthProviderParameter;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.OAuthRequestFailedException;
import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.security.oauth.consumer.OAuthSecurityContextImpl;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.client.CoreOAuthConsumerSupport;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerContextFilter;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.client.HttpStatusCodeException;

public class CustomOAuthConsumerContextFilter extends OAuthConsumerContextFilter {
	
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	private final ApplicationConfigurationService applicationConfigurationService;
	
	@Autowired
	public CustomOAuthConsumerContextFilter(final ApplicationConfigurationService applicationConfigurationService) {
		this.applicationConfigurationService = applicationConfigurationService;
		
	}
	
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		OAuthSecurityContextImpl context = new OAuthSecurityContextImpl();
		context.setDetails(request);

		Map<String, OAuthConsumerToken> rememberedTokens = getRememberMeServices().loadRememberedTokens(request, response);
		Map<String, OAuthConsumerToken> accessTokens = new TreeMap<String, OAuthConsumerToken>();
		Map<String, OAuthConsumerToken> requestTokens = new TreeMap<String, OAuthConsumerToken>();
		if (rememberedTokens != null) {
			for (Map.Entry<String, OAuthConsumerToken> tokenEntry : rememberedTokens.entrySet()) {
				OAuthConsumerToken token = tokenEntry.getValue();
				if (token != null) {
					if (token.isAccessToken()) {
						accessTokens.put(tokenEntry.getKey(), token);
					}
					else {
						requestTokens.put(tokenEntry.getKey(), token);
					}
				}
			}
		}

		context.setAccessTokens(accessTokens);
		OAuthSecurityContextHolder.setContext(context);
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("Storing access tokens in request attribute '" + getAccessTokensRequestAttribute() + "'.");
//		}

		try {
			try {
				request.setAttribute(getAccessTokensRequestAttribute(), new ArrayList<OAuthConsumerToken>(accessTokens.values()));
				chain.doFilter(request, response);
			}
			catch (Exception e) {
				try {
					ProtectedResourceDetails resourceThatNeedsAuthorization = checkForResourceThatNeedsAuthorization(e);
					String neededResourceId = resourceThatNeedsAuthorization.getId();
					while (!accessTokens.containsKey(neededResourceId)) {
						OAuthConsumerToken token = requestTokens.remove(neededResourceId);
						if (token == null) {
							token = getTokenServices().getToken(neededResourceId);
						}

						String verifier = request.getParameter(OAuthProviderParameter.oauth_verifier.toString());
						// if the token is null OR
						// if there is NO access token and (we're not using 1.0a or the verifier is not null)
						if (token == null || (!token.isAccessToken() && (!resourceThatNeedsAuthorization.isUse10a() || verifier == null))) {
							//no token associated with the resource, start the oauth flow.
							//if there's a request token, but no verifier, we'll assume that a previous oauth request failed and we need to get a new request token.
//							if (LOG.isDebugEnabled()) {
//								LOG.debug("Obtaining request token for resource: " + neededResourceId);
//							}

							//obtain authorization.
							String callbackURL = response.encodeRedirectURL(getCallbackURL(request));
							token = getConsumerSupport().getUnauthorizedRequestToken(neededResourceId, callbackURL);
//							if (LOG.isDebugEnabled()) {
//								LOG.debug("Request token obtained for resource " + neededResourceId + ": " + token);
//							}

							//okay, we've got a request token, now we need to authorize it.
							requestTokens.put(neededResourceId, token);
							getTokenServices().storeToken(neededResourceId, token);
							
							
							try {
								String baseURL = this.applicationConfigurationService.retrieveOAuthProviderDetails().getUserAuthorizationUrl();
								StringBuilder builder = new StringBuilder(baseURL);
								char appendChar = baseURL.indexOf('?') < 0 ? '?' : '&';
								builder.append(appendChar).append("oauth_token=");
								builder.append(URLEncoder.encode(token.getValue(), "UTF-8"));
								builder.append('&').append("oauth_callback=");
								builder.append(URLEncoder.encode(callbackURL, "UTF-8"));

								String redirect = builder.toString();
										
								request.setAttribute("org.springframework.security.oauth.consumer.AccessTokenRequiredException", e);
								this.redirectStrategy.sendRedirect(request, response, redirect);
								return;
							}
							catch (UnsupportedEncodingException use) {
								throw new IllegalStateException(use);
							}
						}
						else if (!token.isAccessToken()) {
							//we have a presumably authorized request token, let's try to get an access token with it.
//							if (LOG.isDebugEnabled()) {
//								LOG.debug("Obtaining access token for resource: " + neededResourceId);
//							}

							//authorize the request token and store it.
							try {
								token = getConsumerSupport().getAccessToken(token, verifier);
							}
							finally {
								getTokenServices().removeToken(neededResourceId);
							}

//							if (LOG.isDebugEnabled()) {
//								LOG.debug("Access token " + token + " obtained for resource " + neededResourceId + ". Now storing and using.");
//							}

							getTokenServices().storeToken(neededResourceId, token);
						}

						accessTokens.put(neededResourceId, token);
						
						// hack here to detect this situation which happens once user is authenticated successfully on provider 
						// and redirected back to this client application
						String oauthAccessToken = request.getParameter("oauth_token");
						String oauthVerifier = request.getParameter("oauth_verifier");
						
						if (StringUtils.isNotBlank(oauthAccessToken) && StringUtils.isNotBlank(oauthVerifier)) {
							CoreOAuthConsumerSupport support = (CoreOAuthConsumerSupport)getConsumerSupport();
							
							MifosNgOauthRestTemplate oauthRestTemplate = new MifosNgOauthRestTemplate(support.getProtectedResourceDetailsService());
							try {
								URI restUri = URI
										.create(getBaseServerUrl().concat("api/protected/user/"
												+ oauthAccessToken));

								ResponseEntity<AuthenticatedUserData> s = oauthRestTemplate.exchange(restUri, HttpMethod.GET, emptyRequest(), AuthenticatedUserData.class);
								
								AuthenticatedUserData userData = s.getBody();
								
								SecurityContextHolder.clearContext();
								SecurityContext springSecurityContext = new SecurityContextImpl();
								
								Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
								authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
								for (String permissionCode : userData.getPermissions()) {
									authorities.add(new SimpleGrantedAuthority(permissionCode));
								}
								
								UserDetails principal = new ConsumerUserDetails(userData.getUsername(), authorities);
								Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal, principal.getAuthorities());
								springSecurityContext.setAuthentication(auth);
								
								SecurityContextHolder.setContext(springSecurityContext);
								
							} catch (HttpStatusCodeException ex) {
								System.err.println(ex.getResponseBodyAsString());
								throw ex;
							}
						}

						try {
							//try again
							if (!response.isCommitted()) {
								request.setAttribute(getAccessTokensRequestAttribute(), new ArrayList<OAuthConsumerToken>(accessTokens.values()));
								chain.doFilter(request, response);
							}
							else {
								//dang. what do we do now?
								throw new IllegalStateException("Unable to reprocess filter chain with needed OAuth2 resources because the response is already committed.");
							}
						}
						catch (Exception e1) {
							resourceThatNeedsAuthorization = checkForResourceThatNeedsAuthorization(e1);
							neededResourceId = resourceThatNeedsAuthorization.getId();
						}
					}
				}
				catch (OAuthRequestFailedException eo) {
					fail(request, response, eo);
				}
				catch (Exception ex) {
					Throwable[] causeChain = getThrowableAnalyzer().determineCauseChain(ex);
					OAuthRequestFailedException rfe = (OAuthRequestFailedException) getThrowableAnalyzer().getFirstThrowableOfType(OAuthRequestFailedException.class, causeChain);
					if (rfe != null) {
						fail(request, response, rfe);
					}
					else {
						// Rethrow ServletExceptions and RuntimeExceptions as-is
						if (ex instanceof ServletException) {
							throw (ServletException) ex;
						}
						else if (ex instanceof RuntimeException) {
							throw (RuntimeException) ex;
						}

						// Wrap other Exceptions. These are not expected to happen
						throw new RuntimeException(ex);
					}
				}
			}
		}
		finally {
			OAuthSecurityContextHolder.setContext(null);
			HashMap<String, OAuthConsumerToken> tokensToRemember = new HashMap<String, OAuthConsumerToken>();
			tokensToRemember.putAll(requestTokens);
			tokensToRemember.putAll(accessTokens);
			getRememberMeServices().rememberTokens(tokensToRemember, request, response);
		}
	}
	
	private HttpEntity<Object> emptyRequest() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Accept", "application/xml");
		requestHeaders.set("Content-Type", "application/xml");

		HttpEntity<Object> requestEntity = new HttpEntity<Object>(
				requestHeaders);
		return requestEntity;
	}

	private String getBaseServerUrl() {
		return this.applicationConfigurationService.retrieveOAuthProviderDetails().getProviderBaseUrl();
	}
}
