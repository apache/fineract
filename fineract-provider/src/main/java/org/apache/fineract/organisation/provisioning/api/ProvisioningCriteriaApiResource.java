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
package org.apache.fineract.organisation.provisioning.api;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaData;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/provisioningcriteria")
@Component
@Tag(name = "Provisioning Criteria", description = "This defines the Provisioning Criteria")
@RequiredArgsConstructor
public class ProvisioningCriteriaApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ProvisioningCriteriaData> toApiJsonSerializer;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ProvisioningCriteriaData retrieveTemplate() {
        platformSecurityContext.authenticatedUser();
        return provisioningCriteriaReadPlatformService.retrievePrivisiongCriteriaTemplate();
    }

    @GET
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieves a Provisioning Criteria", description = "Retrieves a Provisioning Criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.GetProvisioningCriteriaCriteriaIdResponse.class))) })
    public ProvisioningCriteriaData retrieveProvisioningCriteria(
            @PathParam("criteriaId") @Parameter(description = "criteriaId") final Long criteriaId, @Context final UriInfo uriInfo) {
        platformSecurityContext.authenticatedUser();
        ProvisioningCriteriaData criteria = provisioningCriteriaReadPlatformService.retrieveProvisioningCriteria(criteriaId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            criteria = provisioningCriteriaReadPlatformService.retrievePrivisiongCriteriaTemplate(criteria);
        }
        return criteria;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieves all created Provisioning Criterias", description = "Retrieves all created Provisioning Criterias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.GetProvisioningCriteriaResponse.class)))) })
    public List<ProvisioningCriteriaData> retrieveAllProvisioningCriterias() {
        platformSecurityContext.authenticatedUser();
        return provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Provisioning Criteria", description = "Creates a new Provisioning Criteria\n" + "\n"
            + "Mandatory Fields: \n" + "criteriaName\n" + "provisioningcriteria\n" + "\n" + "Optional Fields: \n" + "loanProducts")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.PostProvisioningCriteriaRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.PostProvisioningCriteriaResponse.class))) })
    public CommandProcessingResult createProvisioningCriteria(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformSecurityContext.authenticatedUser();
        CommandWrapper commandWrapper = new CommandWrapperBuilder().createProvisioningCriteria().withJson(apiRequestBodyAsJson).build();
        return commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @PUT
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Updates a new Provisioning Criteria", description = "Updates a new Provisioning Criteria\n" + "\n"
            + "Optional Fields\n" + "criteriaName, loanProducts, provisioningcriteria")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.PutProvisioningCriteriaRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.PutProvisioningCriteriaResponse.class))) })
    public CommandProcessingResult updateProvisioningCriteria(
            @PathParam("criteriaId") @Parameter(description = "criteriaId") final Long criteriaId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProvisioningCriteria(criteriaId)
                .withJson(apiRequestBodyAsJson).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{criteriaId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Deletes Provisioning Criteria", description = "Deletes Provisioning Criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProvisioningCriteriaApiResourceSwagger.DeleteProvisioningCriteriaResponse.class))) })
    public CommandProcessingResult deleteProvisioningCriteria(
            @PathParam("criteriaId") @Parameter(description = "criteriaId") final Long criteriaId) {
        platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProvisioningCriteria(criteriaId).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
