package org.mifosplatform.useradministration.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.api.PortfolioCommandSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.data.PermissionUsageData;
import org.mifosplatform.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/permissions")
@Component
@Scope("singleton")
public class PermissionsApiResource {

    private final String resourceNameForPermissions = "PERMISSION";
    
    private final PlatformSecurityContext context;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public PermissionsApiResource(
            final PlatformSecurityContext context, 
            final PermissionReadPlatformService readPlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.permissionReadPlatformService = readPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllPermissions(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final boolean makerCheckerable = ApiParameterHelper.makerCheckerable(uriInfo.getQueryParameters());

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<PermissionUsageData> permissions = null;
        if (makerCheckerable) {
            permissions = this.permissionReadPlatformService.retrieveAllMakerCheckerablePermissions();
        } else {
            permissions = this.permissionReadPlatformService.retrieveAllPermissions();
        }

        return this.apiJsonSerializerService.serializePermissionDataToJson(prettyPrint, responseParameters, permissions);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updatePermissionsDetails(final String apiRequestBodyAsJson) {

        // FIXME - kw/jw - reuse 'PERMISSIONS_ROLE' permission for now
        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "USER_ADMINISTRATION_SUPER_USER", "PERMISSIONS_ROLE");
        context.authenticatedUser().validateHasPermissionTo("PERMISSIONS_ROLE", allowedPermissions);
        
        final PermissionsCommand command = this.apiDataConversionService.convertApiRequestJsonToPermissionsCommand(apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "permissions", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}