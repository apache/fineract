package org.mifosng.ui.reporting;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.configuration.OAuthProviderDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/reporting")
public class ReportingController {

	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public ReportingController(
			ApplicationConfigurationService applicationConfigurationService) {
		this.applicationConfigurationService = applicationConfigurationService;
	}

    @RequestMapping(value = "/flexireport", method = RequestMethod.GET)
    public String viewFlexibleReportingPage(final Model model) {

//    	UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
//    	ConsumerUserDetails user =  (ConsumerUserDetails) authenticationToken.getPrincipal();
//    	if (user.hasNoReportingAuthority()) {
//    		throw new AccessDeniedException("");
//    	}
    	
		OAuthProviderDetails oauthDetails = this.applicationConfigurationService
				.retrieveOAuthProviderDetails();

		model.addAttribute("baseUrl", oauthDetails.getProviderBaseUrl());
//		model.addAttribute("consumerKey", oauthDetails.getConsumerkey());
//		model.addAttribute("consumerSecret", oauthDetails.getSharedSecret());
//    	
        return "reports/flexireport";
    }
    
    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}
}