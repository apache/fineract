package org.mifosng.platform.security;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mifosng.oauth.CustomOAuthProviderTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth.common.OAuthConsumerParameter;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.provider.OAuthProviderSupport;
import org.springframework.security.oauth.provider.filter.CoreOAuthProviderSupport;
import org.springframework.security.oauth.provider.token.OAuthProviderToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AccessConfirmationController {

	@Autowired
	private CustomOAuthProviderTokenServices tokenServices;

	@Autowired
	private ConsumerDetailsService consumerDetailsService;
	
	private OAuthProviderSupport providerSupport = new CoreOAuthProviderSupport();
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@RequestMapping("/oauth/confirm_access")
	public ModelAndView getAccessConfirmation(final HttpServletRequest request,
			final HttpServletResponse response) throws Exception {

		Map<String, String> oauthParams = this.providerSupport.parseParameters(request);
		String oauthToken = retrieveOAuthToken(oauthParams);
		if (oauthToken == null) {
			throw new IllegalArgumentException(
					"A request token to authorize must be provided.");
		}
		
		OAuthProviderToken providerToken = this.tokenServices.getToken(oauthToken);
		ConsumerDetails consumer = this.consumerDetailsService
				.loadConsumerByConsumerKey(providerToken.getConsumerKey());

		String callback = retrieveCallbackurl(oauthParams);
		TreeMap<String, Object> model = new TreeMap<String, Object>();
		model.put(OAuthConsumerParameter.oauth_token.toString(), oauthToken);
		if (callback != null) {
			model.put(OAuthConsumerParameter.oauth_callback.toString(), callback);
		}
		model.put("consumer", consumer);
		
		// check if user is fully authenticated here and if not redirect to login page passing oauth token info etc
		SecurityContext context = SecurityContextHolder.getContext();
		if (context.getAuthentication() instanceof AnonymousAuthenticationToken) {
			model.put("successUrl", "/oauth/confirm_access");
			
			return new ModelAndView("login", model);
		}
		
		if ("mifosng-ui-consumer-key".equalsIgnoreCase(consumer.getConsumerKey())) {
			response.sendRedirect("authorize?requestToken=" + oauthToken);
			return null;
		} else {
			redirectStrategy.sendRedirect(request, response, "access_confirmation");
			return new ModelAndView("access_confirmation", model);
		}
	}
	
	private String retrieveOAuthToken(Map<String, String> oauthParams) {
		return oauthParams.get(OAuthConsumerParameter.oauth_token.toString());
	}
	
	private String retrieveCallbackurl(Map<String, String> oauthParams) {
		return oauthParams.get(OAuthConsumerParameter.oauth_callback.toString());
	}
}