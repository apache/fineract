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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralTemplateData;
import org.apache.fineract.portfolio.collateralmanagement.data.UpdateClientCollateralRequest;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/clients/{clientId}/collaterals")
@Component
@Tag(name = "Client Collateral Management", description = "Client Collateral Management is for managing collateral operations")
@RequiredArgsConstructor
public class ClientCollateralManagementApiResource {

    private final DefaultToApiJsonSerializer<ClientCollateralManagementData> apiJsonSerializerDataService;
    private final DefaultToApiJsonSerializer<LoanCollateralTemplateData> apiJsonSerializerForLoanCollateralTemplateService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;
    private final ClientCollateralManagementReadPlatformService clientCollateralManagementReadPlatformService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Clients Collateral Products", description = "Get Collateral Product of a Client")
    public List<ClientCollateralManagementData> getClientCollateral(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId, @Context final UriInfo uriInfo,
            @QueryParam("prodId") @Parameter(description = "prodId") final Long prodId) {

        this.context.authenticatedUser()
                .validateHasReadPermission(CollateralManagementJsonInputParams.CLIENT_COLLATERAL_PRODUCT_READ_PERMISSION.getValue());

        return this.clientCollateralManagementReadPlatformService.getClientCollaterals(clientId, prodId);
    }

    @GET
    @Path("{clientCollateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Client Collateral Data", description = "Get Client Collateral Data")
    public ClientCollateralManagementData getClientCollateralData(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("clientCollateralId") @Parameter(description = "clientCollateralId") final Long collateralId) {

        this.context.authenticatedUser()
                .validateHasReadPermission(CollateralManagementJsonInputParams.CLIENT_COLLATERAL_PRODUCT_READ_PERMISSION.getValue());

        return this.clientCollateralManagementReadPlatformService.getClientCollateralManagementData(collateralId);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Client Collateral Template", description = "Get Client Collateral Template")
    public List<LoanCollateralTemplateData> getClientCollateralTemplate(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        return this.clientCollateralManagementReadPlatformService.getLoanCollateralTemplate(clientId);
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Add New Collateral For a Client", description = "Add New Collateral For a Client")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientCollateralRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCollateralManagementApiResourceSwagger.PostClientCollateralResponse.class))) })
    public CommandProcessingResult addCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) ClientCollateralRequest clientCollateralRequest) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().addClientCollateralProduct(clientId)
                .withJson(apiJsonSerializerDataService.serialize(clientCollateralRequest)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @PUT
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update New Collateral of a Client", description = "Update New Collateral of a Client")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UpdateClientCollateralRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCollateralManagementApiResourceSwagger.PutClientCollateralResponse.class))) })
    public CommandProcessingResult updateCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) UpdateClientCollateralRequest updateClientCollateralRequest) {

        final CommandWrapper commandWrapper = new CommandWrapperBuilder().updateClientCollateralProduct(clientId, collateralId)
                .withJson(apiJsonSerializerDataService.serialize(updateClientCollateralRequest)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

    @DELETE
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Client Collateral", description = "Delete Client Collateral")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientCollateralManagementApiResourceSwagger.DeleteClientCollateralResponse.class))) })
    public CommandProcessingResult deleteCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        final CommandWrapper commandWrapper = new CommandWrapperBuilder().deleteClientCollateralProduct(collateralId, clientId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
    }

}
