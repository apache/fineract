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
package org.apache.fineract.investor.api;

import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.BUY_BACK_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.CANCEL_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.CREATE_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.INTERMEDIARY_SALE_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.SALE_COMMAND_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandHandlerRegistry;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.apache.fineract.investor.api.search.ExternalAssetOwnersSearchApiDelegate;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.data.ExternalAssetOwnerLoanAttributesData;
import org.apache.fineract.investor.data.ExternalOwnerJournalEntryData;
import org.apache.fineract.investor.data.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferOwnerData;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerRequest;
import org.apache.fineract.investor.service.ExternalAssetOwnerLoanAttributesReadService;
import org.apache.fineract.investor.service.ExternalAssetOwnersReadService;
import org.apache.fineract.investor.service.search.domain.ExternalAssetOwnerSearchRequest;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceCommon;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Path("/v1/external-asset-owners")
@Component
@Tag(name = "External Asset Owners", description = "External Asset Owners")
@RequiredArgsConstructor
@Conditional(InvestorModuleIsEnabledCondition.class)
public class ExternalAssetOwnersApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final ExternalAssetOwnersReadService externalAssetOwnersReadService;
    private final DefaultToApiJsonSerializer<String> postApiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanReadPlatformServiceCommon loanReadPlatformService;
    private final ExternalAssetOwnersSearchApiDelegate delegate;
    private final ExternalAssetOwnerLoanAttributesReadService externalAssetOwnerLoanAttributesReadService;

    private static final String COMMAND_PARAM = "command";
    private static final String LOAN_ATTRIBUTES_GET_DESCRIPTION = "Retrieves the copy of the external asset owner loan product attributes "
            + "taken when the loan was first sold. Attributes the loan product gains or changes later do not affect a loan with a copy. "
            + "copiedFromTransferId is the id of the sale request (the pending transfer) whose execution created the copy. "
            + "A loan without a copy follows its loan product attributes and is returned with hasLoanLevelCopy=false and no attributes.";

    private static final CommandHandlerRegistry<String, Long, String, CommandWrapper> COMMAND_HANDLER_REGISTRY = new CommandHandlerRegistry<>(
            Map.of(CANCEL_COMMAND_VALUE, (id, json) -> new CommandWrapperBuilder().cancelTransactionByIdToExternalAssetOwner(id).build(),
                    INTERMEDIARY_SALE_COMMAND_VALUE,
                    (id, json) -> new CommandWrapperBuilder().withJson(json).intermediarySaleLoanToExternalAssetOwner(id).build(),
                    SALE_COMMAND_VALUE, (id, json) -> new CommandWrapperBuilder().withJson(json).saleLoanToExternalAssetOwner(id).build(),
                    BUY_BACK_COMMAND_VALUE,
                    (id, json) -> new CommandWrapperBuilder().withJson(json).buybackLoanToExternalAssetOwner(id).build(),
                    CREATE_COMMAND_VALUE, (id, json) -> new CommandWrapperBuilder().withJson(json).createExternalAssetOwner().build()));

    @POST
    @Path("/transfers/loans/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public CommandProcessingResult transferRequestWithLoanId(@PathParam("loanId") final Long loanId,
            @Parameter ExternalAssetOwnerRequest assetOwnerReq,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam) {
        platformUserRightsContext.isAuthenticated();
        final String serializedAssetRequest = postApiJsonSerializerService.serialize(assetOwnerReq);
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(commandParam, loanId, serializedAssetRequest,
                new UnrecognizedQueryParamException(COMMAND_PARAM, commandParam));
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("/transfers/loans/external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public CommandProcessingResult transferRequestWithLoanExternalId(@PathParam("loanExternalId") final String externalLoanId,
            @Parameter ExternalAssetOwnerRequest assetOwnerReq,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam) {
        platformUserRightsContext.isAuthenticated();
        final Long loanId = loanReadPlatformService.getLoanIdByLoanExternalId(externalLoanId);
        final String serializedAssetRequest = postApiJsonSerializerService.serialize(assetOwnerReq);
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(commandParam, loanId, serializedAssetRequest,
                new UnrecognizedQueryParamException(COMMAND_PARAM, commandParam));
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

    }

    @POST
    @Path("/transfers/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Transfer external asset", operationId = "transferRequestWithId")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public CommandProcessingResult transferRequestWithId(@PathParam("id") final Long id,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(commandParam, id, null,
                new UnrecognizedQueryParamException(COMMAND_PARAM, commandParam));
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("/transfers/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Transfer external asset by external ID", operationId = "transferRequestWithIdByExternalId")
    @AlternativeOperationId("transferRequestWithId_1")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public CommandProcessingResult transferRequestWithId(@PathParam("externalId") final String externalId,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam) {
        platformUserRightsContext.isAuthenticated();
        final Long id = externalAssetOwnersReadService.retrieveLastTransferIdByExternalId(new ExternalId(externalId));
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(commandParam, id, null,
                new UnrecognizedQueryParamException(COMMAND_PARAM, commandParam));
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("/transfers")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve External Asset Owner Transfers", description = "Retrieve External Asset Owner Transfer items by transferExternalId, loanId or loanExternalId")
    public Page<ExternalTransferData> getTransfers(
            @QueryParam("transferExternalId") @Parameter(description = "transferExternalId") final String transferExternalId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveTransferData(loanId, loanExternalId, transferExternalId, offset, limit);
    }

    @GET
    @Path("/transfers/active-transfer")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve Active Asset Owner Transfer", description = "Retrieve Active External Asset Owner Transfer by transferExternalId, loanId or loanExternalId")
    public ExternalTransferData getActiveTransfer(
            @QueryParam("transferExternalId") @Parameter(description = "transferExternalId") final String transferExternalId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveActiveTransferData(loanId, loanExternalId, transferExternalId);
    }

    @GET
    @Path("/transfers/{transferId}/journal-entries")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve Journal Entries of Transfer", description = "Retrieve Journal entries of transfer by transferId")
    public ExternalOwnerTransferJournalEntryData getJournalEntriesOfTransfer(
            @PathParam("transferId") @Parameter(description = "transferId") final Long transferId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveJournalEntriesOfTransfer(transferId, offset, limit);
    }

    @GET
    @Path("/owners/external-id/{ownerExternalId}/journal-entries")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve Journal Entries of Owner", description = "Retrieve Journal entries of owner by owner externalId")
    public ExternalOwnerJournalEntryData getJournalEntriesOfOwner(
            @PathParam("ownerExternalId") @Parameter(description = "ownerExternalId") final String ownerExternalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveJournalEntriesOfOwner(ownerExternalId, offset, limit);
    }

    @GET
    @Path("/loans/{loanId}/attributes")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve the loan-level external asset owner attributes of a loan", operationId = "retrieveExternalAssetOwnerLoanAttributes", description = LOAN_ATTRIBUTES_GET_DESCRIPTION)
    public ExternalAssetOwnerLoanAttributesData retrieveLoanAttributes(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnerLoanAttributesReadService.retrieveLoanAttributes(loanId);
    }

    @GET
    @Path("/loans/external-id/{loanExternalId}/attributes")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve the loan-level external asset owner attributes of a loan by loan external id", operationId = "retrieveExternalAssetOwnerLoanAttributesByLoanExternalId", description = LOAN_ATTRIBUTES_GET_DESCRIPTION)
    public ExternalAssetOwnerLoanAttributesData retrieveLoanAttributesByLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        platformUserRightsContext.isAuthenticated();
        final Long loanId = loanReadPlatformService.getLoanIdByLoanExternalId(loanExternalId);
        return externalAssetOwnerLoanAttributesReadService.retrieveLoanAttributes(loanId);
    }

    @PUT
    @Path("/loans/{loanId}/attributes")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PutExternalAssetOwnerLoanAttributeRequest.class)))
    @Operation(tags = {
            "External Asset Owners" }, summary = "Correct a loan-level external asset owner attribute", operationId = "updateExternalAssetOwnerLoanAttribute", description = "Exceptional correction tool, not part of the normal workflow. "
                    + "Sets a single external asset owner attribute on the loan-level copy of one loan. Validation, normalisation and error codes are the same as for "
                    + "the loan product attribute endpoint (PUT /external-asset-owners/loan-product/{loanProductId}/attributes/{id}). "
                    + "It affects only this loan and only activity from this point onwards; already posted journal entries and events are not changed. "
                    + "It is intended for use outside COB: relative to a COB run already in progress for the loan, when the correction takes effect is undefined "
                    + "and it may apply to only part of that run. "
                    + "The loan must already have a loan-level copy (taken when it was first sold), and SETTLEMENT_MODEL cannot be changed while a transfer is in progress. "
                    + "Requires the UPDATE_EXTERNAL_ASSET_OWNER_LOAN_ATTRIBUTE permission and supports maker-checker.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class)))
    @ApiResponse(responseCode = "403", description = "The loan has no loan-level copy, the attribute is invalid, or a transfer is in progress")
    public CommandProcessingResult updateLoanAttribute(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson)
                .updateExternalAssetOwnerLoanAttribute(loanId).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("/search")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Search External Asset Owner Transfers by text or date ranges to settlement or effective dates")
    public Page<ExternalTransferData> searchInvestorData(@Parameter PagedRequest<ExternalAssetOwnerSearchRequest> request) {
        platformUserRightsContext.isAuthenticated();
        return delegate.searchInvestorData(request);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an External Asset Owner using the External Id")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostExternalAssetOwnerRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostExternalAssetOwnerResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad requests due invalid json data")
    public CommandProcessingResult createExternalAssetOwner(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        final CommandWrapper commandRequest = COMMAND_HANDLER_REGISTRY.execute(CREATE_COMMAND_VALUE, null, apiRequestBodyAsJson,
                new UnrecognizedQueryParamException(COMMAND_PARAM, CREATE_COMMAND_VALUE));
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get all External Asset Owner with details")
    public List<ExternalTransferOwnerData> retrieveExternalAssetOwners() {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveAllExternalOwners();
    }
}
