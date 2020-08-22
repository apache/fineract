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
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.currencyCodeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.incentivesParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartSlabReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/interestratecharts/{chartId}/chartslabs")
@Component
@Scope("singleton")
@Tag(name = "Interest Rate Slab (A.K.A interest bands)", description = "The slabs a.k.a interest bands are associated with Interest Rate Chart. These bands allow to define different interest rates for different deposit term periods.")
public class InterestRateChartSlabsApiResource {

    private final InterestRateChartSlabReadPlatformService interestRateChartSlabReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartSlabData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private static final Set<String> INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(InterestRateChartSlabApiConstants.localeParamName, InterestRateChartSlabApiConstants.idParamName,
                    descriptionParamName, periodTypeParamName, fromPeriodParamName, toPeriodParamName, amountRangeFromParamName,
                    amountRangeToParamName, annualInterestRateParamName, currencyCodeParamName, incentivesParamName));

    @Autowired
    public InterestRateChartSlabsApiResource(final InterestRateChartSlabReadPlatformService interestRateChartSlabReadPlatformService,
            final PlatformSecurityContext context, final DefaultToApiJsonSerializer<InterestRateChartSlabData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.interestRateChartSlabReadPlatformService = interestRateChartSlabReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@Context final UriInfo uriInfo, @PathParam("chartId") @Parameter(description = "chartId") final Long chartId) {
        InterestRateChartSlabData chartSlab = this.interestRateChartSlabReadPlatformService.retrieveTemplate();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartSlab, INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all Slabs", description = "Retrieve list of slabs associated with a chart\n" + "\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.GetInterestRateChartsChartIdChartSlabsResponse.class)))) })
    public String retrieveAll(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);
        Collection<InterestRateChartSlabData> chartSlabs = this.interestRateChartSlabReadPlatformService.retrieveAll(chartId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, chartSlabs, INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Slab", description = "Retrieve a slab associated with an Interest rate chart\n" + "\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs/1\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.GetInterestRateChartsChartIdChartSlabsResponse.class))) })
    public String retrieveOne(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @PathParam("chartSlabId") @Parameter(description = "chartSlabId") final Long chartSlabId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);

        InterestRateChartSlabData chartSlab = this.interestRateChartSlabReadPlatformService.retrieveOne(chartId, chartSlabId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            chartSlab = this.interestRateChartSlabReadPlatformService.retrieveWithTemplate(chartSlab);
        }

        return this.toApiJsonSerializer.serialize(settings, chartSlab, INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Slab", description = "Creates a new interest rate slab for an interest rate chart.\n"
            + "Mandatory Fields\n" + "periodType, fromPeriod, annualInterestRate\n" + "Optional Fields\n" + "toPeriod and description\n"
            + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.PostInterestRateChartsChartIdChartSlabsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.PostInterestRateChartsChartIdChartSlabsResponse.class))) })
    public String create(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChartSlab(chartId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Slab", description = "It updates the Slab from chart")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse.class))) })
    public String update(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @PathParam("chartSlabId") @Parameter(description = "chartSlabId") final Long chartSlabId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChartSlab(chartId, chartSlabId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Slab", description = "Delete a Slab from a chart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartSlabsApiResourceSwagger.DeleteInterestRateChartsChartIdChartSlabsResponse.class))) })
    public String delete(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @PathParam("chartSlabId") @Parameter(description = "chartSlabId") final Long chartSlabId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChartSlab(chartId, chartSlabId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
