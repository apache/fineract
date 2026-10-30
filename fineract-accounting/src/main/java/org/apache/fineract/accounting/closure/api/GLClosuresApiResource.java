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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.closure.command.GLClosureCreateCommand;
import org.apache.fineract.accounting.closure.command.GLClosureDeleteCommand;
import org.apache.fineract.accounting.closure.command.GLClosureUpdateCommand;
import org.apache.fineract.accounting.closure.data.GLClosureCreateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureCreateResponse;
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteRequest;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteResponse;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateResponse;
import org.apache.fineract.accounting.closure.service.GLClosureReadPlatformService;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/glclosures")
@Component
@Tag(name = "Accounting Closure", description = """
        An accounting closure indicates that no more journal entries may be logged (or reversed) in the system, either manually or via the portfolio with an entry date prior to the defined closure date

        Field Descriptions
        closingDate
        The date for which the accounting closure is defined
        officeId
        The identifier of the branch for which accounting has been closed
        comments
        Description associated with an Accounting closure
        """)
@RequiredArgsConstructor
public class GLClosuresApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSION = "GLCLOSURE";

    private final PlatformSecurityContext context;
    private final GLClosureReadPlatformService glClosureReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final CommandDispatcher dispatcher;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Accounting closures", operationId = "retrieveAllGLClosures", description = """
            Example Requests:

            glclosures""")
    @AlternativeOperationId("retrieveAllClosures")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GLClosuresApiResourceSwagger.GetGlClosureResponse.class))))
    public List<GLClosureData> retrieveAllClosures(@QueryParam("officeId") @Parameter(name = "officeId") final Long officeId) {

        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
        return glClosureReadPlatformService.retrieveAllGLClosures(officeId);
    }

    @GET
    @Path("{glClosureId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Accounting Closure", operationId = "retrieveOneGLClosure", description = """
            Example Requests:

            glclosures/1


            /glclosures/1?fields=officeName,closingDate""")
    @AlternativeOperationId("retreiveClosure")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.GetGlClosureResponse.class)))
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

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Accounting Closure", operationId = "createGLClosure", description = "Mandatory Fields\n"
            + "officeId,closingDate")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PostGlClosuresRequest.class, description = "Request Body")))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PostGlClosuresResponse.class)))
    public GLClosureCreateResponse createGLClosure(@Valid GLClosureCreateRequest request) {
        final var command = new GLClosureCreateCommand();
        command.setPayload(request);

        final Supplier<GLClosureCreateResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

    @PUT
    @Path("{glClosureId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an Accounting closure", operationId = "updateGLClosure", description = "Once an accounting closure is created, only the comments associated with it may be edited")
    @RequestBody(content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PutGlClosuresRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.PutGlClosuresResponse.class)))
    public GLClosureUpdateResponse updateGLClosure(@PathParam("glClosureId") @Parameter(description = "glClosureId") final Long glClosureId,
            @Valid final GLClosureUpdateRequest request) {
        request.setId(glClosureId);

        final var command = new GLClosureUpdateCommand();
        command.setPayload(request);

        final Supplier<GLClosureUpdateResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

    @DELETE
    @Path("{glClosureId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete an accounting closure", operationId = "deleteGLClosure", description = "Note: Only the latest accounting closure associated with a branch may be deleted.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GLClosuresApiResourceSwagger.DeleteGlClosuresResponse.class)))
    public GLClosureDeleteResponse deleteGLClosure(
            @PathParam("glClosureId") @Parameter(description = "glclosureId") final Long glClosureId) {
        final var command = new GLClosureDeleteCommand();
        command.setPayload(GLClosureDeleteRequest.builder().id(glClosureId).build());

        final Supplier<GLClosureDeleteResponse> response = dispatcher.dispatch(command);
        return response.get();
    }
}
