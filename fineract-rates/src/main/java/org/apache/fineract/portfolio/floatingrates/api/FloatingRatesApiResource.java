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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateIndividualResponse;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodResponse;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateResponse;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/floatingrates")
@Component
@Tag(name = "Floating Rates", description = "It lets you create, list, retrieve and upload the floating rates")
@RequiredArgsConstructor
public class FloatingRatesApiResource {

    private static final String RESOURCE_NAME = "FLOATINGRATE";
    private static final Set<String> LIST_FLOATING_RATES_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "name", "isBaseLendingRate", "isActive", "createdby", "createdOn", "modifiedBy", "modifiedOn"));
    private static final Set<String> INDIVIDUAL_FLOATING_RATES_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "isBaseLendingRate",
            "isActive", "createdBy", "createdOn", "modifiedBy", "modifiedOn", "ratePeriods"));
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<FloatingRateData> toApiJsonSerializer;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Floating Rate", description = "Creates a new Floating Rate\n" + "Mandatory Fields: name\n"
            + "Optional Fields: isBaseLendingRate, isActive, ratePeriods")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PostFloatingRatesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PostFloatingRatesResponse.class))) })
    public CommandProcessingResult createFloatingRate(@Parameter(hidden = true) final @Valid FloatingRateRequest floatingRateRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createFloatingRate()
                .withJson(toApiJsonSerializer.serialize(floatingRateRequest)).build();
        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Floating Rates", description = "Lists Floating Rates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.GetFloatingRatesResponse.class)))) })
    public List<FloatingRateResponse> retrieveAll() {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        final List<FloatingRateData> floatingRates = this.floatingRatesReadPlatformService.retrieveAll();
        return floatingRates.stream().map(data -> new FloatingRateResponse(data.getId(), data.getName(), data.isBaseLendingRate(),
                data.isActive(), data.getCreatedBy(), data.getCreatedOn(), data.getModifiedBy(), data.getModifiedOn())).toList();
    }

    @GET
    @Path("{floatingRateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Floating Rate", description = "Retrieves Floating Rate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.GetFloatingRatesFloatingRateIdResponse.class))) })
    public FloatingRateIndividualResponse retrieveOne(
            @PathParam("floatingRateId") @Parameter(description = "floatingRateId") final Long floatingRateId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        final FloatingRateData rate = this.floatingRatesReadPlatformService.retrieveOne(floatingRateId);
        return new FloatingRateIndividualResponse(rate.getId(), rate.getName(), rate.isBaseLendingRate(), rate.isActive(),
                rate.getCreatedBy(), rate.getCreatedOn(), rate.getModifiedBy(), rate.getModifiedOn(),
                rate.getRatePeriods().stream()
                        .map(period -> new FloatingRatePeriodResponse(period.getId(), period.getFromDate(), period.getInterestRate(),
                                period.isDifferentialToBaseLendingRate(), period.isActive(), period.getCreatedBy(), period.getCreatedOn(),
                                period.getModifiedBy(), period.getModifiedOn()))
                        .toList());
    }

    @PUT
    @Path("{floatingRateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Floating Rate", description = "Updates new Floating Rate. Rate Periods in the past cannot be modified. All the future rateperiods would be replaced with the new ratePeriods data sent.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdResponse.class))) })
    public CommandProcessingResult updateFloatingRate(
            @PathParam("floatingRateId") @Parameter(description = "floatingRateId") final Long floatingRateId,
            @Parameter(hidden = true) final @Valid FloatingRateRequest floatingRateRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFloatingRate(floatingRateId)
                .withJson(toApiJsonSerializer.serialize(floatingRateRequest)).build();

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
