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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.command.ClientCollateralManagementCreateCommand;
import org.apache.fineract.portfolio.collateralmanagement.command.ClientCollateralManagementDeleteCommand;
import org.apache.fineract.portfolio.collateralmanagement.command.ClientCollateralManagementUpdateCommand;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementCreateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementDeleteRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementDeleteResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementUpdateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralTemplateData;
import org.apache.fineract.portfolio.collateralmanagement.service.ClientCollateralManagementReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/clients/{clientId}/collaterals")
@Component
@Tag(name = "Client Collateral Management", description = "Client Collateral Management is for managing collateral operations")
@RequiredArgsConstructor
public class ClientCollateralManagementApiResource {

    private final PlatformSecurityContext context;
    private final ClientCollateralManagementReadPlatformService clientCollateralManagementReadPlatformService;
    private final CommandPipeline pipeline;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Clients Collateral Products", description = "Get Collateral Product of a Client")
    public List<ClientCollateralManagementData> getClientCollateral(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId, @Context final UriInfo uriInfo,
            @QueryParam("prodId") @Parameter(description = "prodId") final Long prodId) {

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
    public ClientCollateralManagementResponse addCollateral(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("clientId") final Long clientId,
            @Valid ClientCollateralManagementCreateRequest clientCollateralManagementCreateRequest) {

        ClientCollateralManagementCreateCommand command = new ClientCollateralManagementCreateCommand();
        initCommand(idempotencyKey, command);
        clientCollateralManagementCreateRequest.setClientId(clientId);
        command.setPayload(clientCollateralManagementCreateRequest);

        Supplier<ClientCollateralManagementResponse> result = pipeline.send(command);
        return result.get();
    }

    @PUT
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update New Collateral of a Client", description = "Update New Collateral of a Client")
    public ClientCollateralManagementResponse updateCollateral(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) @Valid ClientCollateralManagementUpdateRequest request) {

        ClientCollateralManagementUpdateCommand command = new ClientCollateralManagementUpdateCommand();
        initCommand(idempotencyKey, command);

        request.setClientId(clientId);
        request.setCollateralId(collateralId);
        command.setPayload(request);

        Supplier<ClientCollateralManagementResponse> result = pipeline.send(command);
        return result.get();
    }

    @DELETE
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete Client Collateral", description = "Delete Client Collateral")
    public ClientCollateralManagementDeleteResponse deleteCollateral(
            @HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey, @PathParam("clientId") final Long clientId,
            @PathParam("collateralId") final Long collateralId) {

        ClientCollateralManagementDeleteCommand command = new ClientCollateralManagementDeleteCommand();
        initCommand(idempotencyKey, command);

        ClientCollateralManagementDeleteRequest request = new ClientCollateralManagementDeleteRequest();
        request.setClientId(clientId);
        request.setCollateralId(collateralId);
        command.setPayload(request);

        Supplier<ClientCollateralManagementDeleteResponse> result = pipeline.send(command);
        return result.get();
    }

    private void initCommand(String idempotencyKey, Command command) {
        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        command.setId(UUID.randomUUID());
        command.setCreatedAt(OffsetDateTime.now(ZoneId.of("UTC")));
        command.setTenantId(tenantIdentifier);
        command.setIdempotencyKey(idempotencyKey);
    }
}
