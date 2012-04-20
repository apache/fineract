package org.mifosng.oauth;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth.common.OAuthConsumerParameter;
import org.springframework.security.oauth.provider.OAuthProviderSupport;
import org.springframework.security.oauth.provider.filter.CoreOAuthProviderSupport;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenImpl;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;
import org.springframework.web.filter.GenericFilterBean;

public class OAuthParametersAwareSecurityContextPerRequestFilter extends
		GenericFilterBean {

	private OAuthProviderSupport providerSupport = new CoreOAuthProviderSupport();
	
	private final OAuthProviderTokenServices tokenServices;

	public OAuthParametersAwareSecurityContextPerRequestFilter(OAuthProviderTokenServices tokenServices) {
		this.tokenServices = tokenServices;
	}
	 
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		Map<String, String> oauthParams = this.providerSupport.parseParameters(request);
		
		String anonymousUserHash = "" + "anonymousUser".hashCode();
		Collection<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
		Authentication authentication = new AnonymousAuthenticationToken(anonymousUserHash, "anonymousUser", authorities);

		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);

		if (oauthTokenExists(oauthParams)) {
			String oauthToken = retrieveOAuthToken(oauthParams);
			OAuthProviderTokenImpl token = (OAuthProviderTokenImpl) this.tokenServices.getToken(oauthToken);
			
			if (token.getUserAuthentication() != null) {
				context.setAuthentication(token.getUserAuthentication());
			}
		} else {
			String requestToken = request.getParameter("requestToken");
			if (StringUtils.isNotBlank(requestToken)) {
				OAuthProviderTokenImpl token = (OAuthProviderTokenImpl) this.tokenServices.getToken(requestToken);
				
				if (token.getUserAuthentication() != null) {
					context.setAuthentication(token.getUserAuthentication());
				}
			}
		}

		chain.doFilter(request, response);
	}

	private String retrieveOAuthToken(Map<String, String> oauthParams) {
		return oauthParams.get(OAuthConsumerParameter.oauth_token.toString());
	}

	private boolean oauthTokenExists(Map<String, String> oauthParams) {
		return oauthParams.containsKey(OAuthConsumerParameter.oauth_token.toString());
	}

}
