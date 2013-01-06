package org.mifosplatform.useradministration.api;

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
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.data.CommandSourceData;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.commands.service.PortfolioCommandsReadPlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.data.PermissionUsageData;
import org.mifosplatform.useradministration.data.RoleData;
import org.mifosplatform.useradministration.data.RolePermissionsData;
import org.mifosplatform.useradministration.serialization.RoleCommandFromApiJsonDeserializer;
import org.mifosplatform.useradministration.service.PermissionReadPlatformService;
import org.mifosplatform.useradministration.service.RoleReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/roles")
@Component
@Scope("singleton")
public class RolesApiResource {

    /**
     * The set of parameters that are supported in response for {@link CodeData}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "description",
            "availablePermissions", "selectedPermissions"));

    /**
     * The set of parameters that are supported in response for {@link CodeData}
     */
    private final Set<String> PERMISSIONS_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "description",
            "permissionUsageData"));

    private final String resourceNameForPermissions = "ROLE";

    private final PlatformSecurityContext context;
    private final RoleReadPlatformService roleReadPlatformService;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final RoleCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final DefaultToApiJsonSerializer<RoleData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<RolePermissionsData> permissionsToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandsReadPlatformService commandSourceReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public RolesApiResource(final PlatformSecurityContext context, final RoleReadPlatformService readPlatformService,
            final PermissionReadPlatformService permissionReadPlatformService,
            final RoleCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final DefaultToApiJsonSerializer<RoleData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<RolePermissionsData> permissionsToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandsReadPlatformService commandSourceReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.roleReadPlatformService = readPlatformService;
        this.permissionReadPlatformService = permissionReadPlatformService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.permissionsToApiJsonSerializer = permissionsToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandSourceReadPlatformService = commandSourceReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllRoles(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Collection<RoleData> roles = this.roleReadPlatformService.retrieveAllRoles();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, roles, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createRole(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createRole().withUrl("/roles").withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveRole(@PathParam("roleId") final Long roleId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        RoleData role = this.roleReadPlatformService.retrieveRole(roleId);
        if (settings.isCommandIdPassed()) {
            RoleData currentChanges = handleRequestToIntegrateProposedChangesFromRoleCommand(settings.getCommandId());

            role = RoleData.integrateChanges(role, currentChanges);
        }

        return this.toApiJsonSerializer.serialize(settings, role, RESPONSE_DATA_PARAMETERS);
    }

    private RoleData handleRequestToIntegrateProposedChangesFromRoleCommand(final Long commandId) {
        final CommandSourceData entry = this.commandSourceReadPlatformService.retrieveById(commandId);
        return assembleRoleChanges(entry);
    }

    private RoleData assembleRoleChanges(final CommandSourceData entry) {
        final RoleCommand changesOnly = this.fromApiJsonDeserializer.commandFromApiJson(entry.json());
        return RoleData.changes(changesOnly.getName(), changesOnly.getDescription());
    }

    @PUT
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateRole(@PathParam("roleId") final Long roleId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRole().withUrl("/roles").withJson(apiRequestBodyAsJson)
                .withEntityId(roleId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{roleId}/permissions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveRolePermissions(@PathParam("roleId") final Long roleId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveRole(roleId);
        final Collection<PermissionUsageData> permissionUsageData = this.permissionReadPlatformService.retrieveAllRolePermissions(roleId);
        Collection<PermissionUsageData> currentChanges = null;

        final RolePermissionsData permissionsData = role.toRolePermissionData(permissionUsageData, currentChanges);
        return this.permissionsToApiJsonSerializer.serialize(settings, permissionsData, PERMISSIONS_RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{roleId}/permissions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateRolePermissions(@PathParam("roleId") final Long roleId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRolePermissions().withUrl("/roles/" + roleId + "/permissions").withJson(apiRequestBodyAsJson)
                .withEntityId(roleId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        
        return this.toApiJsonSerializer.serialize(result);
    }
}