package org.mifosng.ui;

import org.mifosng.configuration.ApplicationConfigurationService;
import org.mifosng.configuration.OAuthProviderDetails;
import org.mifosng.ui.infrastructure.BasicAuthUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller responsible for routing requests on client application to correct jsp page.
 */
@Controller
public class ApplicationPagesRoutesController {

	private final ApplicationConfigurationService applicationConfigurationService;

	@Autowired
	public ApplicationPagesRoutesController(ApplicationConfigurationService applicationConfigurationService) {
		this.applicationConfigurationService = applicationConfigurationService;
	}
	
    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String redirectToIndexPage(final Model model, final Authentication authentication) {
    	
    	BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
    	
        return "redirect:/home";
    }

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView showIndividualClientsOnHomePage(final Model model, final Authentication authentication) {
		
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
    	
		return new ModelAndView("/home");
	}

    @RequestMapping(value="/switchToClient", method = RequestMethod.POST)
    public String switchToClientView(@RequestParam("client") final Long client) {
        return "redirect:/portfolio/client/" + client;
    }
    
	@RequestMapping(value = "/portfolio/client/{clientId}", method = RequestMethod.GET)
	public String viewClientAccount(final Model model, final Authentication authentication, @PathVariable("clientId") final Long clientId) {
		
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
		model.addAttribute("clientId", clientId);
		return "client/viewClientAccount";
	}
	
	@RequestMapping(value = "/org/admin", method = RequestMethod.GET)
	public String organisationAdminScreen(final Model model, final Authentication authentication) {
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
		return "admin/orghome";
	}
	
	@RequestMapping(value = "/org/admin/user", method = RequestMethod.GET)
	public String userAdminScreen(final Model model, final Authentication authentication) {
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
		return "admin/userhome";
	}
	
	@RequestMapping(value = "/org/admin/settings", method = RequestMethod.GET)
	public String userSettingsScreen(final Model model, final Authentication authentication) {
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
		return "admin/accountsettings";
	}
	
	@RequestMapping(value = "/portfolio/client/{clientId}/loan/new", method = RequestMethod.GET)
	public String loadLoanCreationWorkflow(final Model model, final Authentication authentication, @PathVariable("clientId") final Long clientId) {
		
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
		model.addAttribute("clientId", clientId);
		
		return "newloanapplication";
	}
	
	@RequestMapping(value = "/reporting/flexireport", method = RequestMethod.GET)
	public String viewFlexibleReportingPage(final Model model, final Authentication authentication) {

		OAuthProviderDetails oauthDetails = this.applicationConfigurationService.retrieveOAuthProviderDetails();
		
		BasicAuthUserDetails userDetails = (BasicAuthUserDetails) authentication.getPrincipal();
    	model.addAttribute("basicAuthKey", userDetails.getBasicAuthenticationKey());
    	model.addAttribute("baseApiUrl", userDetails.getFullApiUrl());
    	
		model.addAttribute("baseUrl", oauthDetails.getProviderBaseUrl());
		
		return "reports/flexireport";
	}
}