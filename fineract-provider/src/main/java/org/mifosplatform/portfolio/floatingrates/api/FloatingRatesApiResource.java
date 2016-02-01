/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.api;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.floatingrates.data.FloatingRateData;
import org.mifosplatform.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/floatingrates")
@Component
@Scope("singleton")
public class FloatingRatesApiResource {

	private static final String RESOURCE_NAME = "FLOATINGRATE";
	private static final Set<String> LIST_FLOATING_RATES_PARAMETERS = new HashSet<>(
			Arrays.asList("id", "name", "isBaseLendingRate", "isActive",
					"createdby", "createdOn", "modifiedBy", "modifiedOn"));
	private static final Set<String> INDIVIDUAL_FLOATING_RATES_PARAMETERS = new HashSet<>(
			Arrays.asList("id", "name", "isBaseLendingRate", "isActive",
					"createdBy", "createdOn", "modifiedBy", "modifiedOn",
					"ratePeriods"));
	private final PlatformSecurityContext context;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<FloatingRateData> toApiJsonSerializer;
	private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;

	@Autowired
	public FloatingRatesApiResource(
			final PlatformSecurityContext context,
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
	public String createFloatingRate(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.createFloatingRate().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAll(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(
				RESOURCE_NAME);
		final List<FloatingRateData> floatingRates = this.floatingRatesReadPlatformService
				.retrieveAll();
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, floatingRates,
				FloatingRatesApiResource.LIST_FLOATING_RATES_PARAMETERS);
	}

	@GET
	@Path("{floatingRateId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOne(
			@PathParam("floatingRateId") final Long floatingRateId,
			@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(
				RESOURCE_NAME);
		final FloatingRateData floatingRates = this.floatingRatesReadPlatformService
				.retrieveOne(floatingRateId);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, floatingRates,
				FloatingRatesApiResource.INDIVIDUAL_FLOATING_RATES_PARAMETERS);
	}

	@PUT
	@Path("{floatingRateId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateFloatingRate(
			@PathParam("floatingRateId") final Long floatingRateId,
			final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.updateFloatingRate(floatingRateId)
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}
