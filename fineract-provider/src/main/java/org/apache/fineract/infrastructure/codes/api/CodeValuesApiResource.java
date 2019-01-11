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
package org.apache.fineract.infrastructure.codes.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.codes.CodeConstants.CODEVALUE_JSON_INPUT_PARAMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/codes/{codeId}/codevalues")
@Component
@Scope("singleton")
@Api(value = "Code Values", description = "Code and code values: Codes represent a specific category of data, their code values are a specific instance of that category.\n" + "\n" + "Codes are mostly system defined which means the code itself comes out of the box and cannot be modified however its code values can be. e.g. 'Customer Identifier', it defaults to a code value of 'Passport' but could be 'Drivers License, National Id' etc")
public class CodeValuesApiResource {

    /**
     * The set of parameters that are supported in response for {@link CodeData}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(CODEVALUE_JSON_INPUT_PARAMS.CODEVALUE_ID.getValue(), 
                    CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue(), CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue(), 
                    CODEVALUE_JSON_INPUT_PARAMS.IS_MANDATORY.getValue(), 
                    CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue()));
    private final String resourceNameForPermissions = "CODEVALUE";

    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CodeValueData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public CodeValuesApiResource(final PlatformSecurityContext context, final CodeValueReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<CodeValueData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Code Values", notes = "Returns the list of Code Values for a given Code\n" + "\n" + "Example Requests:\n" + "\n" + "codes/1/codevalues")
    @ApiResponses({@ApiResponse(code = 200, message = "A List of Given response", response = CodeValuesApiResourceSwagger.GetCodeValuesDataResponse.class, responseContainer = "list")})
    public String retrieveAllCodeValues(@Context final UriInfo uriInfo, @PathParam("codeId") @ApiParam(value = "codeId") final Long codeId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CodeValueData> codeValues = this.readPlatformService.retrieveAllCodeValues(codeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, codeValues, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{codeValueId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Code Value", notes = "Returns the details of a Code Value\n" + "\n" + "Example Requests:\n" + "\n" + "codes/1/codevalues/1")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = CodeValuesApiResourceSwagger.GetCodeValuesDataResponse.class)})
    public String retrieveCodeValue(@Context final UriInfo uriInfo, @PathParam("codeValueId") @ApiParam(value = "codeValueId") final Long codeValueId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final CodeValueData codeValue = this.readPlatformService.retrieveCodeValue(codeValueId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, codeValue, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Code Value", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CodeValuesApiResourceSwagger.PostCodeValuesDataRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = CodeValuesApiResourceSwagger.PostCodeValueDataResponse.class)})
    public String createCodeValue(@PathParam("codeId") @ApiParam(value = "codeId") final Long codeId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCodeValue(codeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{codeValueId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Code Value", notes = "Updates the details of a code value.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CodeValuesApiResourceSwagger.PutCodeValuesDataRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = CodeValuesApiResourceSwagger.PutCodeValueDataResponse.class)})
    public String updateCodeValue(@PathParam("codeId") @ApiParam(value = "codeId") final Long codeId, @PathParam("codeValueId") @ApiParam(value = "codeValueId") final Long codeValueId,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCodeValue(codeId, codeValueId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{codeValueId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Code Value", notes = "Deletes a code value")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = CodeValuesApiResourceSwagger.DeleteCodeValueDataResponse.class)})
    public String deleteCodeValue(@PathParam("codeId") @ApiParam(value = "codeId") final Long codeId, @PathParam("codeValueId") @ApiParam(value = "codeValueId") final Long codeValueId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCodeValue(codeId, codeValueId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}