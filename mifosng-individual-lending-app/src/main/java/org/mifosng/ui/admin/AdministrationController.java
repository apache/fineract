package org.mifosng.ui.admin;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mifosng.data.AppUserData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.UserCommand;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller for user and organisation administration functions.
 */
@Controller
public class AdministrationController {

	private final CommonRestOperations commonRestOperations;

	@Autowired
	public AdministrationController(final CommonRestOperations commonRestOperations) {
		this.commonRestOperations = commonRestOperations;
	}
	
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
	public String userSettingsScreen(Model model) {
		return "admin/accountsettings";
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "org/admin/settings/details", method = RequestMethod.GET)
	public @ResponseBody AppUserData viewUserDetails() {

		return this.commonRestOperations.retrieveCurrentUser();
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "org/admin/settings/details", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier updateUserDetails(HttpServletRequest request,
			@RequestParam(value="username", required=false) String username,
			@RequestParam(value="firstname", required=false) String firstname,
			@RequestParam(value="lastname", required=false) String lastname,
			@RequestParam(value="email", required=false) String email)  {
		
		UserCommand command = new UserCommand(username, firstname, lastname, email);
		
		return this.commonRestOperations.updateCurrentUserDetails(command);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "org/admin/settings/password", method = RequestMethod.GET)
	public @ResponseBody ChangePasswordCommand viewChangePasswordDetails() {

		return new ChangePasswordCommand();
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "org/admin/settings/password", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier updateUserDetails(HttpServletRequest request,
			@RequestParam(value="password", required=false) String password,
			@RequestParam(value="passwordrepeat", required=false) String passwordrepeat)  {
		
		ChangePasswordCommand command = new ChangePasswordCommand();
		command.setPassword(password);
		command.setPasswordrepeat(passwordrepeat);
		
		return this.commonRestOperations.updateCurrentUserPassword(command);
	}
}