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
package org.apache.fineract.portfolio.floatingrates.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.floatingrates.command.FloatingRateCreateCommand;
import org.apache.fineract.portfolio.floatingrates.command.FloatingRateUpdateCommand;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateResponse;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateResponse;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/floatingrates")
@Component
@Tag(name = "Floating Rates", description = "It lets you create, list, retrieve and upload the floating rates")
@RequiredArgsConstructor
public class FloatingRatesApiResource {

    private static final String RESOURCE_NAME = "FLOATINGRATE";
    private final PlatformSecurityContext context;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
    private final CommandDispatcher dispatcher;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Floating Rate", operationId = "createFloatingRate", description = "Creates a new Floating Rate\n"
            + "Mandatory Fields: name\n" + "Optional Fields: isBaseLendingRate, isActive, ratePeriods")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FloatingRateRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PostFloatingRatesResponse.class)))
    public FloatingRateCreateResponse createFloatingRate(@Parameter(hidden = true) final FloatingRateRequest floatingRateRequest) {
        final FloatingRateCreateCommand command = new FloatingRateCreateCommand();
        command.setPayload(floatingRateRequest);
        final Supplier<FloatingRateCreateResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Floating Rates", operationId = "retrieveAllFloatingRates", description = "Lists Floating Rates")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.GetFloatingRatesResponse.class))))
    public List<FloatingRateData> retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        return floatingRatesReadPlatformService.retrieveAll();
    }

    @GET
    @Path("{floatingRateId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Floating Rate", operationId = "retrieveOneFloatingRate", description = "Retrieves Floating Rate")
    public FloatingRateData retrieveOne(@PathParam("floatingRateId") @Parameter(description = "floatingRateId") final Long floatingRateId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        return floatingRatesReadPlatformService.retrieveOne(floatingRateId);
    }

    @PUT
    @Path("{floatingRateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Floating Rate", operationId = "updateFloatingRate", description = "Updates new Floating Rate. Rate Periods in the past cannot be modified. All the future rateperiods would be replaced with the new ratePeriods data sent.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FloatingRateRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdResponse.class)))
    public FloatingRateUpdateResponse updateFloatingRate(
            @PathParam("floatingRateId") @Parameter(description = "floatingRateId") final Long floatingRateId,
            @Parameter(hidden = true) final FloatingRateRequest floatingRateRequest) {
        final FloatingRateUpdateRequest updateRequest = FloatingRateUpdateRequest.builder().id(floatingRateId)
                .name(floatingRateRequest.getName()).isBaseLendingRate(floatingRateRequest.getIsBaseLendingRate())
                .isActive(floatingRateRequest.getIsActive()).ratePeriods(floatingRateRequest.getRatePeriods()).build();
        final FloatingRateUpdateCommand command = new FloatingRateUpdateCommand();
        command.setPayload(updateRequest);
        final Supplier<FloatingRateUpdateResponse> response = dispatcher.dispatch(command);
        return response.get();
    }

}
