package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
import org.mifosng.platform.user.service.RoleReadPlatformService;
import org.mifosng.platform.user.service.RoleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/roles")
@Component
@Scope("singleton")
public class RoleApiResource {

	private String allowedFieldList = "availablePermissions";
	private String filterName = "roleFilter";

	@Autowired
	private RoleReadPlatformService roleReadPlatformService;

	@Autowired
	private PermissionReadPlatformService permissionReadPlatformService;

	@Autowired
	private RoleWritePlatformService roleWritePlatformService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllRoles(@Context UriInfo uriInfo) {

		Collection<RoleData> roles = this.roleReadPlatformService
				.retrieveAllRoles();

		String selectedFields = "id,name,description";
		return this.jsonFormattingService.convertRequest(roles, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveNewRoleDetails(@Context UriInfo uriInfo) {

		Collection<PermissionData> allPermissions = this.permissionReadPlatformService
				.retrieveAllPermissions();

		RoleData role = new RoleData();
		role.setAvailablePermissions(allPermissions);

		String selectedFields = allowedFieldList;
		return this.jsonFormattingService.convertRequest(role, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createRole(final RoleCommand command) {

		Long roleId = this.roleWritePlatformService.createRole(command);
		return Response.ok().entity(new EntityIdentifier(roleId)).build();
	}

	@GET
	@Path("{roleId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveRole(@PathParam("roleId") final Long roleId,
			@QueryParam("template") String template, @Context UriInfo uriInfo) {

		RoleData role = this.roleReadPlatformService.retrieveRole(roleId);

		if (template != null && template.equalsIgnoreCase("true")) {
			Collection<PermissionData> availablePermissions = this.permissionReadPlatformService
					.retrieveAllPermissions();
			availablePermissions.removeAll(role.getSelectedPermissions());
			role.setAvailablePermissions(availablePermissions);
		}

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(role, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	@PUT
	@Path("{roleId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateRole(@PathParam("roleId") final Long roleId,
			final RoleCommand command) {

		command.setId(roleId);
		this.roleWritePlatformService.updateRole(command);
		return Response.ok().entity(new EntityIdentifier(roleId)).build();
	}
}