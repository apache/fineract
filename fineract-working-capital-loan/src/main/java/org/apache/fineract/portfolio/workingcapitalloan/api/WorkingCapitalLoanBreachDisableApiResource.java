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
package org.apache.fineract.portfolio.workingcapitalloan.api;

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
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanBreachActionReadService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@Tag(name = "Working Capital Loan Breach Disable", description = "Manages breach disable / enable actions for Working Capital loans")
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachDisableApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "WC_BREACH_DISABLE";

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanBreachActionReadService readService;
    private final WorkingCapitalLoanApplicationReadPlatformService loanReadPlatformService;

    @POST
    @Path("{loanId}/breach-disable")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Breach Disable / Enable", description = "Disables or re-enables breach evaluation for a Working Capital loan. Both actions are applied as of the current business date; enable re-triggers and recomputes breach evaluation as of that date.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachDisableApiResourceSwagger.PostWorkingCapitalLoansBreachDisableRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachDisableApiResourceSwagger.PostWorkingCapitalLoansBreachDisableResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public CommandProcessingResult createBreachDisable(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.context.authenticatedUser().validateHasCreatePermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createWorkingCapitalLoanBreachDisable(loanId) //
                .withJson(apiRequestBodyAsJson) //
                .build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("external-id/{loanExternalId}/breach-disable")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createBreachDisableByExternalId", summary = "Create Breach Disable / Enable by external id", description = "Disables or re-enables breach evaluation for a Working Capital loan identified by external id.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachDisableApiResourceSwagger.PostWorkingCapitalLoansBreachDisableRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachDisableApiResourceSwagger.PostWorkingCapitalLoansBreachDisableResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public CommandProcessingResult createBreachDisable(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createBreachDisable(resolveExternalId(loanExternalId), apiRequestBodyAsJson);
    }

    @GET
    @Path("{loanId}/breach-disable")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Breach Disable / Enable Actions", description = "Retrieves all breach disable and enable actions for a Working Capital loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanBreachActionData.class)))),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public List<WorkingCapitalLoanBreachActionData> retrieveBreachDisableActions(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readService.retrieveBreachDisableActions(loanId);
    }

    @GET
    @Path("external-id/{loanExternalId}/breach-disable")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveBreachDisableActionsByExternalId", summary = "Retrieve Breach Disable / Enable Actions by external id", description = "Retrieves all breach disable and enable actions for a Working Capital loan identified by external id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanBreachActionData.class)))),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public List<WorkingCapitalLoanBreachActionData> retrieveBreachDisableActions(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        return retrieveBreachDisableActions(resolveExternalId(loanExternalId));
    }

    private Long resolveExternalId(final String loanExternalIdStr) {
        final ExternalId externalId = ExternalIdFactory.produce(loanExternalIdStr);
        final Long resolvedLoanId = loanReadPlatformService.getResolvedLoanId(externalId);
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(externalId);
        }
        return resolvedLoanId;
    }

}
