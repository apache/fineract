package org.mifosng.platform.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mifosng.data.AppUserData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.EnumOptionList;
import org.mifosng.data.EnumOptionReadModel;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.PermissionData;
import org.mifosng.data.PermissionList;
import org.mifosng.data.RoleData;
import org.mifosng.data.RoleList;
import org.mifosng.data.UserList;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.ClientNotAuthenticatedException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.infrastructure.PlatformEmailSendException;
import org.mifosng.platform.infrastructure.UsernameAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Path("/protected/admin")
@Component
@Scope("singleton")
public class UserAdmistrationResource {

    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;

    @GET
	@Path("user/all")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveUsers() {

    	try {
    		Collection<AppUserData> users = this.readPlatformService.retrieveAllUsers();

    		return Response.ok().entity(new UserList(users)).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
    
    @GET
	@Path("user/new")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response newUserDetails() {

    	try {
    		AppUserData newUser = this.readPlatformService.retrieveNewUserDetails();
        	
    		return Response.ok().entity(newUser).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@POST
	@Path("user/new")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createUser(final UserCommand command) {

		try {
			Long userId = this.writePlatformService.createUser(command);
			return Response.ok().entity(new EntityIdentifier(userId)).build();
		} catch (UsernameAlreadyExistsException e) {
			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
			ErrorResponse err = new ErrorResponse("validation.msg.username.already.exists.in.organisation", "username", command.getUsername());
			allErrors.add(err);

			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		} catch (PlatformEmailSendException e) {
			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
			ErrorResponse err = new ErrorResponse("error.msg.user.email.invalid","email", "Email is invalid.");
			allErrors.add(err);
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@DELETE
	@Path("user/{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteUser(@PathParam("userId") final Long userId) {

		try {
			this.writePlatformService.deleteUser(userId);

			return Response.ok().build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("user/{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveUser(@PathParam("userId") final Long userId) {

		try {
			AppUserData user = this.readPlatformService.retrieveUser(userId);
	    	
			return Response.ok().entity(user).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@POST
	@Path("user/{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateUser(UserCommand command) {

		try {
			Long userId = this.writePlatformService.updateUser(command);
	    	
			return Response.ok().entity(new EntityIdentifier(userId)).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("user/current")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveCurrentUser() {

		try {
			AppUserData user = this.readPlatformService.retrieveCurrentUser();
	    	
			return Response.ok().entity(user).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@POST
	@Path("user/current")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateCurrentUser(UserCommand command) {

		try {
			Long userId = this.writePlatformService.updateCurrentUser(command);

			return Response.ok().entity(new EntityIdentifier(userId)).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		} catch (DataIntegrityViolationException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.username.already.exists.in.organisation", "username", command.getUsername());
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		}
	}
	
	@POST
	@Path("user/current/password")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateCurrentUserPassword(ChangePasswordCommand command) {

		try {
			Long userId = this.writePlatformService.updateCurrentUserPassword(command);

			return Response.ok().entity(new EntityIdentifier(userId)).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@GET
	@Path("role/all")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveAllRoles() {

		try {
			Collection<RoleData> roles = this.readPlatformService.retrieveAllRoles();
			return Response.ok().entity(new RoleList(roles)).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("role/new")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveNewRoleDetails() {

		try {
			Collection<PermissionData> allPermissions = this.readPlatformService.retrieveAllPermissions();
			
			RoleData role = new RoleData();
			role.setAvailablePermissions(allPermissions);

			return Response.ok().entity(role).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("role/{roleId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response retrieveRole(@PathParam("roleId") final Long roleId) {

		try {
			RoleData role = this.readPlatformService.retrieveRole(roleId);
			
			Collection<PermissionData> availablePermissions = this.readPlatformService.retrieveAllPermissions();
			
			availablePermissions.removeAll(role.getSelectedPermissions());
			
			role.setAvailablePermissions(availablePermissions);

			return Response.ok().entity(role).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	

	@POST
	@Path("role/{roleId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateRole(@PathParam("roleId") final Long roleId, final RoleCommand command) {

		try {
			this.writePlatformService.updateRole(command);
			return Response.ok().entity(new EntityIdentifier(roleId)).build();
			
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@POST
	@Path("role/new")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createRole(final RoleCommand command) {

		try {
			Long roleId = this.writePlatformService.createRole(command);
			return Response.ok().entity(new EntityIdentifier(roleId)).build();
			
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@GET
	@Path("permissiongroup/all")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrievePermissionGroups() {

		try {
			Collection<EnumOptionReadModel> options = this.readPlatformService
					.retrieveAllPermissionGroups();

			return Response
					.ok()
					.entity(new EnumOptionList(new ArrayList<EnumOptionReadModel>(
							options))).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@GET
	@Path("permissions/all")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrievePermissions() {

		try {
			Collection<PermissionData> permissions = this.readPlatformService.retrieveAllPermissions();

			return Response.ok().entity(new PermissionList(permissions)).build();
		} catch (ClientNotAuthenticatedException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
}