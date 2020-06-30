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

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.chartSlabs;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.endDateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.fromDateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.idParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.isPrimaryGroupingByAmountParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.nameParamName;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/interestratecharts")
@Component
@Scope("singleton")
@Tag(name = "Interest Rate Chart", description = "This defines an interest rate scheme that can be associated to a term deposit product. This will have a slab (band or range) of deposit periods and the associated interest rates applicable along with incentives for each band.")
public class InterestRateChartsApiResource {

    private final InterestRateChartReadPlatformService chartReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private static final Set<String> INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            InterestRateChartApiConstants.localeParamName, InterestRateChartApiConstants.dateFormatParamName, idParamName, nameParamName,
            descriptionParamName, fromDateParamName, endDateParamName, chartSlabs, isPrimaryGroupingByAmountParamName));

    @Autowired
    public InterestRateChartsApiResource(final InterestRateChartReadPlatformService chartReadPlatformService,
            final PlatformSecurityContext context, final DefaultToApiJsonSerializer<InterestRateChartData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {

        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.chartReadPlatformService = chartReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Chart Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for creating a chart. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "interestratecharts/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.GetInterestRateChartsTemplateResponse.class))) })
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        InterestRateChartData chartData = this.chartReadPlatformService.template();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartData, INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve all Charts", description = "Retrieve list of charts associated with a term deposit product(FD or RD).\n"
            + "Example Requests:\n" + "\n" + "interestratecharts?productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId) {

        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        Collection<InterestRateChartData> chartDatas = this.chartReadPlatformService.retrieveAllWithSlabs(productId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartDatas, INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Chart", description = "It retrieves the Interest Rate Chart\n" + "Example Requests:\n" + "\n"
            + "interestratecharts/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.class))) })
    public String retrieveOne(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        InterestRateChartData chartData = null;
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty() && associationParameters.contains(InterestRateChartApiConstants.chartSlabs)) {
            chartData = this.chartReadPlatformService.retrieveOneWithSlabs(chartId);
        } else {
            chartData = this.chartReadPlatformService.retrieveOne(chartId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            chartData = this.chartReadPlatformService.retrieveWithTemplate(chartData);
        }

        return this.toApiJsonSerializer.serialize(settings, chartData, INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Chart", description = "Creates a new chart which can be attached to a term deposit products (FD or RD).")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.PostInterestRateChartsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.PostInterestRateChartsResponse.class))) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChart().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Chart", description = "It updates the chart")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.PutInterestRateChartsChartIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.PutInterestRateChartsChartIdResponse.class))) })
    public String update(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChart(chartId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Chart", description = "It deletes the chart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InterestRateChartsApiResourceSwagger.DeleteInterestRateChartsChartIdResponse.class))) })
    public String delete(@PathParam("chartId") @Parameter(description = "chartId") final Long chartId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChart(chartId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
