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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(ID, NAME, DESCRIPTION, AVAILABLE_PERMISSIONS, SELECTED_PERMISSIONS));
    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private static final Set<String> PERMISSIONS_RESPONSE_PARAMETERS = new HashSet<>(
            Arrays.asList(ID, NAME, DESCRIPTION, PERMISSION_USAGE_DATA));
    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "ROLE";

    private final PlatformSecurityContext context;
    private final RoleReadPlatformService roleReadPlatformService;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final DefaultToApiJsonSerializer<RoleData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<RolePermissionsData> permissionsToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Roles", description = "Example Requests:\n" + "\n" + "roles\n" + "\n" + "\n" + "roles?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RolesApiResourceSwagger.GetRolesResponse.class)))) })
    public String retrieveAllRoles(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<RoleData> roles = this.roleReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, roles, RESPONSE_DATA_PARAMETERS);
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

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);

        return this.toApiJsonSerializer.serialize(settings, role, RESPONSE_DATA_PARAMETERS);
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

        if (is(commandParam, DISABLE)) {
            final CommandWrapper commandRequest = builder.disableRole(roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, ENABLE)) {
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

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);
        final Collection<PermissionData> permissionUsageData = this.permissionReadPlatformService.retrieveAllRolePermissions(roleId);
        final RolePermissionsData permissionsData = role.toRolePermissionData(permissionUsageData);
        return this.permissionsToApiJsonSerializer.serialize(settings, permissionsData, PERMISSIONS_RESPONSE_PARAMETERS);
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
