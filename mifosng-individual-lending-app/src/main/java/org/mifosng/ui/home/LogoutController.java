package org.mifosng.ui.home;

import java.util.Map;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.ui.CommonRestOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.OAuthSecurityContext;
import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LogoutController {

	private final CommonRestOperations commonRestOperations;
//	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public LogoutController(final CommonRestOperations commonRestOperations,
			ApplicationConfigurationService applicationConfigurationService) {
		this.commonRestOperations = commonRestOperations;
//		this.applicationConfigurationService = applicationConfigurationService;
	}
	
	@RequestMapping(value="/logoutcontroller", method = RequestMethod.GET)
    public String forceOAuth() {
		
//		OAuthProviderDetails oauthDetails = this.applicationConfigurationService
//				.retrieveOAuthProviderDetails();

		String resourceId = "protectedMifosNgServices";
		OAuthSecurityContext context = OAuthSecurityContextHolder.getContext();
		if (context == null) {
			throw new IllegalStateException(
					"No OAuth security context has been established. Unable to access resource '"
							+ resourceId + "'.");
		}

		Map<String, OAuthConsumerToken> accessTokens = context
				.getAccessTokens();
		OAuthConsumerToken accessToken = accessTokens == null ? null
				: accessTokens.get(resourceId);

//		model.addAttribute("baseUrl", oauthDetails.getProviderBaseUrl());
//		model.addAttribute("consumerKey", oauthDetails.getConsumerkey());
//		model.addAttribute("consumerSecret", oauthDetails.getSharedSecret());
//		if (accessToken != null) {
//			model.addAttribute("accessToken", accessToken.getValue());
//			model.addAttribute("tokenSecret", accessToken.getSecret());
//		}
		
		String oauthToken = "";
		if (accessToken != null) {
			oauthToken = accessToken.getValue();
		}
		
		this.commonRestOperations.logout(oauthToken);
		
        return "redirect:/j_spring_security_logout";
    }
}