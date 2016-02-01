/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.api;

import java.util.Collection;
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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartData;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/interestratecharts")
@Component
@Scope("singleton")
public class InterestRateChartsApiResource {

    private final InterestRateChartReadPlatformService chartReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

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
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        InterestRateChartData chartData = this.chartReadPlatformService.template();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartData,
                InterestRateChartApiConstants.INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("productId") final Long productId) {

        this.context.authenticatedUser().validateHasReadPermission(InterestRateChartApiConstants.INTERESTRATE_CHART_RESOURCE_NAME);

        Collection<InterestRateChartData> chartDatas = this.chartReadPlatformService.retrieveAllWithSlabs(productId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, chartDatas,
                InterestRateChartApiConstants.INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("chartId") final Long chartId, @Context final UriInfo uriInfo) {

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

        return this.toApiJsonSerializer.serialize(settings, chartData,
                InterestRateChartApiConstants.INTERESTRATE_CHART_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChart().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("chartId") final Long chartId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChart(chartId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chartId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("chartId") final Long chartId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChart(chartId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}