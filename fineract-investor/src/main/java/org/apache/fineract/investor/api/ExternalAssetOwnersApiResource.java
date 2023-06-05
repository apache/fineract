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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.security.service.PlatformUserRightsContext;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferResponseData;
import org.apache.fineract.investor.service.ExternalAssetOwnersReadService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceCommon;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Path("/v1/external-asset-owners")
@Component
@Tag(name = "External Asset Owners", description = "External Asset Owners")
@RequiredArgsConstructor
public class ExternalAssetOwnersApiResource {

    private final PlatformUserRightsContext platformUserRightsContext;
    private final ExternalAssetOwnersReadService externalAssetOwnersReadService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<ExternalTransferData> apiJsonSerializerService;
    private final DefaultToApiJsonSerializer<ExternalTransferResponseData> postApiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanReadPlatformServiceCommon loanReadPlatformService;

    @POST
    @Path("/transfers/loans/{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferResponse.class))),
            @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated") })
    public String transferRequestWithLoanId(@PathParam("loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        return getResult(loanId, apiRequestBodyAsJson, commandParam);
    }

    @POST
    @Path("/transfers/loans/external-id/{loanExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ExternalAssetOwnersApiResourceSwagger.PostInitiateTransferResponse.class))),
            @ApiResponse(responseCode = "403", description = "Transfer cannot be initiated") })
    public String transferRequestWithLoanExternalId(@PathParam("loanExternalId") final String externalLoanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        platformUserRightsContext.isAuthenticated();
        Long loanId = loanReadPlatformService.getLoanIdByLoanExternalId(externalLoanId);

        return getResult(loanId, apiRequestBodyAsJson, commandParam);
    }

    @GET
    @Path("/transfers")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = {
            "External Asset Owners" }, summary = "Retrieve External Asset Owner Transfers", description = "Retrieve External Asset Owner Transfer items by transferExternalId, loanId or loanExternalId")
    public Page<ExternalTransferData> getTransfer(
            @QueryParam("transferExternalId") @Parameter(description = "transferExternalId") final String transferExternalId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit, @Context final UriInfo uriInfo) {
        platformUserRightsContext.isAuthenticated();
        return externalAssetOwnersReadService.retrieveTransferData(loanId, loanExternalId, transferExternalId, offset, limit);

    }

    private String getResult(Long loanId, String apiRequestBodyAsJson, String commandParam) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandWrapper commandRequest = null;
        if (CommandParameterUtil.is(commandParam, "sale")) {
            commandRequest = builder.saleLoanToExternalAssetOwner(loanId).build();
        } else if (CommandParameterUtil.is(commandParam, "buyback")) {
            commandRequest = builder.buybackLoanToExternalAssetOwner(loanId).build();
        }

        if (commandRequest == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return postApiJsonSerializerService.serialize(result);
    }
}
