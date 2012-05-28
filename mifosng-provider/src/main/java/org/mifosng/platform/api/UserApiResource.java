package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.AppUserData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.user.service.AppUserReadPlatformService;
import org.mifosng.platform.user.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/users")
@Component
@Scope("singleton")
public class UserApiResource {

	private String allowedFieldList = "allowedOffices,availableRoles";
	private String filterName = "userFilter";

	@Autowired
	private AppUserReadPlatformService appUserReadPlatformService;

	@Autowired
	private AppUserWritePlatformService appUserWritePlatformService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveUsers(@Context UriInfo uriInfo) {

		Collection<AppUserData> users = this.appUserReadPlatformService
				.retrieveAllUsers();

		String selectedFields = "id,officeId,officeName,username,firstname,lastname,email";
		return this.jsonFormattingService.convertRequest(users, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@GET
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveUser(@PathParam("userId") final Long userId,
			@Context UriInfo uriInfo) {

		AppUserData user = this.appUserReadPlatformService.retrieveUser(userId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(user, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newUserDetails(@Context UriInfo uriInfo) {

		AppUserData newUser = this.appUserReadPlatformService
				.retrieveNewUserDetails();

		String selectedFields = allowedFieldList;
		return this.jsonFormattingService.convertRequest(newUser, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createUser(UserCommand command) {

		Long userId = this.appUserWritePlatformService.createUser(command);
		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}

	@DELETE
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteUser(@PathParam("userId") final Long userId) {

		this.appUserWritePlatformService.deleteUser(userId);

		return Response.ok(new EntityIdentifier(userId)).build();
	}

	@PUT
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateUser(@PathParam("userId") final Long userId,
			UserCommand command) {

		command.setId(userId);

		this.appUserWritePlatformService.updateUser(command);

		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}
}