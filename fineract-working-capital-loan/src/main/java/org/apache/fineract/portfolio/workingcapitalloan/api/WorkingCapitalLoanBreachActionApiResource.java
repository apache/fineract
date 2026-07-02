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

import static org.apache.fineract.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParameters.ACTION;

import com.google.gson.JsonElement;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanBreachActionReadService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@Tag(name = "Working Capital Loan Breach Actions", description = "Manages breach pause, resume, reschedule, reset and undo reset actions for Working Capital loans")
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachActionApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "WC_BREACH_ACTION";
    private static final String RESET_RESOURCE_NAME_FOR_PERMISSIONS = "WC_BREACH_RESET";

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanBreachActionReadService readService;
    private final WorkingCapitalLoanApplicationReadPlatformService loanReadPlatformService;

    @POST
    @Path("{loanId}/breach-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Breach Action", description = "Creates a breach action (pause, reschedule, resume, reset, undo_reset) for a Working Capital loan. A resume shortens the currently active pause to the current business date.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachActionApiResourceSwagger.PostWorkingCapitalLoansBreachActionRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachActionApiResourceSwagger.PostWorkingCapitalLoansBreachActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public CommandProcessingResult createBreachAction(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeBreachAction(loanId, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/breach-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createBreachActionByExternalId", summary = "Create Breach Action by external id", description = "Creates a breach action (pause, reschedule, resume, reset, undo_reset) for a Working Capital loan identified by external id.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachActionApiResourceSwagger.PostWorkingCapitalLoansBreachActionRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanBreachActionApiResourceSwagger.PostWorkingCapitalLoansBreachActionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public CommandProcessingResult createBreachAction(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createBreachAction(resolveExternalId(loanExternalId), apiRequestBodyAsJson);
    }

    @GET
    @Path("{loanId}/breach-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Breach Actions", description = "Retrieves all breach actions for a Working Capital loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanBreachActionData.class)))) })
    public List<WorkingCapitalLoanBreachActionData> retrieveBreachActions(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readService.retrieveBreachActions(loanId);
    }

    @GET
    @Path("external-id/{loanExternalId}/breach-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveBreachActionsByExternalId", summary = "Retrieve Breach Actions by external id", description = "Retrieves all breach actions for a Working Capital loan identified by external id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanBreachActionData.class)))) })
    public List<WorkingCapitalLoanBreachActionData> retrieveBreachActions(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        return retrieveBreachActions(resolveExternalId(loanExternalId));
    }

    private CommandProcessingResult executeBreachAction(final Long loanId, final String apiRequestBodyAsJson) {
        final String action = extractAction(apiRequestBodyAsJson);
        if (isResetAction(action)) {
            this.context.authenticatedUser().validateHasCreatePermission(RESET_RESOURCE_NAME_FOR_PERMISSIONS);
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson)
                .createWorkingCapitalLoanBreachAction(loanId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private String extractAction(final String apiRequestBodyAsJson) {
        if (!StringUtils.isNotBlank(apiRequestBodyAsJson)) {
            return null;
        }
        final JsonElement json = fromJsonHelper.parse(apiRequestBodyAsJson);
        return fromJsonHelper.extractStringNamed(ACTION, json);
    }

    private boolean isResetAction(final String action) {
        return CommandParameterUtil.is(action, "reset") || CommandParameterUtil.is(action, "undo_reset");
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
