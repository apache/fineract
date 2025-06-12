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
package org.apache.fineract.investor.external_assets_owner.api;

import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.BUY_BACK_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.CANCEL_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.INTERMEDIARY_SALE_COMMAND_VALUE;
import static org.apache.fineract.infrastructure.core.service.CommandParameterUtil.SALE_COMMAND_VALUE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.command.CommandHandlerRegistry;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.investor.api.search.ExternalAssetOwnersSearchApiDelegate;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.data.ExternalOwnerJournalEntryData;
import org.apache.fineract.investor.data.ExternalOwnerTransferJournalEntryData;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.external_assets_owner.command.TransferAssetRequestCommand;
import org.apache.fineract.investor.external_assets_owner.command.TransferExternalAssetRequestCommand;
import org.apache.fineract.investor.external_assets_owner.command.TransferLoanExternalAssetRequestCommand;
import org.apache.fineract.investor.external_assets_owner.data.BuyBackLoanExternalAssetRequest;
import org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest;
import org.apache.fineract.investor.external_assets_owner.data.ExternalAssetOwnerResponse;
import org.apache.fineract.investor.external_assets_owner.data.SaleLoanExternalAssetRequest;
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

    private final ExternalAssetOwnersReadService externalAssetOwnersReadService;
    private final LoanReadPlatformServiceCommon loanReadPlatformService;
    private final ExternalAssetOwnersSearchApiDelegate delegate;
    private final CommandPipeline commandPipeline;

    private final CommandHandlerRegistry<String, Long, Command<?>, Supplier<ExternalAssetOwnerResponse>> COMMAND_HANDLER_REGISTRY = new CommandHandlerRegistry<>(
            new HashMap<>());

    @PostConstruct
    public void init() {
        COMMAND_HANDLER_REGISTRY.register(SALE_COMMAND_VALUE, (id, command) -> commandPipeline.send(command));
        COMMAND_HANDLER_REGISTRY.register(INTERMEDIARY_SALE_COMMAND_VALUE, (id, command) -> commandPipeline.send(command));
        COMMAND_HANDLER_REGISTRY.register(BUY_BACK_COMMAND_VALUE, (id, command) -> commandPipeline.send(command));
        COMMAND_HANDLER_REGISTRY.register(CANCEL_COMMAND_VALUE, (id, command) -> commandPipeline.send(command));
    }

    @POST
    @Path("/transfers/loans/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ExternalAssetOwnerResponse transferRequestWithLoanId(@PathParam("loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Valid SaleLoanExternalAssetRequest assetOwnerReq) {

        final TransferExternalAssetRequestCommand command = new TransferExternalAssetRequestCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());

        assetOwnerReq.setLoanId(loanId);

        command.setPayload(assetOwnerReq);

        Supplier<ExternalAssetOwnerResponse> result = COMMAND_HANDLER_REGISTRY.execute(commandParam, loanId, command,
                new UnrecognizedQueryParamException("command", commandParam));

        return result.get();
    }

    @POST
    @Path("/transfers/loans/external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ExternalAssetOwnerResponse transferRequestWithLoanExternalId(@PathParam("loanExternalId") final String externalLoanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Valid BuyBackLoanExternalAssetRequest assetOwnerReq) {

        final Long loanId = loanReadPlatformService.getLoanIdByLoanExternalId(externalLoanId);

        final TransferLoanExternalAssetRequestCommand command = new TransferLoanExternalAssetRequestCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());

        assetOwnerReq.setLoanId(loanId);
        command.setPayload(assetOwnerReq);

        Supplier<ExternalAssetOwnerResponse> result = COMMAND_HANDLER_REGISTRY.execute(commandParam, loanId, command,
                new UnrecognizedQueryParamException("command", commandParam));

        return result.get();
    }

    @POST
    @Path("/transfers/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ExternalAssetOwnerResponse transferRequestWithId(@PathParam("id") final Long id,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Valid CancelTransactionExternalAssetRequest cancelTransactionExternalAssetRequest) {

        final TransferAssetRequestCommand command = new TransferAssetRequestCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());

        cancelTransactionExternalAssetRequest.setTransferId(id);
        command.setPayload(cancelTransactionExternalAssetRequest);

        Supplier<ExternalAssetOwnerResponse> result = COMMAND_HANDLER_REGISTRY.execute(commandParam, id, command,
                new UnrecognizedQueryParamException("command", commandParam));

        return result.get();
    }

    @POST
    @Path("/transfers/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public ExternalAssetOwnerResponse transferRequestWithId(@PathParam("externalId") final String externalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Valid SaleLoanExternalAssetRequest assetOwnerReq) {

        final Long id = externalAssetOwnersReadService.retrieveLastTransferIdByExternalId(new ExternalId(externalId));

        final TransferExternalAssetRequestCommand command = new TransferExternalAssetRequestCommand();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());

        assetOwnerReq.setLoanId(id);
        command.setPayload(assetOwnerReq);

        Supplier<ExternalAssetOwnerResponse> result = COMMAND_HANDLER_REGISTRY.execute(commandParam, id, command,
                new UnrecognizedQueryParamException("command", commandParam));

        return result.get();
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

        return externalAssetOwnersReadService.retrieveJournalEntriesOfOwner(ownerExternalId, offset, limit);
    }

    @POST
    @Path("/search")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Search External Asset Owner Transfers by text or date ranges to settlement or effective dates")
    public Page<ExternalTransferData> searchInvestorData(@Parameter PagedRequest<ExternalAssetOwnerSearchRequest> request) {

        return delegate.searchInvestorData(request);
    }
}
