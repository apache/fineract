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
package org.apache.fineract.portfolio.loanorigination.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorTemplateData;
import org.apache.fineract.portfolio.loanorigination.service.LoanOriginatorReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Path("/v1/loan-originators")
@Component
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
@Tag(name = "Loan Originators", description = "Manage loan originator details for revenue sharing and reporting")
@RequiredArgsConstructor
public class LoanOriginatorApiResource {

    private final PlatformSecurityContext context;
    private final LoanOriginatorReadPlatformService loanOriginatorReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new loan originator", description = "Creates a new loan originator record. Requires CREATE_LOAN_ORIGINATOR permission.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.PostLoanOriginatorsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.PostLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "400", description = "Required parameter is missing or incorrect format")
    @ApiResponse(responseCode = "403", description = "Duplicate external ID or insufficient permissions")
    public CommandProcessingResult create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanOriginator().withJson(apiRequestBodyAsJson).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all loan originators", description = "Retrieves all loan originator records. Requires READ_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.GetLoanOriginatorsResponse.class))))
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public List<LoanOriginatorData> retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(LoanOriginatorApiConstants.RESOURCE_NAME);

        return this.loanOriginatorReadPlatformService.retrieveAll();
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get loan originator template data", description = "Retrieves the Loan Originator template data")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.GetLoanOriginatorTemplateResponse.class)))
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public LoanOriginatorTemplateData retrieveLoanOriginatorTemplate() {
        this.context.authenticatedUser().validateHasReadPermission(LoanOriginatorApiConstants.RESOURCE_NAME);

        return this.loanOriginatorReadPlatformService.retrieveTemplate();
    }

    @GET
    @Path("{originatorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a loan originator by ID", description = "Retrieves a loan originator by its internal ID. Requires READ_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.GetLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Originator not found")
    public LoanOriginatorData retrieveOne(@PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId) {
        this.context.authenticatedUser().validateHasReadPermission(LoanOriginatorApiConstants.RESOURCE_NAME);

        return this.loanOriginatorReadPlatformService.retrieveById(originatorId);
    }

    @GET
    @Path("external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a loan originator by external ID", description = "Retrieves a loan originator by its external ID. Requires READ_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.GetLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Originator not found")
    public LoanOriginatorData retrieveByExternalId(
            @PathParam("externalId") @Parameter(description = "externalId") final String externalId) {
        this.context.authenticatedUser().validateHasReadPermission(LoanOriginatorApiConstants.RESOURCE_NAME);

        return this.loanOriginatorReadPlatformService.retrieveByExternalId(externalId);
    }

    @PUT
    @Path("{originatorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a loan originator by ID", description = "Updates a loan originator by its internal ID. Requires UPDATE_LOAN_ORIGINATOR permission.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.PutLoanOriginatorsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.PutLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "400", description = "Incorrect format")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Originator not found")
    public CommandProcessingResult update(@PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanOriginator(originatorId).withJson(apiRequestBodyAsJson)
                .build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a loan originator by external ID", description = "Updates a loan originator by its external ID. Requires UPDATE_LOAN_ORIGINATOR permission.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.PutLoanOriginatorsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.PutLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "400", description = "Incorrect format")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Originator not found")
    public CommandProcessingResult updateByExternalId(
            @PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final Long originatorId = this.loanOriginatorReadPlatformService.resolveIdByExternalId(externalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanOriginator(originatorId).withJson(apiRequestBodyAsJson)
                .build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{originatorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a loan originator by ID", description = "Deletes a loan originator by its internal ID. Requires DELETE_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.DeleteLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "403", description = "Originator is mapped to loans and cannot be deleted, or insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Originator not found")
    public CommandProcessingResult delete(@PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanOriginator(originatorId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a loan originator by external ID", description = "Deletes a loan originator by its external ID. Requires DELETE_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorApiResourceSwagger.DeleteLoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "403", description = "Originator is mapped to loans and cannot be deleted, or insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Originator not found")
    public CommandProcessingResult deleteByExternalId(
            @PathParam("externalId") @Parameter(description = "externalId") final String externalId) {
        final Long originatorId = this.loanOriginatorReadPlatformService.resolveIdByExternalId(externalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanOriginator(originatorId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
