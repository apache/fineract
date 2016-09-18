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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.data.FieldConfigurationData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.apache.fineract.portfolio.address.service.FieldConfigurationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/fieldconfiguration/{entity}")
@Component
@Scope("singleton")
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
	public String getAddresses(@PathParam("entity") final String entityname, @Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<FieldConfigurationData> fldconfig = this.readPlatformServicefld
				.retrieveFieldConfiguration(entityname);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerfld.serialize(settings, fldconfig, this.RESPONSE_DATA_PARAMETERS);

	}


}
