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
