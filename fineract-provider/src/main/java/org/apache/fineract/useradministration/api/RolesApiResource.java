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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.annotation.LegacyApiResponse;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.data.PermissionData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.data.RolePermissionsData;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/roles")
@Component
@Tag(name = "Roles", description = "An API capability to support management of application roles for user administration.")
@RequiredArgsConstructor
public class RolesApiResource {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String AVAILABLE_PERMISSIONS = "availablePermissions";
    public static final String SELECTED_PERMISSIONS = "selectedPermissions";
    public static final String PERMISSION_USAGE_DATA = "permissionUsageData";
    public static final String DISABLE = "disable";
    public static final String ENABLE = "enable";
    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "ROLE";

    private final PlatformSecurityContext context;
    private final RoleReadPlatformService roleReadPlatformService;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Roles", operationId = "retrieveAllRoles", tags = { "Roles" }, description = "Example Requests:\n" + "\n"
            + "roles\n" + "\n" + "\n" + "roles?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesResponse.class)))) })
    @LegacyApiResponse({ "id", "name", "description", "availablePermissions", "selectedPermissions" })
    public Collection<RoleData> retrieveAllRoles(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<RoleData> roles = this.roleReadPlatformService.retrieveAll();

        return roles;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a New Role", operationId = "createRole", tags = { "Roles" }, description = "Mandatory Fields\n"
            + "name, description")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PostRolesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PostRolesResponse.class))) })
    @LegacyApiResponse
    public CommandProcessingResult createRole(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createRole() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result;
    }

    @GET
    @Path("{roleId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Role", operationId = "retrieveOneRole", tags = { "Roles" }, description = "Example Requests:\n" + "\n"
            + "roles/1\n" + "\n" + "\n" + "roles/1?fields=name")
    @AlternativeOperationId("retrieveRole")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesRoleIdResponse.class))) })
    @LegacyApiResponse({ "id", "name", "description", "availablePermissions", "selectedPermissions" })
    public RoleData retrieveRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);

        return role;
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
    @Operation(summary = "Enable Role | Disable Role", operationId = "handleCommandsRole", tags = {
            "Roles" }, description = "Description : Enable role in case role is disabled. | Disable the role in case role is not associated with any users.\n\n\n\n"
                    + "\n\n" + "Example Request:   https://DomainName/api/v1/roles/{roleId}?command=enable" + "\n\n\n\n" + "\n\n"
                    + "https://DomainName/api/v1/roles/{roleId}?command=disable")
    @AlternativeOperationId("actionsOnRoles")
    @Parameters({ @Parameter(description = "No Request Body", name = "No Request Body") })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PostRolesRoleIdResponse.class))) })
    @LegacyApiResponse
    public CommandProcessingResult actionsOnRoles(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, DISABLE)) {
            final CommandWrapper commandRequest = builder.disableRole(roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, ENABLE)) {
            final CommandWrapper commandRequest = builder.enableRole(roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return result;
    }

    @PUT
    @Path("{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Role", operationId = "updateRole", tags = { "Roles" }, description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdResponse.class))) })
    @LegacyApiResponse
    public CommandProcessingResult updateRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateRole(roleId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result;
    }

    @GET
    @Path("{roleId}/permissions")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Role's Permissions", operationId = "retrieveRolePermissions", tags = {
            "Roles" }, description = "Example Requests:\n" + "\n" + "roles/1/permissions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesRoleIdPermissionsResponse.class))) })
    @LegacyApiResponse({ "id", "name", "description", "permissionUsageData" })
    public RolePermissionsData retrieveRolePermissions(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);
        final Collection<PermissionData> permissionUsageData = this.permissionReadPlatformService.retrieveAllRolePermissions(roleId);
        final RolePermissionsData permissionsData = role.toRolePermissionData(permissionUsageData);
        return permissionsData;
    }

    @PUT
    @Path("{roleId}/permissions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Role's Permissions", operationId = "updateRolePermissions", tags = { "Roles" }, description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdPermissionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.PutRolesRoleIdPermissionsResponse.class))) })
    @LegacyApiResponse
    public CommandProcessingResult updateRolePermissions(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateRolePermissions(roleId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result;
    }

    /**
     * Delete Role
     *
     * @param roleId
     * @return
     */
    @DELETE
    @Path("{roleId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Role", operationId = "deleteRole", tags = {
            "Roles" }, description = "Description : Delete the role in case role is not associated with any users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RolesApiResourceSwagger.DeleteRolesRoleIdResponse.class))) })
    @LegacyApiResponse
    public CommandProcessingResult deleteRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteRole(roleId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
