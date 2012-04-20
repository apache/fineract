package org.mifosng.ui.reporting;

import java.security.Principal;
import java.util.Map;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.configuration.OAuthProviderDetails;
import org.mifosng.oauth.ConsumerUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth.consumer.OAuthConsumerToken;
import org.springframework.security.oauth.consumer.OAuthSecurityContext;
import org.springframework.security.oauth.consumer.OAuthSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/reporting")
public class ReportingController {

	private final ReportingRestOperations reportingRestOperations;
	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public ReportingController(final ReportingRestOperations reportingRestOperations, 
			ApplicationConfigurationService applicationConfigurationService) {
		this.reportingRestOperations = reportingRestOperations;
		this.applicationConfigurationService = applicationConfigurationService;
	}

    @RequestMapping(value = "/flexireport", method = RequestMethod.GET)
    public String viewFlexibleReportingPage(final Model model, final Principal principal) {

    	UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
    	ConsumerUserDetails user =  (ConsumerUserDetails) authenticationToken.getPrincipal();
    	if (user.hasNoReportingAuthority()) {
    		throw new AccessDeniedException("");
    	}
    	
    	this.reportingRestOperations.hackToForceAuthentication();
    	
		OAuthProviderDetails oauthDetails = this.applicationConfigurationService
				.retrieveOAuthProviderDetails();

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

		model.addAttribute("baseUrl", oauthDetails.getProviderBaseUrl());
		model.addAttribute("consumerKey", oauthDetails.getConsumerkey());
		model.addAttribute("consumerSecret", oauthDetails.getSharedSecret());
		if (accessToken != null) {
			model.addAttribute("accessToken", accessToken.getValue());
			model.addAttribute("tokenSecret", accessToken.getSecret());
		}
    	
        return "reports/flexireport";
    }
    
    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}
}