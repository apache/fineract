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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.accounting.closure.data.request.GLClosureRequest;
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
import org.springframework.stereotype.Component;

@Path("/v1/glclosures")
@Component
@Tag(name = "Accounting Closure", description = "An accounting closure indicates that no more journal entries may be logged (or reversed) in the system, either manually or via the portfolio with an entry date prior to the defined closure date\n"
        + "\n" + "Field Descriptions\n" + "closingDate\n" + "The date for which the accounting closure is defined\n" + "officeId\n"
        + "The identifer of the branch for which accounting has been closed\n" + "comments\n"
        + "Description associated with an Accounting closure")
@RequiredArgsConstructor
public class GLClosuresApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSION = "GLCLOSURE";

    private final PlatformSecurityContext context;
    private final GLClosureReadPlatformService glClosureReadPlatformService;
    private final DefaultToApiJsonSerializer<GLClosureData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Accounting closures", description = "Example Requests:\n" + "\n" + "glclosures")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GLClosuresApiResourceSwagger.GetGlClosureResponse.class)))) })
    public List<GLClosureData> retrieveAllClosures(@QueryParam("officeId") @Parameter(name = "officeId") final Long officeId) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
        return glClosureReadPlatformService.retrieveAllGLClosures(officeId);
    }

    @GET
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Accounting Closure", description = "Example Requests:\n" + "\n" + "glclosures/1\n" + "\n" + "\n"
            + "/glclosures/1?fields=officeName,closingDate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.GetGlClosureResponse.class))) })
    public GLClosureData retreiveClosure(@PathParam("glClosureId") @Parameter(description = "glClosureId") final Long glClosureId,
            @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final GLClosureData glClosureData = glClosureReadPlatformService.retrieveGLClosureById(glClosureId);
        if (settings.isTemplate()) {
            glClosureData.setAllowedOffices(officeReadPlatformService.retrieveAllOfficesForDropdown());
        }
        return glClosureData;
    }

    // NOTE: proposal slight changes @Aleks
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Accounting Closure", description = "Mandatory Fields\n" + "officeId,closingDate")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PostGlClosuresRequest.class, description = "Request Body")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PostGlClosuresResponse.class))) })
    public CommandProcessingResult createGLClosure(@Parameter(hidden = true) GLClosureRequest glClosureRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGLClosure()
                .withJson(apiJsonSerializerService.serialize(glClosureRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an Accounting closure", description = "Once an accounting closure is created, only the comments associated with it may be edited")
    @RequestBody(content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PutGlClosuresRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PutGlClosuresResponse.class))) })
    public CommandProcessingResult updateGLClosure(@PathParam("glClosureId") @Parameter(description = "glClosureId") final Long glClosureId,
            @Parameter(hidden = true) GLClosureRequest glClosureRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGLClosure(glClosureId)
                .withJson(apiJsonSerializerService.serialize(glClosureRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete an accounting closure", description = "Note: Only the latest accounting closure associated with a branch may be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.DeleteGlClosuresResponse.class))) })
    public CommandProcessingResult deleteGLClosure(
            @PathParam("glClosureId") @Parameter(description = "glclosureId") final Long glClosureId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteGLClosure(glClosureId).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
