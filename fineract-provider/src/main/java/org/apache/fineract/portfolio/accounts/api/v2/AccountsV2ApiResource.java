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
package org.apache.fineract.portfolio.accounts.api.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Component;

@Path("/v2/accounts/{type}")
@Component
@Tag(name = "Share Account V2", description = "Version 2 share account endpoints with explicit operation-specific request schemas.")
@RequiredArgsConstructor
public class AccountsV2ApiResource {

    private final AccountsV2ApiDelegate delegate;

    @POST
    @Path("{accountId}/redeem")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Redeem shares on a share account", operationId = "redeemShares", description = "Redeem shares on a share account:\n\n"
            + "Results redeem some/all shares from share account.\n\n" + "requestedDate is requested date of shares redeem\n\n"
            + "requestedShares is number of shares to be redeemed\n\n"
            + "Mandatory Fields: dateFormat,locale,requestedDate,requestedShares\n\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdRedeemRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdResponse.class)))
    public CommandProcessingResult redeemShares(@PathParam("type") @Parameter(description = "type") final String accountType,
            @PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return delegate.redeemShares(accountType, accountId, apiRequestBodyAsJson);
    }

    @POST
    @Path("{accountId}/additional-shares")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Apply additional shares on a share account", operationId = "applyAdditionalShares", description = "Apply additional shares on a share account:\n\n"
            + "requestedDate is requested date of share purchase\n\n" + "requestedShares is number of shares to be purchased\n\n"
            + "Mandatory Fields: dateFormat,locale,requestedDate,requestedShares\n\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdAdditionalSharesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdResponse.class)))
    public CommandProcessingResult applyAdditionalShares(@PathParam("type") @Parameter(description = "type") final String accountType,
            @PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return delegate.applyAdditionalShares(accountType, accountId, apiRequestBodyAsJson);
    }

    @POST
    @Path("{accountId}/additional-shares/approve")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve additional shares request on a share account", operationId = "approveAdditionalShares", description = "Approve additional shares request on a share account\n\n"
            + "requestedShares contains Share purchase transaction IDs\n\n" + "Mandatory Fields: requestedShares\n\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdApproveAdditionalSharesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdResponse.class)))
    public CommandProcessingResult approveAdditionalShares(@PathParam("type") @Parameter(description = "type") final String accountType,
            @PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return delegate.approveAdditionalShares(accountType, accountId, apiRequestBodyAsJson);
    }

    @POST
    @Path("{accountId}/additional-shares/reject")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Reject additional shares request on a share account", operationId = "rejectAdditionalShares", description = "Reject additional shares request on a share account:\n\n"
            + "requestedShares contains Share purchase transaction IDs\n\n" + "Mandatory Fields: requestedShares\n\n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdRejectAdditionalSharesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsV2ApiResourceSwagger.PostAccountsTypeAccountIdResponse.class)))
    public CommandProcessingResult rejectAdditionalShares(@PathParam("type") @Parameter(description = "type") final String accountType,
            @PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return delegate.rejectAdditionalShares(accountType, accountId, apiRequestBodyAsJson);
    }
}
