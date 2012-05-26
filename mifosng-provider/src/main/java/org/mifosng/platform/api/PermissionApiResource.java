package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.PermissionData;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/permissions")
@Component
@Scope("singleton")
public class PermissionApiResource {
	private String allowedFieldList = "";
	private String filterName = "permissionFilter";

    @Autowired
   	private PermissionReadPlatformService permissionReadPlatformService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllPermissions(@Context UriInfo uriInfo) {

		Collection<PermissionData> permissions = this.permissionReadPlatformService.retrieveAllPermissions();
		
		//TODO - setting selectedFields just to not show org_id, can be set be to "" after org_id is removed
		String selectedFields = "id,name,description,code,groupType";
		return this.jsonFormattingService.convertRequest(permissions, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}
}