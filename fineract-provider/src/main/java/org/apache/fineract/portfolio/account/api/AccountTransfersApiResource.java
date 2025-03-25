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
package org.apache.fineract.portfolio.account.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.request.AccountTransSearchParam;
import org.apache.fineract.portfolio.account.data.request.AccountTransferRequest;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/accounttransfers")
@Component
@Tag(name = "Account Transfers", description = "Ability to be able to transfer monetary funds from one account to another.\n\nNote: At present only savings account to savings account transfers are supported.")
@RequiredArgsConstructor
public class AccountTransfersApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<AccountTransferData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Account Transfer Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n\n"
            + "\n\n" + "Field Defaults\n\n" + "Allowed Value Lists\n\n" + "Example Requests:\n\n" + "\n\n"
            + "accounttransfers/template?fromAccountType=2&fromOfficeId=1\n\n" + "\n\n"
            + "accounttransfers/template?fromAccountType=2&fromOfficeId=1&fromClientId=1\n\n" + "\n\n"
            + "accounttransfers/template?fromClientId=1&fromAccountType=2&fromAccountId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransfersApiResourceSwagger.GetAccountTransfersTemplateResponse.class))) })
    public AccountTransferData template(@BeanParam AccountTransSearchParam accountTransSearchParam) {

        context.authenticatedUser().validateHasReadPermission(AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME);

        return accountTransfersReadPlatformService.retrieveTemplate(accountTransSearchParam.getFromAccountId(),
                accountTransSearchParam.getFromClientId(), accountTransSearchParam.getFromAccountId(),
                accountTransSearchParam.getFromAccountType(), accountTransSearchParam.getToOfficeId(),
                accountTransSearchParam.getToClientId(), accountTransSearchParam.getToAccountId(),
                accountTransSearchParam.getToAccountType());
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Transfer", description = "Ability to create new transfer of monetary funds from one account to another.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountTransferRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransfersApiResourceSwagger.PostAccountTransfersResponse.class)))
    public CommandProcessingResult create(@Parameter(hidden = true) AccountTransferRequest accountTransferRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountTransfer()
                .withJson(toApiJsonSerializer.serialize(accountTransferRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List account transfers", description = "Lists account's transfers\n\n" + "Example Requests:\n\n" + "\n\n"
            + "accounttransfers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransfersApiResourceSwagger.GetAccountTransfersResponse.class))) })
    public Page<AccountTransferData> retrieveAll(@QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(example = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("accountDetailId") @Parameter(description = "accountDetailId") final Long accountDetailId) {

        context.authenticatedUser().validateHasReadPermission(AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();

        return accountTransfersReadPlatformService.retrieveAll(searchParameters, accountDetailId);
    }

    @GET
    @Path("{transferId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve account transfer", description = "Retrieves account transfer\n\n" + "Example Requests :\n\n" + "\n\n"
            + "accounttransfers/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransfersApiResourceSwagger.GetAccountTransfersResponse.GetAccountTransfersPageItems.class))) })
    public AccountTransferData retrieveOne(@PathParam("transferId") @Parameter(description = "transferId") final Long transferId) {
        context.authenticatedUser().validateHasReadPermission(AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME);
        return accountTransfersReadPlatformService.retrieveOne(transferId);
    }

    @GET
    @Path("templateRefundByTransfer")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Refund of an Active Loan by Transfer Template", description = "Retrieves Refund of an Active Loan by Transfer Template"
            + "Example Requests :\n\n" + "\n\n"
            + "accounttransfers/templateRefundByTransfer?fromAccountId=2&fromAccountType=1& toAccountId=1&toAccountType=2&toClientId=1&toOfficeId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransfersApiResourceSwagger.GetAccountTransfersTemplateRefundByTransferResponse.class))) })
    public AccountTransferData templateRefundByTransfer(@BeanParam AccountTransSearchParam accountTransSearchParam) {
        context.authenticatedUser().validateHasReadPermission(AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME);
        return accountTransfersReadPlatformService.retrieveRefundByTransferTemplate(accountTransSearchParam.getFromAccountId(),
                accountTransSearchParam.getFromClientId(), accountTransSearchParam.getFromAccountId(),
                accountTransSearchParam.getFromAccountType(), accountTransSearchParam.getToOfficeId(),
                accountTransSearchParam.getToClientId(), accountTransSearchParam.getToAccountId(),
                accountTransSearchParam.getToAccountType());
    }

    @POST
    @Path("refundByTransfer")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Refund of an Active Loan by Transfer", description = "Ability to refund an active loan by transferring to a savings account.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountTransferRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountTransfersApiResourceSwagger.PostAccountTransfersRefundByTransferResponse.class))) })
    public CommandProcessingResult templateRefundByTransferPost(@Parameter(hidden = true) AccountTransferRequest accountTransferRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().refundByTransfer()
                .withJson(toApiJsonSerializer.serialize(accountTransferRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
