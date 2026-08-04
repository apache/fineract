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

import static org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_COMMAND;
import static org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants.GOODWILL_CREDIT_LOAN_COMMAND;
import static org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants.PAYOUT_REFUND_COMMAND;
import static org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants.REPAYMENT_LOAN_COMMAND;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.jersey.Pagination;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanTransactionNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanTransactionReadPlatformService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/working-capital-loans")
@Tag(name = "Working Capital Loan Transactions", description = "Working Capital Loan Transactions (e.g. disbursements).")
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = WorkingCapitalLoanConstants.WCL_RESOURCE_NAME;

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanApplicationReadPlatformService loanReadPlatformService;
    private final WorkingCapitalLoanTransactionReadPlatformService transactionReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;

    @GET
    @Path("{loanId}/transactions")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTransactionsById", summary = "Retrieve transactions", description = "Retrieves transactions of a Working Capital Loan.\n\nExample: working-capital-loans/1/transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionsResponse.class))) })
    public Page<WorkingCapitalLoanTransactionData> retrieveTransactionsByLoanId(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @Parameter(hidden = true) @Pagination final Pageable pageable) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.transactionReadPlatformService.retrieveTransactions(loanId, pageable);
    }

    @GET
    @Path("external-id/{loanExternalId}/transactions")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTransactionsByExternalId", summary = "Retrieve transactions by loan external id", description = "Retrieves transactions of a Working Capital Loan by loan external id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionsResponse.class))) })
    public Page<WorkingCapitalLoanTransactionData> retrieveTransactionsByExternalLoanId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @Parameter(hidden = true) @Pagination final Pageable pageable) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.transactionReadPlatformService.retrieveTransactions(ExternalIdFactory.produce(loanExternalId), pageable);
    }

    @GET
    @Path("{loanId}/transactions/{transactionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTransactionById", summary = "Retrieve a transaction", description = "Retrieves a single Working Capital Loan transaction.\n\nExample: working-capital-loans/1/transactions/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionIdResponse.class))) })
    public WorkingCapitalLoanTransactionData retrieveTransactionByLoanIdAndTransactionId(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @PathParam("transactionId") @Parameter(description = "transactionId", required = true) final Long transactionId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.transactionReadPlatformService.retrieveTransaction(loanId, transactionId);
    }

    @GET
    @Path("{loanId}/transactions/external-id/{externalTransactionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTransactionByExternalTransactionId", summary = "Retrieve a transaction by external id", description = "Retrieves a single Working Capital Loan transaction by loan id and transaction external id.\n\nExample: working-capital-loans/1/transactions/external-id/txn-ext-001")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionIdResponse.class))) })
    public WorkingCapitalLoanTransactionData retrieveTransactionByLoanIdAndTransactionExternalId(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @PathParam("externalTransactionId") @Parameter(description = "externalTransactionId", required = true) final String externalTransactionId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.transactionReadPlatformService.retrieveTransaction(loanId, ExternalIdFactory.produce(externalTransactionId));
    }

    @GET
    @Path("external-id/{loanExternalId}/transactions/{transactionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTransactionByExternalLoanIdAndTransactionId", summary = "Retrieve a transaction by loan external id and transaction id", description = "Retrieves a single Working Capital Loan transaction by loan external id and transaction id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionIdResponse.class))) })
    public WorkingCapitalLoanTransactionData retrieveTransactionByExternalLoanIdAndTransactionId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @PathParam("transactionId") @Parameter(description = "transactionId", required = true) final Long transactionId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.transactionReadPlatformService.retrieveTransaction(ExternalIdFactory.produce(loanExternalId), transactionId);
    }

    @GET
    @Path("external-id/{loanExternalId}/transactions/external-id/{externalTransactionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTransactionByExternalLoanIdAndExternalTransactionId", summary = "Retrieve a transaction by loan and transaction external ids", description = "Retrieves a single Working Capital Loan transaction by loan external id and transaction external id.\n\nExample: working-capital-loans/external-id/loan-ext-001/transactions/external-id/txn-ext-001")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.GetWorkingCapitalLoanTransactionIdResponse.class))) })
    public WorkingCapitalLoanTransactionData retrieveTransactionByExternalLoanIdAndTransactionExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @PathParam("externalTransactionId") @Parameter(description = "externalTransactionId", required = true) final String externalTransactionId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.transactionReadPlatformService.retrieveTransaction(ExternalIdFactory.produce(loanExternalId),
                ExternalIdFactory.produce(externalTransactionId));
    }

    @GET
    @Path("{loanId}/template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanActionTemplate", summary = "Retrieve Working Capital Loan action template", description = "Returns loan data for applying the proper loan action")
    public WorkingCapitalLoanCommandTemplateData retrieveWorkingCapitalLoanTemplate(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return handleLoanTransactionTemplate(loanId, null, templateType);
    }

    private WorkingCapitalLoanCommandTemplateData handleLoanTransactionTemplate(final Long loanId, final String loanExternalIdStr,
            final String templateType) {
        final Long resolvedLoanId = resolveLoanId(loanId, loanExternalIdStr);

        final WorkingCapitalLoanCommandTemplateData loanTransactionTemplateData = transactionReadPlatformService
                .retrieveLoanTransactionTemplate(resolvedLoanId, templateType);
        if (loanTransactionTemplateData == null) {
            throw new UnrecognizedQueryParamException("command", templateType);
        }

        return loanTransactionTemplateData;
    }

    @POST
    @Path("{loanId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "executeWorkingCapitalLoanTransactionById", summary = "Execute Working Capital Loan transaction", description = "Supported command query parameter: repayment, creditBalanceRefund, discountFee, discountFeeAdjustment")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.PostWorkingCapitalLoanTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.PostWorkingCapitalLoanTransactionsResponse.class))) })
    public CommandProcessingResult executeLoanTransactionById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeTransaction(loanId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "executeWorkingCapitalLoanTransactionByExternalId", summary = "Execute Working Capital Loan transaction by external id", description = "Supported command query parameter: repayment, creditBalanceRefund, discountFee, discountFeeAdjustment")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.PostWorkingCapitalLoanTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.PostWorkingCapitalLoanTransactionsResponse.class))) })
    public CommandProcessingResult executeLoanTransactionByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeTransaction(null, loanExternalId, commandParam, apiRequestBodyAsJson);
    }

    private CommandProcessingResult executeTransaction(final Long loanId, final String loanExternalIdStr, final String commandParam,
            final String apiRequestBodyAsJson) {
        final Long resolvedLoanId = resolveLoanId(loanId, loanExternalIdStr);

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        final CommandWrapper commandRequest;
        if (CommandParameterUtil.is(commandParam, REPAYMENT_LOAN_COMMAND)) {
            commandRequest = builder.repaymentWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, CREDIT_BALANCE_REFUND_COMMAND)) {
            commandRequest = builder.creditBalanceRefundWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, PAYOUT_REFUND_COMMAND)) {
            commandRequest = builder.payoutRefundWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, GOODWILL_CREDIT_LOAN_COMMAND)) {
            commandRequest = builder.goodwillCreditWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.DISCOUNT_FEE_LOAN_COMMAND)) {
            commandRequest = builder.discountFeeWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.DISCOUNT_FEE_ADJUSTMENT_LOAN_COMMAND)) {
            commandRequest = builder.discountFeeAdjustmentWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.CHARGE_OFF_LOAN_COMMAND)) {
            commandRequest = builder.chargeOffWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.UNDO_CHARGE_OFF_LOAN_COMMAND)) {
            commandRequest = builder.undoChargeOffWorkingCapitalLoanTransaction(resolvedLoanId).build();
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private Long resolveLoanId(Long loanId, String loanExternalId) {
        final Long resolvedLoanId = loanId != null ? loanId
                : loanReadPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalId));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalId));
        }
        return resolvedLoanId;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId", summary = "Execute Working Capital Loan transaction command by loan id and transaction id", description = "Supported command query parameter: undo")
    @Path("{loanId}/transactions/{transactionId}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandResponse.class))) })
    public CommandProcessingResult executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @PathParam("transactionId") @Parameter(description = "transactionId", required = true) final Long transactionId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String command,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeWorkingCapitalLoanTransactionCommand(loanId, null, transactionId, null, command, apiRequestBodyAsJson);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionExternalId", summary = "Execute Working Capital Loan transaction command by loan id and transaction external id", description = "Supported command query parameter: undo")
    @Path("{loanId}/transactions/external-id/{transactionExternalId}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandResponse.class))) })
    public CommandProcessingResult executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionExternalId(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @PathParam("transactionExternalId") @Parameter(description = "transactionExternalId", required = true) final String transactionExternalId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String command,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeWorkingCapitalLoanTransactionCommand(loanId, null, null, transactionExternalId, command, apiRequestBodyAsJson);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "executeWorkingCapitalLoanTransactionCommandByLoanExternalIdTransactionId", summary = "Execute Working Capital Loan transaction command by loan external id and transaction id", description = "Supported command query parameter: undo")
    @Path("external-id/{loanExternalId}/transactions/{transactionId}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandResponse.class))) })
    public CommandProcessingResult executeWorkingCapitalLoanTransactionCommandByLoanExternalIdTransactionId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @PathParam("transactionId") @Parameter(description = "transactionId", required = true) final Long transactionId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String command,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeWorkingCapitalLoanTransactionCommand(null, loanExternalId, transactionId, null, command, apiRequestBodyAsJson);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "executeWorkingCapitalLoanTransactionCommandByLoanExternalIdTransactionExternalId", summary = "Execute Working Capital Loan transaction command by loan external id and transaction external id", description = "Supported command query parameter: undo")
    @Path("external-id/{loanExternalId}/transactions/external-id/{transactionExternalId}")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanTransactionsApiResourceSwagger.ExecuteWorkingCapitalLoanTransactionCommandResponse.class))) })
    public CommandProcessingResult executeWorkingCapitalLoanTransactionCommandByLoanExternalIdTransactionExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @PathParam("transactionExternalId") @Parameter(description = "transactionExternalId", required = true) final String transactionExternalId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String command,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return executeWorkingCapitalLoanTransactionCommand(null, loanExternalId, null, transactionExternalId, command,
                apiRequestBodyAsJson);
    }

    private CommandProcessingResult executeWorkingCapitalLoanTransactionCommand(Long loanId, String loanExternalId, Long transactionId,
            String transactionExternalId, String command, String apiRequestBodyAsJson) {
        final Long resolvedLoanId = resolveLoanId(loanId, loanExternalId);
        final Long resolvedTransactionId = resolveTransactionId(resolvedLoanId, transactionId, transactionExternalId);
        final String commandParam = command == null ? null : command.trim().toLowerCase();
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        final CommandWrapper commandRequest;
        if (CommandParameterUtil.is(commandParam, WorkingCapitalLoanConstants.UNDO_COMMAND)) {
            commandRequest = builder.undoWorkingCapitalLoanTransaction(resolvedLoanId, resolvedTransactionId).build();
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private Long resolveTransactionId(Long loanId, Long transactionId, String transactionExternalId) {
        if (transactionId != null) {
            return transactionId;
        }
        ExternalId externalId = ExternalIdFactory.produce(transactionExternalId);
        return transactionRepository.findByWcLoan_IdAndExternalId(loanId, externalId).map(WorkingCapitalLoanTransaction::getId)
                .orElseThrow(() -> new WorkingCapitalLoanTransactionNotFoundException(externalId));
    }
}
