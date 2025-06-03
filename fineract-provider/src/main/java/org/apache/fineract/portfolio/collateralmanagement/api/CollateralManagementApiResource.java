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
import jakarta.ws.rs.core.MediaType;
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
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.command.CollateralManagementProductCommand;
import org.apache.fineract.portfolio.collateralmanagement.command.CollateralManagementProductDeleteCommand;
import org.apache.fineract.portfolio.collateralmanagement.command.CollateralManagementProductUpdateCommand;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementData;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductCreateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductDeleteRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductUpdateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductUpdateResponse;
import org.apache.fineract.portfolio.collateralmanagement.service.CollateralManagementReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/collateral-management")
@Component
@Tag(name = "Collateral Management", description = "Collateral Management is for managing collateral operations")
@RequiredArgsConstructor
public class CollateralManagementApiResource {

    private final PlatformSecurityContext context;
    private final CollateralManagementReadPlatformService collateralManagementReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final CommandPipeline pipeline;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new collateral", description = "Collateral Creation")
    public CollateralManagementProductResponse createCollateral(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @Parameter(hidden = true) @Valid final CollateralManagementProductCreateRequest collateralManagementProductRequest) {

        CollateralManagementProductCommand command = new CollateralManagementProductCommand();
        initCommand(idempotencyKey, command);
        command.setPayload(collateralManagementProductRequest);

        Supplier<CollateralManagementProductResponse> result = pipeline.send(command);
        return result.get();
    }

    @GET
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Collateral", description = "Fetch Collateral")
    public CollateralManagementData getCollateral(
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {

        this.context.authenticatedUser()
                .validateHasReadPermission(CollateralManagementJsonInputParams.COLLATERAL_PRODUCT_READ_PERMISSION.getValue());

        return this.collateralManagementReadPlatformService.getCollateralProduct(collateralId);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get All Collaterals", description = "Fetch all Collateral Products")
    public List<CollateralManagementData> getAllCollaterals() {
        this.context.authenticatedUser()
                .validateHasReadPermission(CollateralManagementJsonInputParams.COLLATERAL_PRODUCT_READ_PERMISSION.getValue());
        return this.collateralManagementReadPlatformService.getAllCollateralProducts();
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Collateral Template", description = "Get Collateral Template")
    public List<CurrencyData> getCollateralTemplate() {
        return currencyReadPlatformService.retrieveAllPlatformCurrencies();
    }

    @PUT
    @Path("{collateralId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Collateral", description = "Update Collateral")
    public CollateralManagementProductUpdateResponse updateCollateral(
            @HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) @Valid final CollateralManagementProductUpdateRequest request) {

        CollateralManagementProductUpdateCommand command = new CollateralManagementProductUpdateCommand();
        initCommand(idempotencyKey, command);

        request.setCollateralId(collateralId);
        command.setPayload(request);

        Supplier<CollateralManagementProductUpdateResponse> result = pipeline.send(command);
        return result.get();
    }

    @DELETE
    @Path("{collateralId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Collateral", description = "Delete Collateral")
    public CollateralManagementProductResponse deleteCollateral(@HeaderParam("Idempotency-Key") @DefaultValue("") String idempotencyKey,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {

        CollateralManagementProductDeleteCommand command = new CollateralManagementProductDeleteCommand();
        initCommand(idempotencyKey, command);

        CollateralManagementProductDeleteRequest request = new CollateralManagementProductDeleteRequest();
        request.setResourceId(collateralId);
        command.setPayload(request);

        Supplier<CollateralManagementProductResponse> result = pipeline.send(command);
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
