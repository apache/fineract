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

import static org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParameters.ACTION;

import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyActionData;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanDelinquencyActionReadService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@Tag(name = "Working Capital Loan Delinquency Actions", description = "Manages delinquency pause, resume, reschedule, disable and enable actions for Working Capital loans")
@RequiredArgsConstructor
public class WorkingCapitalLoanDelinquencyActionApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "WC_DELINQUENCY_ACTION";
    private static final String DISABLE_RESOURCE_NAME_FOR_PERMISSIONS = "WC_DELINQUENCY_DISABLE";
    private static final String RESET_RESOURCE_NAME_FOR_PERMISSIONS = "WC_DELINQUENCY_RESET";
    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_BAD_REQUEST = "Bad Request";
    private static final String RESPONSE_LOAN_NOT_FOUND = "Working Capital Loan not found";

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanDelinquencyActionReadService readService;
    private final WorkingCapitalLoanApplicationReadPlatformService loanReadPlatformService;

    @POST
    @Path("{loanId}/delinquency-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create Delinquency Action", description = "Creates a delinquency action (pause, reschedule, resume, disable, enable) for a Working Capital loan. A disable stops delinquency evaluation as of the current business date; an enable re-triggers and recomputes delinquency evaluation as of that date.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanDelinquencyActionApiResourceSwagger.PostWorkingCapitalLoansDelinquencyActionRequest.class)))
    @ApiResponse(responseCode = "200", description = RESPONSE_OK, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanDelinquencyActionApiResourceSwagger.PostWorkingCapitalLoansDelinquencyActionResponse.class)))
    @ApiResponse(responseCode = "400", description = RESPONSE_BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = RESPONSE_LOAN_NOT_FOUND)
    public CommandProcessingResult createDelinquencyAction(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.context.authenticatedUser().validateHasCreatePermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final String action = extractAction(apiRequestBodyAsJson);
        if (isDisableOrEnableAction(action)) {
            this.context.authenticatedUser().validateHasCreatePermission(DISABLE_RESOURCE_NAME_FOR_PERMISSIONS);
        }
        if (isResetAction(action)) {
            this.context.authenticatedUser().validateHasCreatePermission(RESET_RESOURCE_NAME_FOR_PERMISSIONS);
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createWorkingCapitalLoanDelinquencyAction(loanId) //
                .withJson(apiRequestBodyAsJson) //
                .build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("external-id/{loanExternalId}/delinquency-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "createDelinquencyActionByExternalId", summary = "Create Delinquency Action by external id", description = "Creates a delinquency action (pause, reschedule, resume, disable, enable) for a Working Capital loan identified by external id.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanDelinquencyActionApiResourceSwagger.PostWorkingCapitalLoansDelinquencyActionRequest.class)))
    @ApiResponse(responseCode = "200", description = RESPONSE_OK, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanDelinquencyActionApiResourceSwagger.PostWorkingCapitalLoansDelinquencyActionResponse.class)))
    @ApiResponse(responseCode = "400", description = RESPONSE_BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = RESPONSE_LOAN_NOT_FOUND)
    public CommandProcessingResult createDelinquencyAction(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return createDelinquencyAction(resolveExternalId(loanExternalId), apiRequestBodyAsJson);
    }

    @GET
    @Path("{loanId}/delinquency-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Delinquency Actions", description = "Retrieves all delinquency actions for a Working Capital loan")
    @ApiResponse(responseCode = "200", description = RESPONSE_OK, content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanDelinquencyActionData.class))))
    public List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActions(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return readService.retrieveDelinquencyActions(loanId);
    }

    @GET
    @Path("external-id/{loanExternalId}/delinquency-actions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveDelinquencyActionsByExternalId", summary = "Retrieve Delinquency Actions by external id", description = "Retrieves all delinquency actions for a Working Capital loan identified by external id")
    @ApiResponse(responseCode = "200", description = RESPONSE_OK, content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanDelinquencyActionData.class))))
    public List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActions(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        return retrieveDelinquencyActions(resolveExternalId(loanExternalId));
    }

    private String extractAction(final String apiRequestBodyAsJson) {
        if (StringUtils.isBlank(apiRequestBodyAsJson)) {
            return null;
        }
        final JsonElement json = fromJsonHelper.parse(apiRequestBodyAsJson);
        return fromJsonHelper.extractStringNamed(ACTION, json);
    }

    private boolean isDisableOrEnableAction(final String action) {
        return CommandParameterUtil.is(action, "disable") || CommandParameterUtil.is(action, "enable");
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
