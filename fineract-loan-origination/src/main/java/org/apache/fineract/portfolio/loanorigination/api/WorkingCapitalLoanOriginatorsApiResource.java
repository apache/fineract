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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorMappingResponse;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorsResponse;
import org.apache.fineract.portfolio.workingcapitalloan.loan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.loan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.loan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.loan.service.WorkingCapitalLoanOriginatorReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
@Tag(name = "Working Capital Loan Originators", description = "Fetch loan originator details for a specific working capital loan")
public class WorkingCapitalLoanOriginatorsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = WorkingCapitalLoanConstants.WCL_RESOURCE_NAME;

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanApplicationReadPlatformService workingCapitalLoanReadPlatformService;
    private final WorkingCapitalLoanOriginatorReadPlatformService workingCapitalLoanOriginatorReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Path("{loanId}/originators")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve originators for a working capital loan by loan ID", description = "Retrieves all originators attached to a specific working capital loan. Requires READ_WORKINGCAPITALLOAN permission.")
    @ApiResponse(responseCode = "200", description = "OK - Returns wrapped list of originators (may be empty)", content = @Content(schema = @Schema(implementation = LoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public LoanOriginatorsResponse retrieveOriginatorsByWorkingCapitalLoanId(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        if (!workingCapitalLoanReadPlatformService.existsByLoanId(loanId)) {
            throw new WorkingCapitalLoanNotFoundException(loanId);
        }

        return LoanOriginatorsResponse.of(this.workingCapitalLoanOriginatorReadPlatformService.retrieveByLoanId(loanId));
    }

    @GET
    @Path("external-id/{loanExternalId}/originators")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve originators for a working capital loan by loan external ID", description = "Retrieves all originators attached to a specific working capital loan using loan external ID. Requires READ_WORKINGCAPITALLOAN permission.")
    @ApiResponse(responseCode = "200", description = "OK - Returns wrapped list of originators (may be empty)", content = @Content(schema = @Schema(implementation = LoanOriginatorsResponse.class)))
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public LoanOriginatorsResponse retrieveOriginatorsByWorkingCapitalLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ExternalId externalId = ExternalIdFactory.produce(loanExternalId);
        Long resolvedLoanId = this.workingCapitalLoanReadPlatformService.getResolvedLoanId(externalId);
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(externalId);
        }

        return LoanOriginatorsResponse.of(this.workingCapitalLoanOriginatorReadPlatformService.retrieveByLoanId(resolvedLoanId));
    }

    @POST
    @Path("{loanId}/originators/{originatorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Attach originator to working capital loan by IDs", description = "Attaches an originator to a working capital loan. Loan must be in 'Submitted and Pending Approval' status. Requires ATTACH_WORKING_CAPITAL_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK - Originator attached", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status, originator not ACTIVE, duplicate mapping, or insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Loan or originator not found")
    public LoanOriginatorMappingResponse attachOriginatorToWorkingCapitalLoan(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().attachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return buildMappingResponse(result);
    }

    @POST
    @Path("{loanId}/originators/external-id/{originatorExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Attach originator to working capital loan by loan ID and originator external ID")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status, originator not ACTIVE, duplicate mapping")
    @ApiResponse(responseCode = "404", description = "Loan or originator not found")
    public LoanOriginatorMappingResponse attachOriginatorToWorkingCapitalLoanByOriginatorExternalId(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("originatorExternalId") @Parameter(description = "originatorExternalId") final String originatorExternalId) {

        final Long originatorId = this.workingCapitalLoanOriginatorReadPlatformService.resolveIdByExternalId(originatorExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().attachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    @POST
    @Path("external-id/{loanExternalId}/originators/{originatorId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Attach originator to working capital loan by loan external ID and originator ID")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status, originator not ACTIVE, duplicate mapping")
    @ApiResponse(responseCode = "404", description = "Loan or originator not found")
    public LoanOriginatorMappingResponse attachOriginatorToWorkingCapitalLoanByLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId) {

        final ExternalId externalId = ExternalIdFactory.produce(loanExternalId);
        final Long loanId = this.workingCapitalLoanReadPlatformService.getResolvedLoanId(externalId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(externalId);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().attachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    @POST
    @Path("external-id/{loanExternalId}/originators/external-id/{originatorExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Attach originator to working capital loan by loan external ID and originator external ID")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status, originator not ACTIVE, duplicate mapping")
    @ApiResponse(responseCode = "404", description = "Loan or originator not found")
    public LoanOriginatorMappingResponse attachOriginatorToWorkingCapitalLoanByBothExternalIds(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("originatorExternalId") @Parameter(description = "originatorExternalId") final String originatorExternalId) {

        final ExternalId loanExtId = ExternalIdFactory.produce(loanExternalId);
        final Long loanId = this.workingCapitalLoanReadPlatformService.getResolvedLoanId(loanExtId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(loanExtId);
        }

        final Long originatorId = this.workingCapitalLoanOriginatorReadPlatformService.resolveIdByExternalId(originatorExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().attachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    @DELETE
    @Path("{loanId}/originators/{originatorId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Detach originator from working capital loan by IDs", description = "Detaches an originator from a working capital loan. Loan must be in 'Submitted and Pending Approval' status. Requires DETACH_WORKING_CAPITAL_LOAN_ORIGINATOR permission.")
    @ApiResponse(responseCode = "200", description = "OK - Originator detached", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status or insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Loan or originator mapping not found")
    public LoanOriginatorMappingResponse detachOriginatorFromWorkingCapitalLoan(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().detachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    @DELETE
    @Path("{loanId}/originators/external-id/{originatorExternalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Detach originator from working capital loan by loan ID and originator external ID")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status")
    @ApiResponse(responseCode = "404", description = "Loan or originator mapping not found")
    public LoanOriginatorMappingResponse detachOriginatorFromWorkingCapitalLoanByOriginatorExternalId(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("originatorExternalId") @Parameter(description = "originatorExternalId") final String originatorExternalId) {

        final Long originatorId = this.workingCapitalLoanOriginatorReadPlatformService.resolveIdByExternalId(originatorExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().detachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    @DELETE
    @Path("external-id/{loanExternalId}/originators/{originatorId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Detach originator from working capital loan by loan external ID and originator ID")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status")
    @ApiResponse(responseCode = "404", description = "Loan or originator mapping not found")
    public LoanOriginatorMappingResponse detachOriginatorFromWorkingCapitalLoanByLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("originatorId") @Parameter(description = "originatorId") final Long originatorId) {

        final ExternalId externalId = ExternalIdFactory.produce(loanExternalId);
        final Long loanId = this.workingCapitalLoanReadPlatformService.getResolvedLoanId(externalId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(externalId);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().detachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    @DELETE
    @Path("external-id/{loanExternalId}/originators/external-id/{originatorExternalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Detach originator from working capital loan by loan external ID and originator external ID")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanOriginatorMappingResponse.class)))
    @ApiResponse(responseCode = "403", description = "Loan not in correct status")
    @ApiResponse(responseCode = "404", description = "Loan or originator mapping not found")
    public LoanOriginatorMappingResponse detachOriginatorFromWorkingCapitalLoanByBothExternalIds(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("originatorExternalId") @Parameter(description = "originatorExternalId") final String originatorExternalId) {

        final ExternalId loanExtId = ExternalIdFactory.produce(loanExternalId);
        final Long loanId = this.workingCapitalLoanReadPlatformService.getResolvedLoanId(loanExtId);
        if (loanId == null) {
            throw new WorkingCapitalLoanNotFoundException(loanExtId);
        }

        final Long originatorId = this.workingCapitalLoanOriginatorReadPlatformService.resolveIdByExternalId(originatorExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().detachWorkingCapitalLoanOriginator(loanId, originatorId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return buildMappingResponse(result);
    }

    private LoanOriginatorMappingResponse buildMappingResponse(final CommandProcessingResult result) {
        return LoanOriginatorMappingResponse.of(result.getResourceId(),
                result.getResourceExternalId() != null ? result.getResourceExternalId().getValue() : null, result.getSubResourceId(),
                result.getSubResourceExternalId() != null ? result.getSubResourceExternalId().getValue() : null);
    }
}
