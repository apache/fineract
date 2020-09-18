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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.service.CodeReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/codes")
@Component
@Scope("singleton")
@Tag(name = "Codes", description = "Code and code values: Codes represent a specific category of data, their code values are a specific instance of that category.\n"
        + "\n"
        + "Codes are mostly system defined which means the code itself comes out of the box and cannot be modified however its code values can be. e.g. 'Customer Identifier', it defaults to a code value of 'Passport' but could be 'Drivers License, National Id' etc")
public class CodesApiResource {

    /**
     * The set of parameters that are supported in response for {@link CodeData}
     */
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "systemDefined"));
    private final String resourceNameForPermissions = "CODE";

    private final PlatformSecurityContext context;
    private final CodeReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<CodeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public CodesApiResource(final PlatformSecurityContext context, final CodeReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<CodeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
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
    @Operation(summary = "Retrieve Codes", description = "Returns the list of codes.\n" + "\n" + "Example Requests:\n" + "\n" + "codes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CodesApiResourceSwagger.GetCodesResponse.class)))) })
    public String retrieveCodes(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<CodeData> codes = this.readPlatformService.retrieveAllCodes();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, codes, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Code", description = "Creates a code. Codes created through api are always 'user defined' and so system defined is marked as false.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CodesApiResourceSwagger.PostCodesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodesApiResourceSwagger.PostCodesResponse.class))) })
    public String createCode(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCode().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Code", description = "Returns the details of a Code.\n" + "\n" + "Example Requests:\n" + "\n"
            + "codes/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodesApiResourceSwagger.GetCodesResponse.class))) })
    public String retrieveCode(@PathParam("codeId") @Parameter(description = "codeId") final Long codeId, @Context final UriInfo uriInfo) {

        final CodeData code = this.readPlatformService.retrieveCode(codeId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, code, RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Code", description = "Updates the details of a code if it is not system defined.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CodesApiResourceSwagger.PutCodesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodesApiResourceSwagger.PutCodesResponse.class))) })
    public String updateCode(@PathParam("codeId") @Parameter(description = "codeId") final Long codeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCode(codeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Code", description = "Deletes a code if it is not system defined.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CodesApiResourceSwagger.DeleteCodesResponse.class))) })
    public String deleteCode(@PathParam("codeId") @Parameter(description = "codeId") final Long codeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCode(codeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
