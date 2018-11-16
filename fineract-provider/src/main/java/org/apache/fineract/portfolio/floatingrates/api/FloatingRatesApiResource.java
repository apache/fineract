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

import io.swagger.annotations.*;
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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/floatingrates")
@Component
@Scope("singleton")
@Api(value = "Floating Rates", description = "It lets you create, list, retrieve and upload the floating rates")
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
	@ApiOperation(value = "Create a new Floating Rate", httpMethod = "POST", notes = "Creates a new Floating Rate\n" + "Mandatory Fields: name\n" + "Optional Fields: isBaseLendingRate, isActive, ratePeriods")
	@ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = FloatingRatesApiResourceSwagger.PostFloatingRatesRequest.class)})
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = FloatingRatesApiResourceSwagger.PostFloatingRatesResponse.class)})
	public String createFloatingRate(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.createFloatingRate().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "List Floating Rates", httpMethod = "GET", notes = "Lists Floating Rates")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = FloatingRatesApiResourceSwagger.GetFloatingRatesResponse.class, responseContainer = "List")})
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
	@ApiOperation(value = "Retrieve Floating Rate", httpMethod = "GET", notes = "Retrieves Floating Rate")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = FloatingRatesApiResourceSwagger.GetFloatingRatesFloatingRateIdResponse.class)})
	public String retrieveOne(
			@PathParam("floatingRateId") @ApiParam(value = "floatingRateId") final Long floatingRateId,
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
	@ApiOperation(value = "Update Floating Rate", httpMethod = "PUT", notes = "Updates new Floating Rate. Rate Periods in the past cannot be modified. All the future rateperiods would be replaced with the new ratePeriods data sent.")
	@ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdRequest.class)})
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = FloatingRatesApiResourceSwagger.PutFloatingRatesFloatingRateIdResponse.class)})
	public String updateFloatingRate(
			@PathParam("floatingRateId") @ApiParam(value = "floatingRateId") final Long floatingRateId,
			@ApiParam(hidden = true) final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.updateFloatingRate(floatingRateId)
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}
