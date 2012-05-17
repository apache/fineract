package org.mifosng.ui.admin;

import org.springframework.security.access.AccessDeniedException;
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

    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String redirectToIndexPage() {
        return "redirect:/home";
    }

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView showIndividualClientsOnHomePage() {
		return new ModelAndView("/home");
	}

    @RequestMapping(value="/switchToClient", method = RequestMethod.POST)
    public String switchToClientView(@RequestParam("client") final Long client) {
        return "redirect:/portfolio/client/" + client;
    }
    
	@RequestMapping(value = "/portfolio/client/{clientId}", method = RequestMethod.GET)
	public String viewClientAccount(final Model model, @PathVariable("clientId") final Long clientId) {
		model.addAttribute("clientId", clientId);
		return "client/viewClientAccount";
	}
	
	@RequestMapping(value = "/org/admin", method = RequestMethod.GET)
	public String organisationAdminScreen() {
		return "admin/orghome";
	}
	
	@RequestMapping(value = "/org/admin/user", method = RequestMethod.GET)
	public String userAdminScreen() {
		return "admin/userhome";
	}
	
	@RequestMapping(value = "/org/admin/settings", method = RequestMethod.GET)
	public String userSettingsScreen() {
		return "admin/accountsettings";
	}
	
	@RequestMapping(value = "/portfolio/client/{clientId}/loan/new", method = RequestMethod.GET)
	public String loadLoanCreationWorkflow(final Model model, @PathVariable("clientId") final Long clientId) {
		
//		UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
//    	ConsumerUserDetails user =  (ConsumerUserDetails) authenticationToken.getPrincipal();
//    	if (user.hasNoAuthorityToSumitLoanApplication()) {
//    		throw new AccessDeniedException("");
//    	}
		
		model.addAttribute("clientId", clientId);
		
		return "newloanapplication";
	}
}