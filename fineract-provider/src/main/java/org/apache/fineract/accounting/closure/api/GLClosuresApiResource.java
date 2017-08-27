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
package org.apache.fineract.accounting.closure.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.accounting.closure.service.GLClosureReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/glclosures")
@Component
@Scope("singleton")
@Api(value = "Accounting Closure", description = "An accounting closure indicates that no more journal entries may be logged (or reversed) in the system, either manually or via the portfolio with an entry date prior to the defined closure date\n" + "\n" + "Field Descriptions\n" + "closingDate\n" + "The date for which the accounting closure is defined\n" + "officeId\n" + "The identifer of the branch for which accounting has been closed\n" + "comments\n" + "Description associated with an Accounting closure")
public class GLClosuresApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName",
            "closingDate", "deleted", "createdDate", "lastUpdatedDate", "createdByUserId", "createdByUsername", "lastUpdatedByUserId",
            "lastUpdatedByUsername"));

    private final String resourceNameForPermission = "GLCLOSURE";

    private final GLClosureReadPlatformService glClosureReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<GLClosureData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public GLClosuresApiResource(final PlatformSecurityContext context, final GLClosureReadPlatformService glClosureReadPlatformService,
            final DefaultToApiJsonSerializer<GLClosureData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final OfficeReadPlatformService officeReadPlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.glClosureReadPlatformService = glClosureReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Accounting closures", notes = "Example Requests:\n" + "\n" + "glclosures")
    @ApiResponses({@ApiResponse(code = 200, response = GLClosuresApiResourceSwagger.GetGlClosureResponse.class, message = "", responseContainer = "list")})
    public String retrieveAllClosures(@Context final UriInfo uriInfo, @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
        final List<GLClosureData> glClosureDatas = this.glClosureReadPlatformService.retrieveAllGLClosures(officeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glClosureDatas, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve an Accounting Closure", notes = "Example Requests:\n" + "\n" + "glclosures/1\n" + "\n" + "\n" + "/glclosures/1?fields=officeName,closingDate")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLClosuresApiResourceSwagger.GetGlClosureResponse.class)})
    public String retreiveClosure(@PathParam("glClosureId") @ApiParam(value = "glClosureId") final Long glClosureId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final GLClosureData glClosureData = this.glClosureReadPlatformService.retrieveGLClosureById(glClosureId);
        if (settings.isTemplate()) {
            glClosureData.setAllowedOffices(this.officeReadPlatformService.retrieveAllOfficesForDropdown());
        }

        return this.apiJsonSerializerService.serialize(settings, glClosureData, RESPONSE_DATA_PARAMETERS);
    }

    // NOTE: proposal slight changes @Aleks
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create an Accounting Closure", notes = "Mandatory Fields\n" + "officeId,closingDate")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", required = true, type = "body", dataTypeClass = GLClosuresApiResourceSwagger.PostGlClosuresRequest.class, value = "Request Body")})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLClosuresApiResourceSwagger.PostGlClosuresResponse.class)})
    public String createGLClosure(@ApiParam(hidden = true) final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGLClosure().withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update an Accounting closure", notes = "Once an accounting closure is created, only the comments associated with it may be edited")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", value = "Request body", dataType = "body", dataTypeClass = GLClosuresApiResourceSwagger.PutGlClosuresRequest.class, required = true)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLClosuresApiResourceSwagger.PutGlClosuresResponse.class)})
    public String updateGLClosure(@PathParam("glClosureId") @ApiParam(value = "glClosureId") final Long glClosureId, @ApiParam(hidden = true) final String jsonRequestBody) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGLClosure(glClosureId).withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete an accounting closure", notes = "Note: Only the latest accounting closure associated with a branch may be deleted.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = GLClosuresApiResourceSwagger.DeleteGlClosuresResponse.class)})
    public String deleteGLClosure(@PathParam("glClosureId") @ApiParam(value = "glclosureId") final Long glClosureId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGLClosure(glClosureId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}