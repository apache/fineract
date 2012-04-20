package org.mifosng.ui.home;

import org.mifosng.ui.reporting.ReportingRestOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	private final ReportingRestOperations reportingRestOperations;

	@Autowired
	public HomeController(final ReportingRestOperations reportingRestOperations) {
		this.reportingRestOperations = reportingRestOperations;
	}
	
	@RequestMapping(value="/forceOAuth", method = RequestMethod.GET)
    public String forceOAuth() {
		
		this.reportingRestOperations.hackToForceAuthentication();
		
        return "redirect:/home";
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
}