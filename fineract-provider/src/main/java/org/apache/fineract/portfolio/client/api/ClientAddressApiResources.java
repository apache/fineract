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
package org.apache.fineract.portfolio.client.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientid}/addresses")
@Component
@Scope("singleton")
public class ClientAddressApiResources {
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList("addressId", "street", "address_line_1", "address_line_2", "address_line_3", "town_village",
					"city", "county_district", "state_province_id", "country_id", "postal_code", "latitude",
					"longitude", "created_by", "created_on", "updated_by", "updated_on"));
	private final String resourceNameForPermissions = "Address";
	private final PlatformSecurityContext context;
	private final AddressReadPlatformServiceImpl readPlatformService;
	private final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer;
	private final FieldConfigurationReadPlatformService readPlatformServicefld;
	private final DefaultToApiJsonSerializer<FieldConfigurationData> toApiJsonSerializerfld;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

	@Autowired
	public ClientAddressApiResources(final PlatformSecurityContext context,
			final AddressReadPlatformServiceImpl readPlatformService,
			final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer,
			final FieldConfigurationReadPlatformService readPlatformServicefld,
			final DefaultToApiJsonSerializer<FieldConfigurationData> toApiJsonSerializerfld,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.context = context;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.readPlatformServicefld = readPlatformServicefld;
		this.toApiJsonSerializerfld = toApiJsonSerializerfld;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getAddresses(@QueryParam("status") final String status, @QueryParam("type") final long addressTypeId,
			@PathParam("clientid") final long clientid, @Context final UriInfo uriInfo) {
		Collection<AddressData> address;

		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
		System.out.println("addressType Id received" + addressTypeId);
		if (addressTypeId == 0 && status == null) {
			System.out.println("client Id received" + clientid);

			address = this.readPlatformService.retrieveAllClientAddress(clientid);
		} else if (addressTypeId != 0 && status == null) {
			address = this.readPlatformService.retrieveAddressbyType(clientid, addressTypeId);
		} else if (addressTypeId != 0 && status != null) {
			address = this.readPlatformService.retrieveAddressbyTypeAndStatus(clientid, addressTypeId, status);
		} else {
			address = this.readPlatformService.retrieveAddressbyStatus(clientid, status);
		}

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, address, this.RESPONSE_DATA_PARAMETERS);

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String AddClientAddress(@QueryParam("type") final long addressTypeId,
			@PathParam("clientid") final long clientid, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().addClientAddress(clientid, addressTypeId)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String UpdateClientAddress(@QueryParam("type") final long addressTypeId,
			@QueryParam("status") final boolean status, @PathParam("clientid") final long clientid,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.updateClientAddress(clientid, addressTypeId, status).withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

}
