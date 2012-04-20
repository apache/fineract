package org.mifosng.oauth;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth.consumer.AccessTokenRequiredException;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.OAuthSecurityContext;
import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.security.oauth.consumer.filter.OAuthConsumerProcessingFilter;

public class CustomOAuthConsumerProcessingFilter extends
		OAuthConsumerProcessingFilter {

	@Override
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		Set<String> accessTokenDeps = getAccessTokenDependencies(request,
				response, chain);
		if (!accessTokenDeps.isEmpty()) {
			Authentication authentication = SecurityContextHolder.getContext()
					.getAuthentication();
			if (isRequireAuthenticated() && !authentication.isAuthenticated()) {
				throw new InsufficientAuthenticationException(
						"An authenticated principal must be present.");
			}

			OAuthSecurityContext context = OAuthSecurityContextHolder
					.getContext();
			if (context == null) {
				throw new IllegalStateException(
						"No OAuth security context has been established. Unable to access resources.");
			}

			Map<String, OAuthConsumerToken> accessTokens = context
					.getAccessTokens();

			for (String dependency : accessTokenDeps) {
				if (!accessTokens.containsKey(dependency)) {
					throw new AccessTokenRequiredException(
							getProtectedResourceDetailsService()
									.loadProtectedResourceDetailsById(
											dependency));
				}
			}

			chain.doFilter(request, response);
		} else {
			chain.doFilter(servletRequest, servletResponse);
		}
	}
}
