/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.PermissionData;
import org.mifosplatform.useradministration.data.RoleData;
import org.mifosplatform.useradministration.data.RolePermissionsData;
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
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "description",
            "availablePermissions", "selectedPermissions"));

    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private final Set<String> PERMISSIONS_RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "description",
            "permissionUsageData"));

    private final String resourceNameForPermissions = "ROLE";

    private final PlatformSecurityContext context;
    private final RoleReadPlatformService roleReadPlatformService;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final DefaultToApiJsonSerializer<RoleData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<RolePermissionsData> permissionsToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public RolesApiResource(final PlatformSecurityContext context, final RoleReadPlatformService readPlatformService,
            final PermissionReadPlatformService permissionReadPlatformService,
            final DefaultToApiJsonSerializer<RoleData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<RolePermissionsData> permissionsToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.roleReadPlatformService = readPlatformService;
        this.permissionReadPlatformService = permissionReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.permissionsToApiJsonSerializer = permissionsToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllRoles(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Collection<RoleData> roles = this.roleReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, roles, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createRole(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createRole() //
                .withJson(apiRequestBodyAsJson) //
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

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);

        return this.toApiJsonSerializer.serialize(settings, role, RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateRole(@PathParam("roleId") final Long roleId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateRole(roleId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

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

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);
        final Collection<PermissionData> permissionUsageData = this.permissionReadPlatformService.retrieveAllRolePermissions(roleId);
        final RolePermissionsData permissionsData = role.toRolePermissionData(permissionUsageData);
        return this.permissionsToApiJsonSerializer.serialize(settings, permissionsData, PERMISSIONS_RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{roleId}/permissions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateRolePermissions(@PathParam("roleId") final Long roleId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateRolePermissions(roleId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}