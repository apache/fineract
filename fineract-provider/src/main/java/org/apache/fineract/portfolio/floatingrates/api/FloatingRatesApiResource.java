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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/floatingrates")
@Component
@Scope("singleton")
@Tag(name = "Floating Rates", description = "It lets you create, list, retrieve and upload the floating rates")
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
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public FloatingRatesApiResource(final PlatformSecurityContext context,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<FloatingRateData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final FloatingRatesReadPlatformService floatingRatesReadPlatformService) {
        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new Floating Rate", description = "Creates a new Floating Rate\n" + "Mandatory Fields: name\n"
            + "Optional Fields: isBaseLendingRate, isActive, ratePeriods")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PostFloatingRatesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PostFloatingRatesResponse.class))) })
    public String createFloatingRate(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createFloatingRate().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Floating Rates", description = "Lists Floating Rates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.GetFloatingRatesResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        final List<FloatingRateData> floatingRates = this.floatingRatesReadPlatformService.retrieveAll();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, floatingRates, FloatingRatesApiResource.LIST_FLOATING_RATES_PARAMETERS);
    }

    @GET
    @Path("{floatingRateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Floating Rate", description = "Retrieves Floating Rate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.GetFloatingRatesFloatingRateIdResponse.class))) })
    public String retrieveOne(@PathParam("floatingRateId") @Parameter(description = "floatingRateId") final Long floatingRateId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME);
        final FloatingRateData floatingRates = this.floatingRatesReadPlatformService.retrieveOne(floatingRateId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, floatingRates, FloatingRatesApiResource.INDIVIDUAL_FLOATING_RATES_PARAMETERS);
    }

    @PUT
    @Path("{floatingRateId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Floating Rate", description = "Updates new Floating Rate. Rate Periods in the past cannot be modified. All the future rateperiods would be replaced with the new ratePeriods data sent.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdResponse.class))) })
    public String updateFloatingRate(@PathParam("floatingRateId") @Parameter(description = "floatingRateId") final Long floatingRateId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFloatingRate(floatingRateId).withJson(apiRequestBodyAsJson)
                .build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

}
