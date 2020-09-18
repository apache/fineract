/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.useradministration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.data.PermissionData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.data.RolePermissionsData;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/roles")
@Component
@Scope("singleton")
@Tag(name = "Roles", description = "An API capability to support management of application roles for user administration.")
public class RolesApiResource {

    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private final Set<String> responseDataParameters = new HashSet<>(
            Arrays.asList("id", "name", "description", "availablePermissions", "selectedPermissions"));

    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private final Set<String> permissionsResponseParameters = new HashSet<>(
            Arrays.asList("id", "name", "description", "permissionUsageData"));

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
    @Operation(summary = "List Roles", description = "Example Requests:\n" + "\n" + "roles\n" + "\n" + "\n" + "roles?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesResponse.class)))) })
    public String retrieveAllRoles(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<RoleData> roles = this.roleReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, roles, this.responseDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a New Role", description = "Mandatory Fields\n" + "name, description")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PostRolesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PostRolesResponse.class))) })
    public String createRole(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

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
    @Operation(summary = "Retrieve a Role", description = "Example Requests:\n" + "\n" + "roles/1\n" + "\n" + "\n" + "roles/1?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesRoleIdResponse.class))) })
    public String retrieveRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);

        return this.toApiJsonSerializer.serialize(settings, role, this.responseDataParameters);
    }

    /**
     * Roles enable or disable
     *
     * @param roleId
     * @param commandParam
     * @param apiRequestBodyAsJson
     * @return
     */
    @POST
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Enable Role | Disable Role", description = "Description : Enable role in case role is disabled. | Disable the role in case role is not associated with any users.\n\n\n\n"
            + "\n\n" + "Example Request:   https://DomainName/api/v1/roles/{roleId}?command=enable" + "\n\n\n\n" + "\n\n"
            + "https://DomainName/api/v1/roles/{roleId}?command=disable")
    @Parameters({ @Parameter(description = "No Request Body", name = "No Request Body") })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PostRolesRoleIdResponse.class))) })
    public String actionsOnRoles(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, "disable")) {
            final CommandWrapper commandRequest = builder.disableRole(roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "enable")) {
            final CommandWrapper commandRequest = builder.enableRole(roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Role", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdResponse.class))) })
    public String updateRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

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
    @Operation(summary = "Retrieve a Role's Permissions", description = "Example Requests:\n" + "\n" + "roles/1/permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesRoleIdPermissionsResponse.class))) })
    public String retrieveRolePermissions(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);
        final Collection<PermissionData> permissionUsageData = this.permissionReadPlatformService.retrieveAllRolePermissions(roleId);
        final RolePermissionsData permissionsData = role.toRolePermissionData(permissionUsageData);
        return this.permissionsToApiJsonSerializer.serialize(settings, permissionsData, this.permissionsResponseParameters);
    }

    @PUT
    @Path("{roleId}/permissions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Role's Permissions", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdPermissionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdPermissionsResponse.class))) })
    public String updateRolePermissions(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateRolePermissions(roleId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * Delete Role
     *
     * @param roleId
     * @return
     */
    @DELETE
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Role", description = "Description : Delete the role in case role is not associated with any users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.DeleteRolesRoleIdResponse.class))) })
    public String deleteRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteRole(roleId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
