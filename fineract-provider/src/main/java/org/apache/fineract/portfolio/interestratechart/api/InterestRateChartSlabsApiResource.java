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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.*;

@Path("/interestratecharts/{chartId}/chartslabs")
@Component
@Scope("singleton")
@Api(value = "Interest Rate Slab (A.K.A interest bands)", description = "The slabs a.k.a interest bands are associated with Interest Rate Chart. These bands allow to define different interest rates for different deposit term periods.")
public class InterestRateChartSlabsApiResource {

    private final InterestRateChartSlabReadPlatformService interestRateChartSlabReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartSlabData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private static final Set<String> INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            InterestRateChartSlabApiConstants.localeParamName, InterestRateChartSlabApiConstants.idParamName,
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
    public String template(@Context final UriInfo uriInfo) {
        InterestRateChartSlabData chartSlab = this.interestRateChartSlabReadPlatformService.retrieveTemplate();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartSlab, INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve all Slabs", httpMethod = "GET", notes = "Retrieve list of slabs associated with a chart\n" + "\n" + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartSlabsApiResourceSwagger.GetInterestRateChartsChartIdChartSlabsResponse.class, responseContainer = "List")})
    public String retrieveAll(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);
        Collection<InterestRateChartSlabData> chartSlabs = this.interestRateChartSlabReadPlatformService.retrieveAll(chartId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, chartSlabs, INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Slab", httpMethod = "GET", notes = "Retrieve a slab associated with an Interest rate chart\n" + "\n" + "Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs/1\n")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartSlabsApiResourceSwagger.GetInterestRateChartsChartIdChartSlabsResponse.class)})
    public String retrieveOne(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @PathParam("chartSlabId") @ApiParam(value = "chartSlabId") final Long chartSlabId,
            @Context final UriInfo uriInfo) {

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
    @ApiOperation(value = "Create a Slab", httpMethod = "POST", notes = "Creates a new interest rate slab for an interest rate chart.\n" + "Mandatory Fields\n" + "periodType, fromPeriod, annualInterestRate\n" + "Optional Fields\n" + "toPeriod and description\n" +"Example Requests:\n" + "\n" + "interestratecharts/1/chartslabs")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = InterestRateChartSlabsApiResourceSwagger.PostInterestRateChartsChartIdChartSlabsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartSlabsApiResourceSwagger.PostInterestRateChartsChartIdChartSlabsResponse.class)})
    public String create(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChartSlab(chartId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Slab", httpMethod = "PUT", notes = "It updates the Slab from chart")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = InterestRateChartSlabsApiResourceSwagger.PutInterestRateChartsChartIdChartSlabsChartSlabIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartSlabsApiResourceSwagger.PutInterestRateChartsChartIdChartSlabsChartSlabIdResponse.class)})
    public String update(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @PathParam("chartSlabId") @ApiParam(value = "chartSlabId") final Long chartSlabId,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChartSlab(chartId, chartSlabId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Slab", httpMethod = "DELETE", notes = "Delete a Slab from a chart")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InterestRateChartSlabsApiResourceSwagger.DeleteInterestRateChartsChartIdChartSlabsResponse.class)})
    public String delete(@PathParam("chartId") @ApiParam(value = "chartId") final Long chartId, @PathParam("chartSlabId") @ApiParam(value = "chartSlabId") final Long chartSlabId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChartSlab(chartId, chartSlabId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}