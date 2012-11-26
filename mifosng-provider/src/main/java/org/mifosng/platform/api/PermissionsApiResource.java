package org.mifosng.platform.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.PermissionsCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.PermissionUsageData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
import org.mifosng.platform.user.service.PermissionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/permissions")
@Component
@Scope("singleton")
public class PermissionsApiResource {

    @Autowired
    private PermissionReadPlatformService permissionReadPlatformService;
    
    @Autowired
    private PermissionWritePlatformService permissionWritePlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;
	
    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;

    @Autowired
    private PlatformSecurityContext context;

    private final String resourceNameForPermissions = "PERMISSION";

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllPermissions(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final boolean makerCheckerable = ApiParameterHelper.makerCheckerable(uriInfo.getQueryParameters());
        
        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        
        Collection<PermissionUsageData> permissions = null;
        if (makerCheckerable) permissions = this.permissionReadPlatformService.retrieveAllMakerCheckerablePermissions();
        else permissions = this.permissionReadPlatformService.retrieveAllPermissions();

        return this.apiJsonSerializerService.serializePermissionDataToJson(prettyPrint, responseParameters, permissions);
    }
    

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updatePermissionsDetails(final String jsonRequestBody) {

        final PermissionsCommand command = this.apiDataConversionService.convertApiRequestJsonToPermissionsCommand(jsonRequestBody);

        final Long entityId = this.permissionWritePlatformService.updateMakerCheckerPermissions(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
    }
    
}