package org.mifosng.ui.user;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.PermissionData;
import org.mifosng.data.RoleData;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class AppUserController {

	private final CommonRestOperations commonRestOperations;

	@Autowired
	public AppUserController(final CommonRestOperations commonRestOperations) {
		this.commonRestOperations = commonRestOperations;
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/admin/permission/all", method = RequestMethod.GET)
	public @ResponseBody Collection<PermissionData> viewAllPermission() {

		 return this.commonRestOperations.retrieveAllPermissions();
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/admin/role/all", method = RequestMethod.GET)
	public @ResponseBody Collection<RoleData> viewAllRoles() {

		 return this.commonRestOperations.retrieveAllRoles();
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/admin/role/new", method = RequestMethod.GET)
	public @ResponseBody RoleData newRoleDetails() {

		 return this.commonRestOperations.retrieveNewRoleDetails();
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/admin/role/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier createNewRole(@RequestParam(value="selectedItems", required=false) String[] selectedRoles,
			@RequestParam(value="name") String name,
			@RequestParam(value="description") String description)  {
		
		RoleCommand command = new RoleCommand(name, description, selectedRoles);
		
		return this.commonRestOperations.createRole(command);
	}
	
	@RequestMapping(consumes = "application/json", produces = "application/json", value = "/admin/role/{roleId}", method = RequestMethod.GET)
	public @ResponseBody
	RoleData retrieveRoleDetails(@PathVariable("roleId") Long roleId) {

		return this.commonRestOperations.retrieveRole(roleId);
	}
	
	@RequestMapping(consumes="application/x-www-form-urlencoded", produces="application/json", value = "/admin/role/{roleId}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier updateRole(@PathVariable("roleId") Long roleId,
			@RequestParam(value="selectedItems", required=false) String[] selectedRoles,
			@RequestParam(value="name") String name,
			@RequestParam(value="description") String description)  {
		
		RoleCommand command = new RoleCommand(name, description, selectedRoles);
		command.setId(roleId);
		
		return this.commonRestOperations.updateRole(command);
	}
	
	@ExceptionHandler(ClientValidationException.class)
	public @ResponseBody Collection<ErrorResponse> validationException(ClientValidationException ex, HttpServletResponse response) {
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/json");
		
		return ex.getValidationErrors();
	}
}