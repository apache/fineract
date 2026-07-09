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
package org.apache.fineract.portfolio.collateralmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.portfolio.collateralmanagement.command.ClientCollateralCreateCommand;
import org.apache.fineract.portfolio.collateralmanagement.command.ClientCollateralDeleteCommand;
import org.apache.fineract.portfolio.collateralmanagement.command.ClientCollateralUpdateCommand;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralCreateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralCreateResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralDeleteRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralDeleteResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralUpdateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralUpdateResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralTemplateData;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementReadService;
import org.springframework.stereotype.Component;

@Path("/v1/clients/{clientId}/collaterals")
@Component
@Tag(name = "Client Collateral Management", description = "Client Collateral Management is for managing collateral operations")
@RequiredArgsConstructor
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class ClientCollateralManagementApiResource {

    private final CommandDispatcher dispatcher;
    private final ClientCollateralManagementReadService clientCollateralManagementReadService;

    @GET
    @Operation(summary = "Get Clients Collateral Products", operationId = "getClientCollateralProducts", description = "Get Collateral Product of a Client")
    @AlternativeOperationId("getClientCollateral")
    public List<ClientCollateralManagementData> getClientCollateral(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("prodId") @Parameter(description = "prodId") final Long prodId) {
        return clientCollateralManagementReadService.getClientCollaterals(clientId, prodId);
    }

    @GET
    @Path("{clientCollateralId}")
    @Operation(summary = "Get Client Collateral Data", operationId = "getClientCollateralData", description = "Get Client Collateral Data")
    public ClientCollateralManagementData getClientCollateralData(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("clientCollateralId") @Parameter(description = "clientCollateralId") final Long collateralId) {
        return clientCollateralManagementReadService.getClientCollateralManagementData(collateralId);
    }

    @GET
    @Path("template")
    @Operation(summary = "Get Client Collateral Template", operationId = "getClientCollateralTemplate", description = "Get Client Collateral Template")
    public List<LoanCollateralTemplateData> getClientCollateralTemplate(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        return clientCollateralManagementReadService.getLoanCollateralTemplate(clientId);
    }

    @POST
    @Operation(summary = "Add New Collateral For a Client", operationId = "addClientCollateral", description = "Add New Collateral For a Client")
    @AlternativeOperationId("addCollateral")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCollateralCreateResponse.class)))
    public ClientCollateralCreateResponse addCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            ClientCollateralCreateRequest request) {
        request.setClientId(clientId);
        final var command = new ClientCollateralCreateCommand();
        command.setPayload(request);
        return dispatcher.<ClientCollateralCreateRequest, ClientCollateralCreateResponse>dispatch(command).get();
    }

    @PUT
    @Path("{collateralId}")
    @Operation(summary = "Update New Collateral of a Client", operationId = "updateClientCollateral", description = "Update New Collateral of a Client")
    @AlternativeOperationId("updateCollateral_1")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCollateralUpdateResponse.class)))
    public ClientCollateralUpdateResponse updateCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            ClientCollateralUpdateRequest request) {
        request.setClientId(clientId);
        request.setCollateralId(collateralId);
        final var command = new ClientCollateralUpdateCommand();
        command.setPayload(request);
        return dispatcher.<ClientCollateralUpdateRequest, ClientCollateralUpdateResponse>dispatch(command).get();
    }

    @DELETE
    @Path("{collateralId}")
    @Operation(summary = "Delete Client Collateral", operationId = "deleteClientCollateral", description = "Delete Client Collateral")
    @AlternativeOperationId("deleteCollateral_1")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCollateralDeleteResponse.class)))
    public ClientCollateralDeleteResponse deleteCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        final var request = ClientCollateralDeleteRequest.builder().clientId(clientId).collateralId(collateralId).build();
        final var command = new ClientCollateralDeleteCommand();
        command.setPayload(request);
        return dispatcher.<ClientCollateralDeleteRequest, ClientCollateralDeleteResponse>dispatch(command).get();
    }
}
