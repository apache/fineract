package org.mifosng.ui.admin;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.mifosng.data.ErrorResponse;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for user and organisation administration functions.
 */
@Controller
public class AdministrationController {

    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}
	
	@ExceptionHandler(ClientValidationException.class)
	public @ResponseBody Collection<ErrorResponse> validationException(ClientValidationException ex, HttpServletResponse response) {
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/json");
		
		return ex.getValidationErrors();
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
}