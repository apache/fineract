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
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.INTERMEDIARY_SALE_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.SALE_COMMAND_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.apache.fineract.investor.api.search.ExternalAssetOwnersSearchApiDelegate;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.data.ExternalAssetOwnerCreateResponse;
import org.apache.fineract.investor.data.ExternalAssetOwnerTransferResponse;
import org.apache.fineract.investor.data.ExternalOwnerJournalEntryResponse;
import org.apache.fineract.investor.data.ExternalOwnerTransferJournalEntryResponse;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferOwnerData;
import org.apache.fineract.investor.data.ExternalTransferResponse;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerBuybackRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerCancelRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerCreateRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerIntermediarySaleRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerSaleRequest;
import org.apache.fineract.investor.service.ExternalAssetOwnerReadMapper;
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

    private final ExternalAssetOwnerReadMapper externalAssetOwnerReadMapper;
    private final PlatformUserRightsContext platformUserRightsContext;
    private final ExternalAssetOwnersReadService externalAssetOwnersReadService;
    private final ExternalAssetOwnersSearchApiDelegate delegate;
    private final CommandDispatcher commandDispatcher;
    private final LoanReadPlatformServiceCommon loanReadPlatformService;
    private final Validator validator;

    private static final String COMMAND_PARAM = "command";

    @POST
    @Path("/transfers/loans/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerSaleRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnerTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public ExternalAssetOwnerTransferResponse transferRequestWithLoanId(@PathParam("loanId") final Long loanId,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam,
            final ExternalAssetOwnerSaleRequest request) {
        platformUserRightsContext.isAuthenticated();
        request.setLoanId(loanId);
        request.setCommand(StringUtils.trimToEmpty(commandParam));
        validate(request);
        return (ExternalAssetOwnerTransferResponse) commandDispatcher.dispatch(buildTransferCommand(commandParam, request)).get();
    }

    @POST
    @Path("/transfers/loans/external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerSaleRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnerTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public ExternalAssetOwnerTransferResponse transferRequestWithLoanExternalId(@PathParam("loanExternalId") final String externalLoanId,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam,
            final ExternalAssetOwnerSaleRequest request) {
        platformUserRightsContext.isAuthenticated();
        request.setLoanId(loanReadPlatformService.getLoanIdByLoanExternalId(externalLoanId));
        request.setCommand(StringUtils.trimToEmpty(commandParam));
        validate(request);
        return (ExternalAssetOwnerTransferResponse) commandDispatcher.dispatch(buildTransferCommand(commandParam, request)).get();
    }

    @POST
    @Path("/transfers/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Transfer external asset", operationId = "transferRequestWithId")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnerTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public ExternalAssetOwnerTransferResponse transferRequestWithId(@PathParam("id") final Long id,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam) {
        platformUserRightsContext.isAuthenticated();
        final var request = ExternalAssetOwnerCancelRequest.builder().transferId(id).build();
        final var command = new Command<ExternalAssetOwnerCancelRequest>();
        command.setPayload(request);
        return (ExternalAssetOwnerTransferResponse) commandDispatcher.dispatch(command).get();
    }

    @POST
    @Path("/transfers/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Transfer external asset by external ID", operationId = "transferRequestWithIdByExternalId")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnerTransferResponse.class)))
    @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated")
    public ExternalAssetOwnerTransferResponse transferRequestWithId(@PathParam("externalId") final String externalId,
            @QueryParam(COMMAND_PARAM) @Parameter(description = COMMAND_PARAM) final String commandParam) {
        platformUserRightsContext.isAuthenticated();
        final Long id = externalAssetOwnersReadService.retrieveLastTransferIdByExternalId(new ExternalId(externalId));
        final var request = ExternalAssetOwnerCancelRequest.builder().transferId(id).build();
        final var command = new Command<ExternalAssetOwnerCancelRequest>();
        command.setPayload(request);
        return (ExternalAssetOwnerTransferResponse) commandDispatcher.dispatch(command).get();
    }

    @GET
    @Path("/transfers")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve External Asset Owner Transfers", description = "Retrieve External Asset Owner Transfer items by transferExternalId, loanId or loanExternalId")
    public Page<ExternalTransferResponse> getTransfers(
            @QueryParam("transferExternalId") @Parameter(description = "transferExternalId") final String transferExternalId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveTransferData(loanId, loanExternalId, transferExternalId, offset, limit)
                .map(externalAssetOwnerReadMapper::toTransferResponse);
    }

    @GET
    @Path("/transfers/active-transfer")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve Active Asset Owner Transfer", description = "Retrieve Active External Asset Owner Transfer by transferExternalId, loanId or loanExternalId")
    public ExternalTransferResponse getActiveTransfer(
            @QueryParam("transferExternalId") @Parameter(description = "transferExternalId") final String transferExternalId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnerReadMapper
                .toTransferResponse(externalAssetOwnersReadService.retrieveActiveTransferData(loanId, loanExternalId, transferExternalId));
    }

    @GET
    @Path("/transfers/{transferId}/journal-entries")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve Journal Entries of Transfer", description = "Retrieve Journal entries of transfer by transferId")
    public ExternalOwnerTransferJournalEntryResponse getJournalEntriesOfTransfer(
            @PathParam("transferId") @Parameter(description = "transferId") final Long transferId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnerReadMapper
                .toTransferJournalEntryResponse(externalAssetOwnersReadService.retrieveJournalEntriesOfTransfer(transferId, offset, limit));
    }

    @GET
    @Path("/owners/external-id/{ownerExternalId}/journal-entries")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve Journal Entries of Owner", description = "Retrieve Journal entries of owner by owner externalId")
    public ExternalOwnerJournalEntryResponse getJournalEntriesOfOwner(
            @PathParam("ownerExternalId") @Parameter(description = "ownerExternalId") final String ownerExternalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnerReadMapper
                .toOwnerJournalEntryResponse(externalAssetOwnersReadService.retrieveJournalEntriesOfOwner(ownerExternalId, offset, limit));
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
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnerCreateRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnerCreateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad requests due invalid json data")
    public ExternalAssetOwnerCreateResponse createExternalAssetOwner(@Valid final ExternalAssetOwnerCreateRequest request) {
        platformUserRightsContext.isAuthenticated();
        final var command = new Command<ExternalAssetOwnerCreateRequest>();
        command.setPayload(request);
        return (ExternalAssetOwnerCreateResponse) commandDispatcher.dispatch(command).get();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get all External Asset Owner with details")
    public List<ExternalTransferOwnerData> retrieveExternalAssetOwners() {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveAllExternalOwners();
    }

    // Private helper to route by command param
    private Command<?> buildTransferCommand(String commandParam, ExternalAssetOwnerSaleRequest request) {
        return switch (StringUtils.trimToEmpty(commandParam)) {
            case SALE_COMMAND_VALUE -> {
                final var cmd = new Command<ExternalAssetOwnerSaleRequest>();
                cmd.setPayload(request);
                yield cmd;
            }
            case INTERMEDIARY_SALE_COMMAND_VALUE -> {
                final var intermediaryRequest = ExternalAssetOwnerIntermediarySaleRequest.builder().loanId(request.getLoanId())
                        .ownerExternalId(request.getOwnerExternalId()).purchasePriceRatio(request.getPurchasePriceRatio())
                        .settlementDate(request.getSettlementDate()).transferExternalId(request.getTransferExternalId())
                        .transferExternalGroupId(request.getTransferExternalGroupId()).dateFormat(request.getDateFormat())
                        .locale(request.getLocale()).build();
                final var cmd = new Command<ExternalAssetOwnerIntermediarySaleRequest>();
                cmd.setPayload(intermediaryRequest);
                yield cmd;
            }
            case BUY_BACK_COMMAND_VALUE -> {
                final var buybackRequest = ExternalAssetOwnerBuybackRequest.builder().loanId(request.getLoanId())
                        .settlementDate(request.getSettlementDate()).transferExternalId(request.getTransferExternalId())
                        .dateFormat(request.getDateFormat()).locale(request.getLocale()).build();
                final var cmd = new Command<ExternalAssetOwnerBuybackRequest>();
                cmd.setPayload(buybackRequest);
                yield cmd;
            }
            default -> throw new UnrecognizedQueryParamException(COMMAND_PARAM, commandParam,
                    List.of(SALE_COMMAND_VALUE, INTERMEDIARY_SALE_COMMAND_VALUE, BUY_BACK_COMMAND_VALUE));
        };
    }

    private void validate(ExternalAssetOwnerSaleRequest request) {
        final var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
