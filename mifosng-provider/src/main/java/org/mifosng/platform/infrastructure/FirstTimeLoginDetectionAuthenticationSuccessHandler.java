package org.mifosng.platform.infrastructure;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth.common.OAuthConsumerParameter;
import org.springframework.security.oauth.provider.OAuthProviderSupport;
import org.springframework.security.oauth.provider.filter.CoreOAuthProviderSupport;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenImpl;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

public class FirstTimeLoginDetectionAuthenticationSuccessHandler extends
		SimpleUrlAuthenticationSuccessHandler {

	private OAuthProviderSupport providerSupport = new CoreOAuthProviderSupport();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private String firstTimeLoginUrl;
	private final OAuthProviderTokenServices tokenServices;

    public FirstTimeLoginDetectionAuthenticationSuccessHandler(final OAuthProviderTokenServices oauthProviderTokenServices) {
		this.tokenServices = oauthProviderTokenServices;
    }
    
	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request,
			final HttpServletResponse response,
			final Authentication authentication) throws ServletException,
			IOException {
		
		// hardcoded success url
		String authSuccessUrl = "/oauth/confirm_access"; // request.getParameter("successUrl");
		
		Map<String, String> oauthParams = this.providerSupport.parseParameters(request);
		String oauthToken = retrieveOAuthToken(oauthParams);
		if (StringUtils.isBlank(oauthToken)) {
			// throw exception
		}
		
		OAuthProviderTokenImpl token = (OAuthProviderTokenImpl) this.tokenServices.getToken(oauthToken);
		
		PlatformUser loggedInUser = (PlatformUser) authentication.getPrincipal();
		
		token.setUserAuthentication(new UsernamePasswordAuthenticationToken(loggedInUser, loggedInUser, loggedInUser.getAuthorities()));
		
		String targetUrl = this.determineTargetUrl(request, response);

		if (loggedInUser.isFirstTimeLoginRemaining()) {
//			request.getSession().setAttribute("firsttimeloginview", targetUrl);
			request.setAttribute("firsttimeloginview", targetUrl);
			targetUrl = this.firstTimeLoginUrl;
		}

		clearAuthenticationAttributes(request);

		getRedirectStrategy().sendRedirect(request, response, authSuccessUrl + "?oauth_token=" + oauthToken);
	}
	
	private String retrieveOAuthToken(Map<String, String> oauthParams) {
		return oauthParams.get(OAuthConsumerParameter.oauth_token.toString());
	}

    @Override
    protected void handle(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException {

        String targetUrl = this.determineTargetUrl(request, response);
        PlatformUser loggedInUser = (PlatformUser) authentication.getPrincipal();
        if (loggedInUser.isFirstTimeLoginRemaining()) {
            targetUrl = this.firstTimeLoginUrl;
        }

        if (response.isCommitted()) {
            this.logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        this.redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    public String getFirstTimeLoginUrl() {
        return this.firstTimeLoginUrl;
    }

    public void setFirstTimeLoginUrl(final String firstTimeLoginUrl) {
        this.firstTimeLoginUrl = firstTimeLoginUrl;
    }
}