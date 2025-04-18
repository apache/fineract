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
package org.apache.fineract.portfolio.interestratechart.api;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartStabRequest;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/interestratecharts/{chartId}/chartslabs")
@Component
@Tag(name = "Interest Rate Slab (A.K.A interest bands)", description = "The slabs a.k.a interest bands are associated with Interest Rate Chart. These bands allow to define different interest rates for different deposit term periods.")
@RequiredArgsConstructor
public class InterestRateChartSlabsApiResource {

    private final InterestRateChartSlabReadPlatformService interestRateChartSlabReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartSlabData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public InterestRateChartSlabData template(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId) {
        return this.interestRateChartSlabReadPlatformService.retrieveTemplate();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all Slabs", description = "Retrieve list of slabs associated with a chart\n" + "\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    public List<InterestRateChartSlabData> retrieveAll(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId) {

        this.context.authenticatedUser().validateHasReadPermission(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);
        return this.interestRateChartSlabReadPlatformService.retrieveAll(chartId);
    }

    @GET
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Slab", description = "Retrieve a slab associated with an Interest rate chart\n" + "\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs/1\n")
    public InterestRateChartSlabData retrieveOne(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @PathParam("chartSlabId") @Parameter(description = "chartSlabId") final Long chartSlabId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);

        InterestRateChartSlabData chartSlab = this.interestRateChartSlabReadPlatformService.retrieveOne(chartId, chartSlabId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            chartSlab = this.interestRateChartSlabReadPlatformService.retrieveWithTemplate(chartSlab);
        }

        return chartSlab;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Slab", description = "Creates a new interest rate slab for an interest rate chart.\n"
            + "Mandatory Fields\n" + "periodType, fromPeriod, annualInterestRate\n" + "Optional Fields\n" + "toPeriod and description\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InterestRateChartStabRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.PostInterestRateChartsChartIdChartSlabsResponse.class))) })
    public CommandProcessingResult create(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @Parameter(hidden = true) InterestRateChartStabRequest interestRateChartStabRequest) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChartSlab(chartId)
                .withJson(toApiJsonSerializer.serialize(interestRateChartStabRequest)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Slab", description = "It updates the Slab from chart")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InterestRateChartStabRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse.class))) })
    public CommandProcessingResult update(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @PathParam("chartSlabId") @Parameter(description = "chartSlabId") final Long chartSlabId,
            @Parameter(hidden = true) InterestRateChartStabRequest request) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChartSlab(chartId, chartSlabId)
                .withJson(toApiJsonSerializer.serialize(request)).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Slab", description = "Delete a Slab from a chart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.DeleteInterestRateChartsChartIdChartSlabsResponse.class))) })
    public CommandProcessingResult delete(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @PathParam("chartSlabId") @Parameter(description = "chartSlabId") final Long chartSlabId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChartSlab(chartId, chartSlabId).build();
        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
