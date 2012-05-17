package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.PermissionData;
import org.mifosng.data.RoleData;
import org.mifosng.data.RoleList;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
import org.mifosng.platform.user.service.RoleReadPlatformService;
import org.mifosng.platform.user.service.RoleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/roles")
@Component
@Scope("singleton")
public class RoleApiResource {
	
    @Autowired
	private RoleReadPlatformService roleReadPlatformService;

    @Autowired
   	private PermissionReadPlatformService permissionReadPlatformService;
    
	@Autowired
	private RoleWritePlatformService roleWritePlatformService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveAllRoles() {

		Collection<RoleData> roles = this.roleReadPlatformService.retrieveAllRoles();
		return Response.ok().entity(new RoleList(roles)).build();
	}
	
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveNewRoleDetails() {

		Collection<PermissionData> allPermissions = this.permissionReadPlatformService.retrieveAllPermissions();
		
		RoleData role = new RoleData();
		role.setAvailablePermissions(allPermissions);

		return Response.ok().entity(role).build();
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createRole(final RoleCommand command) {

		Long roleId = this.roleWritePlatformService.createRole(command);
		return Response.ok().entity(new EntityIdentifier(roleId)).build();
	}
	
	@GET
	@Path("{roleId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveRole(@PathParam("roleId") final Long roleId) {

		RoleData role = this.roleReadPlatformService.retrieveRole(roleId);
		
		Collection<PermissionData> availablePermissions = this.permissionReadPlatformService.retrieveAllPermissions();
		availablePermissions.removeAll(role.getSelectedPermissions());
		role.setAvailablePermissions(availablePermissions);

		return Response.ok().entity(role).build();
	}

	@PUT
	@Path("{roleId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateRole(@PathParam("roleId") final Long roleId, final RoleCommand command) {
		
		command.setId(roleId);
		this.roleWritePlatformService.updateRole(command);
		return Response.ok().entity(new EntityIdentifier(roleId)).build();
	}
}