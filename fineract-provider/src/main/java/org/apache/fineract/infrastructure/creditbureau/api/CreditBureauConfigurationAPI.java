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

package org.apache.fineract.infrastructure.creditbureau.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauConfigurationData;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauData;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauLoanProductMappingData;
import org.apache.fineract.infrastructure.creditbureau.data.OrganisationCreditBureauData;
import org.apache.fineract.infrastructure.creditbureau.service.CreditBureauLoanProductMappingReadPlatformService;
import org.apache.fineract.infrastructure.creditbureau.service.CreditBureauReadConfigurationService;
import org.apache.fineract.infrastructure.creditbureau.service.CreditBureauReadPlatformService;
import org.apache.fineract.infrastructure.creditbureau.service.OrganisationCreditBureauReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/CreditBureauConfiguration")
@Component
@Scope("singleton")
@Api(value = "CreditBureau Configuration")
public class CreditBureauConfigurationAPI {
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList("creditBureauId", "alias", "country", "creditBureauProductId", "startDate", "endDate", "isActive"));
	private final String resourceNameForPermissions = "CreditBureau";
	private final PlatformSecurityContext context;
	private final CreditBureauReadPlatformService readPlatformService;
	private final DefaultToApiJsonSerializer<CreditBureauData> toApiJsonSerializer;
	private final CreditBureauLoanProductMappingReadPlatformService readPlatformServiceCreditBureauLoanProduct;
	private final OrganisationCreditBureauReadPlatformService readPlatformServiceOrganisationCreditBureau;
	private final DefaultToApiJsonSerializer<CreditBureauLoanProductMappingData> toApiJsonSerializerCreditBureauLoanProduct;
	private final DefaultToApiJsonSerializer<OrganisationCreditBureauData> toApiJsonSerializerOrganisationCreditBureau;
	private final DefaultToApiJsonSerializer<CreditBureauConfigurationData> toApiJsonSerializerReport;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final CreditBureauReadConfigurationService creditBureauConfiguration;

	@Autowired
	public CreditBureauConfigurationAPI(final PlatformSecurityContext context,
			final CreditBureauReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<CreditBureauData> toApiJsonSerializer,
			final CreditBureauLoanProductMappingReadPlatformService readPlatformServiceCreditBureauLoanProduct,
			final DefaultToApiJsonSerializer<CreditBureauLoanProductMappingData> toApiJsonSerializerCreditBureauLoanProduct,
			final OrganisationCreditBureauReadPlatformService readPlatformServiceOrganisationCreditBureau,
			final DefaultToApiJsonSerializer<OrganisationCreditBureauData> toApiJsonSerializerOrganisationCreditBureau,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final DefaultToApiJsonSerializer<CreditBureauConfigurationData> toApiJsonSerializerReport,
			final CreditBureauReadConfigurationService creditBureauConfiguration) {
		this.context = context;
		this.readPlatformService = readPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.readPlatformServiceCreditBureauLoanProduct = readPlatformServiceCreditBureauLoanProduct;
		this.toApiJsonSerializerCreditBureauLoanProduct = toApiJsonSerializerCreditBureauLoanProduct;
		this.readPlatformServiceOrganisationCreditBureau = readPlatformServiceOrganisationCreditBureau;
		this.toApiJsonSerializerOrganisationCreditBureau = toApiJsonSerializerOrganisationCreditBureau;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.toApiJsonSerializerReport = toApiJsonSerializerReport;
		this.creditBureauConfiguration = creditBureauConfiguration;

	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getCreditBureau(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<CreditBureauData> creditBureau = this.readPlatformService.retrieveCreditBureau();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, creditBureau, this.RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("/mappings")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getCreditBureauLoanProductMapping(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<CreditBureauLoanProductMappingData> creditBureauLoanProductMapping = this.readPlatformServiceCreditBureauLoanProduct
				.readCreditBureauLoanProductMapping();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerCreditBureauLoanProduct.serialize(settings, creditBureauLoanProductMapping, this.RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("/organisationCreditBureau")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getOrganisationCreditBureau(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<OrganisationCreditBureauData> organisationCreditBureau = this.readPlatformServiceOrganisationCreditBureau
				.retrieveOrgCreditBureau();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerOrganisationCreditBureau.serialize(settings, organisationCreditBureau,
				this.RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("/config/{organisationCreditBureauId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getConfiguration(@PathParam("organisationCreditBureauId") final Long organisationCreditBureauId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<CreditBureauConfigurationData> configurationData = this.creditBureauConfiguration
				.readConfigurationByOrganisationCreditBureauId(organisationCreditBureauId);

		return this.toApiJsonSerializerReport.serialize(configurationData);
	}
	
	
	@GET
	@Path("/loanProduct")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String fetchLoanProducts(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

		final Collection<CreditBureauLoanProductMappingData> creditBureauLoanProductMapping = this.readPlatformServiceCreditBureauLoanProduct
				.fetchLoanProducts();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerCreditBureauLoanProduct.serialize(settings, creditBureauLoanProductMapping, this.RESPONSE_DATA_PARAMETERS);
	}

	@PUT
	@Path("/organisationCreditBureau")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateCreditBureau(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCreditBureau()
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("/mappings")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateCreditBureauLoanProductMapping(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCreditBureauLoanProductMapping()
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("/organisationCreditBureau/{organisationCreditBureauId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addOrganisationCreditBureau(@PathParam("organisationCreditBureauId") final Long organisationCreditBureauId, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().addOrganisationCreditBureau(organisationCreditBureauId)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("/mappings/{CreditBureauId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createCreditBureauLoanProductMapping(@PathParam("CreditBureauId") final Long CreditBureauId, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createCreditBureauLoanProductMapping(CreditBureauId)
				.withJson(apiRequestBodyAsJson).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}

	

}
