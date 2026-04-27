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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanDelinquencyReadPlatformService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/working-capital-loans")
@Tag(name = "Working Capital Loans", description = "Working Capital Loan applications")
@RequiredArgsConstructor
public class WorkingCapitalLoanApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = WorkingCapitalLoanConstants.WCL_RESOURCE_NAME;

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanApplicationReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final WorkingCapitalLoanDelinquencyReadPlatformService workingCapitalLoanDelinquencyReadPlatformService;

    @GET
    @Path("template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanTemplate", summary = "Retrieve Working Capital Loan application template", description = "Returns loan details plus productOptions, fundOptions, delinquencyBucketOptions, periodFrequencyTypeOptions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.GetWorkingCapitalLoansTemplateResponse.class))) })
    public WorkingCapitalLoanTemplateData retrieveTemplate(
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveTemplate(productId, clientId);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveAllWorkingCapitalLoans", summary = "List Working Capital Loans", description = "Uses Spring Data pagination: page, size, sort (e.g. sort=id,asc or sort=accountNumber,desc). "
            + "Filter by clientId, externalId, status, accountNo. Response: content, totalElements, totalPages, size, number.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.GetWorkingCapitalLoansPagedResponse.class))) })
    public Page<WorkingCapitalLoanData> retrieveAll(
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("status") @Parameter(description = "status") final String status,
            @Parameter(hidden = true) @Pagination(maximumSize = 200) final Pageable pageable) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveAllPaged(pageable, clientId, externalId, status, accountNo);
    }

    @GET
    @Path("{loanId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanById", summary = "Retrieve a Working Capital Loan", description = "Retrieves a Working Capital Loan by id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.GetWorkingCapitalLoansLoanIdResponse.class))) })
    public WorkingCapitalLoanData retrieveOne(@PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        return this.readPlatformService.retrieveOne(loanId);
    }

    @GET
    @Path("external-id/{loanExternalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanByExternalId", summary = "Retrieve a Working Capital Loan by external id", description = "Retrieves a Working Capital Loan by external id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.GetWorkingCapitalLoansLoanIdResponse.class))) })
    public WorkingCapitalLoanData retrieveOne(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final ExternalId externalId = ExternalIdFactory.produce(loanExternalId);
        return this.readPlatformService.retrieveOne(externalId);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "submitWorkingCapitalLoanApplication", summary = "Submit a Working Capital Loan application", description = "Creates a new Working Capital Loan application.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PostWorkingCapitalLoansRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PostWorkingCapitalLoansResponse.class))) })
    public CommandProcessingResult submitLoanApplication(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createWorkingCapitalLoanApplication()
                .withJson(apiRequestBodyAsJson).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "modifyWorkingCapitalLoanApplicationById", summary = "Modify a Working Capital Loan application", description = "Loan application can only be modified when in 'Submitted and pending approval' state.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult modifyLoanApplicationById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return modifyLoanApplication(loanId, null, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("{loanId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "deleteWorkingCapitalLoanApplication", summary = "Delete a Working Capital Loan application", description = "Only loans in \"Submitted and awaiting approval\" status can be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.DeleteWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult deleteLoanApplication(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId) {
        return deleteLoanApplication(loanId, null);
    }

    @PUT
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "modifyWorkingCapitalLoanApplicationByExternalId", summary = "Modify a Working Capital Loan application by external id", description = "Loan application can only be modified when in 'Submitted and pending approval' state.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult modifyLoanApplicationByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return modifyLoanApplication(null, loanExternalId, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("external-id/{loanExternalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "deleteWorkingCapitalLoanApplicationByExternalId", summary = "Delete a Working Capital Loan application by external id", description = "Only loans in \"Submitted and awaiting approval\" status can be deleted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.DeleteWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult deleteLoanApplication(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId) {
        return deleteLoanApplication(null, loanExternalId);
    }

    @GET
    @Path("{loanId}/delinquencyrangetags")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the Loan Delinquency Tag history using the Loan Id", description = "", operationId = "getDelinquencyRangeScheduleTagHistoryById")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse.class)))) })
    public List<WorkingCapitalLoanDelinquencyTagHistoryData> getDelinquencyRangeScheduleTagHistoryById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId, @Context final UriInfo uriInfo) {
        return getDelinquencyRangeScheduleTagHistory(loanId, null, uriInfo);
    }

    @GET
    @Path("external-id/{externalId}/delinquencyrangetags")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the Loan Delinquency Tag history using the Loan External Id", description = "", operationId = "getDelinquencyRangeScheduleTagHistoryByExternalId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.GetWorkingCapitalLoanDelinquencyRangeScheduleTagHistoryResponse.class)))) })
    public List<WorkingCapitalLoanDelinquencyTagHistoryData> getDelinquencyRangeScheduleTagHistoryById(
            @PathParam("externalId") @Parameter(description = "externalId", required = true) final String loanExternalId,
            @Context final UriInfo uriInfo) {
        return getDelinquencyRangeScheduleTagHistory(null, loanExternalId, uriInfo);
    }

    private List<WorkingCapitalLoanDelinquencyTagHistoryData> getDelinquencyRangeScheduleTagHistory(final Long loanId,
            final String loanExternalIdStr, final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission("DELINQUENCY_TAGS");
        final Long resolvedLoanId = loanId == null ? readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr))
                : loanId;
        return workingCapitalLoanDelinquencyReadPlatformService.retrieveDelinquencyRangeScheduleTagHistory(resolvedLoanId);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "stateTransitionWorkingCapitalLoanById", summary = "Approve/Reject/Undo-approve/Disburse/Undo-disburse a Working Capital Loan", description = "Mandatory command query parameter: approve, reject, undoapproval, disburse, or undodisbursal.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PostWorkingCapitalLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PostWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult stateTransitionById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return handleStateTransition(loanId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "stateTransitionWorkingCapitalLoanByExternalId", summary = "Approve/Reject/Undo-approve/Disburse/Undo-disburse a Working Capital Loan by external id", description = "Mandatory command query parameter: approve, reject, undoapproval, disburse, or undodisbursal.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PostWorkingCapitalLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PostWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult stateTransitionByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @QueryParam("command") @Parameter(description = "command", required = true) final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return handleStateTransition(null, loanExternalId, commandParam, apiRequestBodyAsJson);
    }

    private CommandProcessingResult modifyLoanApplication(final Long loanId, final String loanExternalIdStr,
            final String apiRequestBodyAsJson) {
        final Long resolvedLoanId = loanId != null ? loanId
                : readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalIdStr));
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).withLoanId(resolvedLoanId)
                .updateWorkingCapitalLoanApplication().build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private CommandProcessingResult deleteLoanApplication(final Long loanId, final String loanExternalIdStr) {
        final Long resolvedLoanId = loanId != null ? loanId
                : readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalIdStr));
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withLoanId(resolvedLoanId).deleteWorkingCapitalLoanApplication()
                .build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    private CommandProcessingResult handleStateTransition(final Long loanId, final String loanExternalIdStr, final String commandParam,
            final String apiRequestBodyAsJson) {
        final Long resolvedLoanId = loanId != null ? loanId
                : readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalIdStr));
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandWrapper commandRequest = null;
        if (CommandParameterUtil.is(commandParam, "approve")) {
            commandRequest = builder.approveWorkingCapitalLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "reject")) {
            commandRequest = builder.rejectWorkingCapitalLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "undoapproval")) {
            commandRequest = builder.undoWorkingCapitalLoanApplicationApproval(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "disburse")) {
            commandRequest = builder.disburseWorkingCapitalLoanApplication(resolvedLoanId).build();
        } else if (CommandParameterUtil.is(commandParam, "undodisbursal")) {
            commandRequest = builder.undoWorkingCapitalLoanApplicationDisbursal(resolvedLoanId).build();
        }

        if (commandRequest == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{loanId}/discount")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "updateWorkingCapitalLoanDiscountById", summary = "Update discount for a disbursed Working Capital Loan", description = "Discount can be added one time after disbursement and only on disbursement date.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdDiscountRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult updateDiscountById(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return updateDiscount(loanId, null, apiRequestBodyAsJson);
    }

    @PUT
    @Path("external-id/{loanExternalId}/discount")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "updateWorkingCapitalLoanDiscountByExternalId", summary = "Update discount for a disbursed Working Capital Loan by external id", description = "Discount can be added one time after disbursement and only on disbursement date.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdDiscountRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanApiResourceSwagger.PutWorkingCapitalLoansLoanIdResponse.class))) })
    public CommandProcessingResult updateDiscountByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return updateDiscount(null, loanExternalId, apiRequestBodyAsJson);
    }

    private CommandProcessingResult updateDiscount(final Long loanId, final String loanExternalIdStr, final String apiRequestBodyAsJson) {
        final Long resolvedLoanId = loanId != null ? loanId
                : readPlatformService.getResolvedLoanId(ExternalIdFactory.produce(loanExternalIdStr));
        if (resolvedLoanId == null) {
            throw new WorkingCapitalLoanNotFoundException(ExternalIdFactory.produce(loanExternalIdStr));
        }
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson)
                .updateDiscountWorkingCapitalLoanApplication(resolvedLoanId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
