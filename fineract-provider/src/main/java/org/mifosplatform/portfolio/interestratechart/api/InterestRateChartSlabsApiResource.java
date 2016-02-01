/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.api;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESOURCE_NAME;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartSlabApiConstants.INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS;

import java.util.Collection;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartSlabReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/interestratecharts/{chartId}/chartslabs")
@Component
@Scope("singleton")
public class InterestRateChartSlabsApiResource {

    private final InterestRateChartSlabReadPlatformService interestRateChartSlabReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<InterestRateChartSlabData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

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
    public String retrieveAll(@PathParam("chartId") final Long chartId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(INTERESTRATE_CHART_SLAB_RESOURCE_NAME);
        Collection<InterestRateChartSlabData> chartSlabs = this.interestRateChartSlabReadPlatformService.retrieveAll(chartId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, chartSlabs, INTERESTRATE_CHART_SLAB_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("chartId") final Long chartId, @PathParam("chartSlabId") final Long chartSlabId,
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
    public String create(@PathParam("chartId") final Long chartId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInterestRateChartSlab(chartId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("chartId") final Long chartId, @PathParam("chartSlabId") final Long chartSlabId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInterestRateChartSlab(chartId, chartSlabId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chartSlabId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("chartId") final Long chartId, @PathParam("chartSlabId") final Long chartSlabId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInterestRateChartSlab(chartId, chartSlabId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}