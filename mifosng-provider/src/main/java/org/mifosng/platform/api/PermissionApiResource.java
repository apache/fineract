package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.PermissionData;
import org.mifosng.data.PermissionList;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/permissions")
@Component
@Scope("singleton")
public class PermissionApiResource {
	
    @Autowired
   	private PermissionReadPlatformService permissionReadPlatformService;
    
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveAllPermissions() {

		Collection<PermissionData> permissions = this.permissionReadPlatformService.retrieveAllPermissions();
		return Response.ok().entity(new PermissionList(permissions)).build();
	}
}