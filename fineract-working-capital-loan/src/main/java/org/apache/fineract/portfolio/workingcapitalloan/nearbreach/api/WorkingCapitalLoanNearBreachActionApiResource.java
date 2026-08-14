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
package org.apache.fineract.portfolio.workingcapitalloan.nearbreach.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.loan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.loan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.nearbreach.data.WorkingCapitalLoanNearBreachActionData;
import org.apache.fineract.portfolio.workingcapitalloan.nearbreach.service.WorkingCapitalLoanNearBreachActionReadService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@Tag(name = "Working Capital Loan Near Breach Actions", description = "Manages near breach actions for Working Capital loans")
@RequiredArgsConstructor
public class WorkingCapitalLoanNearBreachActionApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "WC_NEAR_BREACH_ACTION";

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanNearBreachActionReadService nearBreachActionReadService;
    private final WorkingCapitalLoanApplicationReadPlatformService readPlatformService;

    @POST
    @Path("{loanId}/near-breach-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createWorkingCapitalLoanNearBreachActionById", summary = "Create a near breach action for an active Working Capital Loan", description = "Creates a near breach action (reschedule) for a Working Capital loan.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanNearBreachActionApiResourceSwagger.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.class)))
    public CommandProcessingResult createNearBreachActionById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createNearBreachAction(loanId, null, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/near-breach-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createWorkingCapitalLoanNearBreachActionByExternalId", summary = "Create a near breach action for an active Working Capital Loan by external id", description = "Creates a near breach action (reschedule) for a Working Capital loan.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanNearBreachActionApiResourceSwagger.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.class)))
    public CommandProcessingResult createNearBreachActionByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createNearBreachAction(null, loanExternalId, apiRequestBodyAsJson);
    }

    private CommandProcessingResult createNearBreachAction(final Long loanId, final String loanExternalIdStr,
            final String apiRequestBodyAsJson) {
        final Long resolvedLoanId = loanId != null ? loanId
                : readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalIdStr));
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson)
                .createNearBreachActionWorkingCapitalLoan(resolvedLoanId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("{loanId}/near-breach-actions")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "getWorkingCapitalLoanNearBreachActionsById", summary = "Retrieve near breach actions for a Working Capital Loan", description = "Returns all near breach action records for the loan, ordered by most recent first.")
    public List<WorkingCapitalLoanNearBreachActionData> getNearBreachActionsById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.nearBreachActionReadService.retrieveNearBreachActions(loanId);
    }

    @GET
    @Path("external-id/{loanExternalId}/near-breach-actions")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "getWorkingCapitalLoanNearBreachActionsByExternalId", summary = "Retrieve near breach actions for a Working Capital Loan by external id", description = "Returns all near breach action records for the loan, ordered by most recent first.")
    public List<WorkingCapitalLoanNearBreachActionData> getNearBreachActionsByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Long resolvedLoanId = readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalId));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalId));
        }
        return this.nearBreachActionReadService.retrieveNearBreachActions(resolvedLoanId);
    }
}
