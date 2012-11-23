package org.mifosng.platform.api;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.data.PermissionUsageData;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.service.PermissionReadPlatformService;
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
    private PortfolioApiJsonSerializerService apiJsonSerializerService;

    @Autowired
    private PlatformSecurityContext context;

    private final String resourceNameForPermissions = "PERMISSION";
    
    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAllPermissions(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<PermissionUsageData> permissions = this.permissionReadPlatformService.retrieveAllPermissions();

        return this.apiJsonSerializerService.serializePermissionDataToJson(prettyPrint, responseParameters, permissions);
    }
}