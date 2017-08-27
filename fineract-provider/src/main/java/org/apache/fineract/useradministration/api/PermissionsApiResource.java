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
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.data.PermissionData;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/permissions")
@Component
@Scope("singleton")
@Api(value = "Permissions", description = "An API capability to support management of application permissions for user administration.\n" + "\n" + "There is no Apache Fineract functionality for creating or deleting permissions. Permissions come pre-installed.\n" + "\n" + "Permissions are not updated, except in the case of enabling or disabling non-read transactions for Maker Checker functionality")
public class PermissionsApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("grouping", "code", "entityName", "actionName",
            "selected", "isMakerChecker"));
    private final String resourceNameForPermissions = "PERMISSION";

    private final PlatformSecurityContext context;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final DefaultToApiJsonSerializer<PermissionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public PermissionsApiResource(final PlatformSecurityContext context, final PermissionReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<PermissionData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.permissionReadPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Application Permissions", notes = "ARGUMENTS\n" + "makerCheckerableoptional, Values are true, false. Default is false.\n" + "If makerCheckerable=false or not supplied then a list of application permissions is returned. The \"selected\" attribute is always true in this case.\n" + "\n" + "If makerCheckerable=true then the \"selected\" attribute shows whether the permission is enabled for Maker Check functionality.\n" + "\n" + "Note: Each Apache Fineract transaction is associated with a permission.\n" + "\n" + "Example Requests:\n" + "\n" + "permissions\n" + "\n" + "\n" + "permissions?makerCheckerable=true\n" + "\n" + "\n" + "permissions?fields=grouping,code")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = PermissionsApiResourceSwagger.GetPermissionsResponse.class, responseContainer = "List")})
    public String retrieveAllPermissions(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        Collection<PermissionData> permissions = null;
        if (settings.isMakerCheckerable()) {
            permissions = this.permissionReadPlatformService.retrieveAllMakerCheckerablePermissions();
        } else {
            permissions = this.permissionReadPlatformService.retrieveAllPermissions();
        }

        return this.toApiJsonSerializer.serialize(settings, permissions, this.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Enable/Disable Permissions for Maker Checker", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = PermissionsApiResourceSwagger.PutPermissionsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = CommandProcessingResult.class)})
    public String updatePermissionsDetails(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updatePermissions() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}