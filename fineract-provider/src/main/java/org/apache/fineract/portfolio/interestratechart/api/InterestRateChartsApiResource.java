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

import io.swagger.annotations.*;
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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartApiConstants.*;

@Path("/interestratecharts")
@Component
@Scope("singleton")
@Api(value = "Interest Rate Chart", description = "This defines an interest rate scheme that can be associated to a term deposit product. This will have a slab (band or range) of deposit periods and the associated interest rates applicable along with incentives for each band.")
public class InterestRateChartsApiResource {

    private final InterestRateChartReadPlatformService chartReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private static final Set<String> INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(InterestRateChartApiConstants.localeParamName,
                    InterestRateChartApiConstants.dateFormatParamName, idParamName, nameParamName, descriptionParamName,
                    fromDateParamName, endDateParamName, chartSlabs, isPrimaryGroupingByAmountParamName));

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
    @ApiOperation(value = "Retrieve Chart Details Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for creating a chart. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "interestratecharts/template")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartsApiResourceSwagger.GetInterestRateChartsTemplateResponse.class)})
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        InterestRateChartData chartData = this.chartReadPlatformService.template();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartData, INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve all Charts", httpMethod = "GET", notes = "Retrieve list of charts associated with a term deposit product(FD or RD).\n" + "Example Requests:\n" + "\n" + "interestratecharts?productId=1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("productId") @ApiParam(value = "productId") final Long productId) {

        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        Collection<InterestRateChartData> chartDatas = this.chartReadPlatformService.retrieveAllWithSlabs(productId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartDatas, INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Chart", httpMethod = "GET", notes = "It retrieves the Interest Rate Chart\n" + "Example Requests:\n" + "\n" + "interestratecharts/1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartsApiResourceSwagger.GetInterestRateChartsResponse.class)})
    public String retrieveOne(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        InterestRateChartData chartData = null;
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty() && associationParameters.contains(InterestRateChartApiConstants.chartSlabs)) {
            chartData = this.chartReadPlatformService.retrieveOneWithSlabs(chartId);
        }else {
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
    @ApiOperation(value = "Create a Chart", httpMethod = "POST", notes = "Creates a new chart which can be attached to a term deposit products (FD or RD).")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = InterestRateChartsApiResourceSwagger.PostInterestRateChartsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartsApiResourceSwagger.PostInterestRateChartsResponse.class)})
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChart().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Chart", notes = "It updates the Chart.", httpMethod = "PUT")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = InterestRateChartsApiResourceSwagger.PutInterestRateChartsChartIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartsApiResourceSwagger.PutInterestRateChartsChartIdResponse.class)})
    public String update(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChart(chartId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Chart", notes = "It deletes the chart", httpMethod = "DELETE")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartsApiResourceSwagger.DeleteInterestRateChartsChartIdResponse.class)})
    public String delete(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChart(chartId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}