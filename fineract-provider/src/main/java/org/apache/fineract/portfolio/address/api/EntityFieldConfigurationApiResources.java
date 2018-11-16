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
package org.apache.fineract.portfolio.address.api;

import io.swagger.annotations.*;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadPlatformService;
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

@Path("/fieldconfiguration/{entity}")
@Component
@Scope("singleton")
@Api(value = "Entity Field Configuration", description = "Entity Field configuration API is a generic and extensible \n" + "wherein various entities and subentities can be related.\n" + "Also it gives the user an ability to enable/disable fields,\n" + "add regular expression for validation")
public class EntityFieldConfigurationApiResources {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("clientAddressId", "client_id",
			"address_id", "address_type_id", "isActive", "fieldConfigurationId", "entity", "table", "field",
			"is_enabled", "is_mandatory", "validation_regex"));
	private final String resourceNameForPermissions = "Address";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer;
	private final FieldConfigurationReadPlatformService readPlatformServicefld;
	private final DefaultToApiJsonSerializer<FieldConfigurationData> toApiJsonSerializerfld;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

	@Autowired
	public EntityFieldConfigurationApiResources(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<AddressData> toApiJsonSerializer,
			final FieldConfigurationReadPlatformService readPlatformServicefld,
			final DefaultToApiJsonSerializer<FieldConfigurationData> toApiJsonSerializerfld,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.readPlatformServicefld = readPlatformServicefld;
		this.toApiJsonSerializerfld = toApiJsonSerializerfld;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
       @ApiOperation(httpMethod = "GET", value = "Retrieves the Entity Field Configuration", notes = "It retrieves all the Entity Field Configuration")
       @ApiResponses({@ApiResponse(code = 200, message = "OK", response = EntityFieldConfigurationApiResourcesSwagger.GetFieldConfigurationEntityResponse.class, responseContainer = "List")})
	public String getAddresses(@PathParam("entity") @ApiParam(value = "entity") final String entityname, @Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<FieldConfigurationData> fldconfig = this.readPlatformServicefld
				.retrieveFieldConfiguration(entityname);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerfld.serialize(settings, fldconfig, this.RESPONSE_DATA_PARAMETERS);

	}


}
