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

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
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
@Api(value = "Roles", description = "An API capability to support management of application roles for user administration.")
public class RolesApiResource {

    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "description", "availablePermissions",
            "selectedPermissions"));

    /**
     * The set of parameters that are supported in response for {@link RoleData}
     */
    private final Set<String> PERMISSIONS_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "description",
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
    @ApiOperation(value = "List Roles", notes = "Example Requests:\n" + "\n" + "roles\n" + "\n" + "\n" + "roles?fields=name")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.GetRolesResponse.class, responseContainer = "List")})
    public String retrieveAllRoles(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<RoleData> roles = this.roleReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, roles, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a New Role", notes = "Mandatory Fields\n" + "name, description")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = RolesApiResourceSwagger.PostRolesRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.PostRolesResponse.class)})
    public String createRole(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Retrieve a Role", notes = "Example Requests:\n" + "\n" + "roles/1\n" + "\n" + "\n" + "roles/1?fields=name")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.GetRolesRoleIdResponse.class)})
    public String retrieveRole(@PathParam("roleId") @ApiParam(value = "roleId") final Long roleId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);

        return this.toApiJsonSerializer.serialize(settings, role, this.RESPONSE_DATA_PARAMETERS);
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
    @ApiOperation(value = "Enable Role | Disable Role", notes = "Description : Enable role in case role is disabled. | Disable the role in case role is not associated with any users.\n\n\n\n" + "\n\n" + "Example Request:   https://DomainName/api/v1/roles/{roleId}?command=enable" + "\n\n\n\n" + "\n\n" + "https://DomainName/api/v1/roles/{roleId}?command=disable")
    @ApiImplicitParams({@ApiImplicitParam(value = "No Request Body", name = "No Request Body")})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.PostRolesRoleIdResponse.class)})
    public String actionsOnRoles(@PathParam("roleId") @ApiParam(value = "roleId") final Long roleId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Update a Role", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = RolesApiResourceSwagger.PutRolesRoleIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.PutRolesRoleIdResponse.class)})
    public String updateRole(@PathParam("roleId") @ApiParam(value = "roleId") final Long roleId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Retrieve a Role's Permissions", notes = "Example Requests:\n" + "\n" + "roles/1/permissions")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.GetRolesRoleIdPermissionsResponse.class)})
    public String retrieveRolePermissions(@PathParam("roleId") @ApiParam(value = "roleId") final Long roleId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final RoleData role = this.roleReadPlatformService.retrieveOne(roleId);
        final Collection<PermissionData> permissionUsageData = this.permissionReadPlatformService.retrieveAllRolePermissions(roleId);
        final RolePermissionsData permissionsData = role.toRolePermissionData(permissionUsageData);
        return this.permissionsToApiJsonSerializer.serialize(settings, permissionsData, this.PERMISSIONS_RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{roleId}/permissions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Role's Permissions", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = RolesApiResourceSwagger.PutRolesRoleIdPermissionsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.PutRolesRoleIdPermissionsResponse.class)})
    public String updateRolePermissions(@PathParam("roleId") @ApiParam(value = "roleId") final Long roleId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Delete a Role", notes = "Description : Delete the role in case role is not associated with any users.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = RolesApiResourceSwagger.DeleteRolesRoleIdResponse.class)})
    public String deleteRole(@PathParam("roleId") @ApiParam(value = "roleId") final Long roleId) {

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