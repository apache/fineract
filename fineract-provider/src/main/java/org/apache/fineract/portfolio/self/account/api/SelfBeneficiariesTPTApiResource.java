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
package org.apache.fineract.portfolio.self.account.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.service.AccountTransferEnumerations;
import org.apache.fineract.portfolio.self.account.data.SelfBeneficiariesTPTData;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/beneficiaries/tpt")
@Component
@Scope("singleton")
public class SelfBeneficiariesTPTApiResource {

	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<SelfBeneficiariesTPTData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final SelfBeneficiariesTPTReadPlatformService readPlatformService;
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			SelfBeneficiariesTPTApiConstants.NAME_PARAM_NAME, SelfBeneficiariesTPTApiConstants.OFFICE_NAME_PARAM_NAME,
			SelfBeneficiariesTPTApiConstants.ACCOUNT_NUMBER_PARAM_NAME,
			SelfBeneficiariesTPTApiConstants.ACCOUNT_TYPE_PARAM_NAME,
			SelfBeneficiariesTPTApiConstants.TRANSFER_LIMIT_PARAM_NAME, SelfBeneficiariesTPTApiConstants.ID_PARAM_NAME,
			SelfBeneficiariesTPTApiConstants.CLIENT_NAME_PARAM_NAME,
			SelfBeneficiariesTPTApiConstants.ACCOUNT_TYPE_OPTIONS_PARAM_NAME));

	@Autowired
	public SelfBeneficiariesTPTApiResource(
			final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<SelfBeneficiariesTPTData> toApiJsonSerializer,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final SelfBeneficiariesTPTReadPlatformService readPlatformService) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.readPlatformService = readPlatformService;
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String template(@Context final UriInfo uriInfo) {

		final EnumOptionData loanAccountType = AccountTransferEnumerations
				.accountType(PortfolioAccountType.LOAN);
		final EnumOptionData savingsAccountType = AccountTransferEnumerations
				.accountType(PortfolioAccountType.SAVINGS);

		final Collection<EnumOptionData> accountTypeOptions = Arrays.asList(
				savingsAccountType, loanAccountType);

		SelfBeneficiariesTPTData templateData = new SelfBeneficiariesTPTData(
				accountTypeOptions);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, templateData, RESPONSE_DATA_PARAMETERS);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String add(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.addSelfServiceBeneficiaryTPT().withJson(apiRequestBodyAsJson)
				.build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("{beneficiaryId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String update(@PathParam("beneficiaryId") final Long beneficiaryId,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.updateSelfServiceBeneficiaryTPT(beneficiaryId)
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@DELETE
	@Path("{beneficiaryId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String delete(@PathParam("beneficiaryId") final Long beneficiaryId,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.deleteSelfServiceBeneficiaryTPT(beneficiaryId)
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAll(@Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(
				SelfBeneficiariesTPTApiConstants.BENEFICIARY_ENTITY_NAME);

		final Collection<SelfBeneficiariesTPTData> beneficiaries = this.readPlatformService
				.retrieveAll();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, beneficiaries, RESPONSE_DATA_PARAMETERS);
	}

}
