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

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
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

@Path("/client")
@Component
@Scope("singleton")
@Api(value = "Clients Address", description = "Address module is an optional module and can be configured into the system by using GlobalConfiguration setting: enable-address. In order to activate Address module, we need to enable the configuration, enable-address by setting its value to true.")
public class ClientAddressApiResources {
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("addressId", "street",
			"addressLine1", "addressLine2", "addressLine3", "townVillage", "city", "countyDistrict", "stateProvinceId",
			"countryId", "postalCode", "latitude", "longitude", "createdBy", "createdOn", "updatedBy", "updatedOn",
			"clientAddressId", "client_id", "address_id", "address_type_id", "isActive", "fieldConfigurationId",
			"entity", "table", "field", "is_enabled", "is_mandatory", "validation_regex"));
	private final String resourceNameForPermissions = "Address";
	private final PlatformSecurityContext context;
	private final AddressReadPlatformServiceImpl readPlatformService;
	private final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

	@Autowired
	public ClientAddressApiResources(final PlatformSecurityContext context,
			final AddressReadPlatformServiceImpl readPlatformService,
			final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.context = context;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

	@GET
	@Path("addresses/template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getAddressesTemplate(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final AddressData template = this.readPlatformService.retrieveTemplate();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, template, this.RESPONSE_DATA_PARAMETERS);

	}

	@POST
	@Path("/{clientid}/addresses")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Create an address for a Client", notes = "Mandatory Fields : \n" + "type and clientId")
	@ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ClientAddressApiResourcesSwagger.PostClientClientIdAddressesRequest.class)})
	@ApiResponses({ @ApiResponse(code = 200, message = "OK", response = ClientAddressApiResourcesSwagger.PostClientClientIdAddressesResponse.class)})
	public String AddClientAddress(@QueryParam("type") @ApiParam(value = "type") final long addressTypeId,
			@PathParam("clientid") @ApiParam(value = "clientId") final long clientid, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().addClientAddress(clientid, addressTypeId)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("/{clientid}/addresses")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "List all addresses for a Client", notes = "Example Requests:\n" + "\n" +"client/1/addresses\n" + "\n" +	"\n" +"clients/1/addresses?status=false,true&&type=1,2,3" )
	@ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = ClientAddressApiResourcesSwagger.GetClientClientIdAddressesResponse.class)})
	public String getAddresses(@QueryParam("status") @ApiParam(value = "status") final String status, @QueryParam("type") @ApiParam(value = "type") final long addressTypeId,
							   @PathParam("clientid") @ApiParam(value = "clientId") final long clientid, @Context final UriInfo uriInfo) {
		Collection<AddressData> address;

		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		if (addressTypeId == 0 && status == null) {
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

	@PUT
	@Path("/{clientid}/addresses")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Update an address for a Client", notes = "All the address fields can be updated by using update client address API\n" + "\n" + "Mandatory Fields\n" + "type and addressId")
	@ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ClientAddressApiResourcesSwagger.PutClientClientIdAddressesRequest.class)})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ClientAddressApiResourcesSwagger.PutClientClientIdAddressesResponse.class)})
	public String UpdateClientAddress(@PathParam("clientid") @ApiParam(value = "clientId") final long clientid, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateClientAddress(clientid)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

}
