package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
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

import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
import org.mifosng.platform.user.service.RoleReadPlatformService;
import org.mifosng.platform.user.service.RoleWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/roles")
@Component
@Scope("singleton")
public class RolesApiResource {

	@Autowired
	private RoleReadPlatformService roleReadPlatformService;

	@Autowired
	private PermissionReadPlatformService permissionReadPlatformService;

	@Autowired
	private RoleWritePlatformService roleWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllRoles(@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<RoleData> roles = this.roleReadPlatformService.retrieveAllRoles();
		
		return this.apiDataConversionService.convertRoleDataToJson(prettyPrint, responseParameters, roles.toArray(new RoleData[roles.size()]));
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewRoleDetails(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description", "availablePermissions", "selectedPermissions"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		Collection<PermissionData> allPermissions = this.permissionReadPlatformService.retrieveAllPermissions();

		RoleData role = new RoleData();
		role.setAvailablePermissions(allPermissions);
		
		return this.apiDataConversionService.convertRoleDataToJson(prettyPrint, responseParameters, role);
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createRole(final String jsonRequestBody) {

		RoleCommand command = this.apiDataConversionService.convertJsonToRoleCommand(null, jsonRequestBody);
		
		Long roleId = this.roleWritePlatformService.createRole(command);
		
		return Response.ok().entity(new EntityIdentifier(roleId)).build();
	}

	@GET
	@Path("{roleId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveRole(@PathParam("roleId") final Long roleId, @Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "name", "description"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

		RoleData role = this.roleReadPlatformService.retrieveRole(roleId);

		if (template) {
			Collection<PermissionData> availablePermissions = this.permissionReadPlatformService.retrieveAllPermissions();
			availablePermissions.removeAll(role.getSelectedPermissions());
			role.setAvailablePermissions(availablePermissions);
			
			responseParameters.add("availablePermissions");
			responseParameters.add("selectedPermissions");
		}

		return this.apiDataConversionService.convertRoleDataToJson(prettyPrint, responseParameters, role);
	}

	@PUT
	@Path("{roleId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateRole(@PathParam("roleId") final Long roleId, final String jsonRequestBody) {

		RoleCommand command = this.apiDataConversionService.convertJsonToRoleCommand(roleId, jsonRequestBody);
			
		this.roleWritePlatformService.updateRole(command);
		
		return Response.ok().entity(new EntityIdentifier(roleId)).build();
	}
}