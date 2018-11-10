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

package org.apache.fineract.portfolio.self.shareaccounts.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.accounts.api.AccountsApiResource;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.accounts.data.AccountData;
import org.apache.fineract.portfolio.accounts.exceptions.ShareAccountNotFoundException;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.service.ProductReadPlatformService;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.shareaccounts.data.SelfShareAccountsDataValidator;
import org.apache.fineract.portfolio.self.shareaccounts.service.AppUserShareAccountsMapperReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountData;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/shareaccounts")
@Component
@Scope("singleton")
public class SelfShareAccountsApiResource {

	private final PlatformSecurityContext context;
	private final AccountsApiResource accountsApiResource;
	private final ShareAccountReadPlatformService readPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;

	private final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer;
	private final AppuserClientMapperReadService appuserClientMapperReadService;
	private final SelfShareAccountsDataValidator selfShareAccountsDataValidator;
	private final ProductReadPlatformService productReadPlatformService;
	private final ChargeReadPlatformService chargeReadPlatformService;
	private final AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService;

	@Autowired
	public SelfShareAccountsApiResource(final PlatformSecurityContext context,
			final AccountsApiResource accountsApiResource, final ShareAccountReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final AppuserClientMapperReadService appuserClientMapperReadService,
			final SelfShareAccountsDataValidator selfShareAccountsDataValidator,
			final ProductReadPlatformService productReadPlatformService,
			final ChargeReadPlatformService chargeReadPlatformService,
			final AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService) {
		this.context = context;
		this.accountsApiResource = accountsApiResource;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.selfShareAccountsDataValidator = selfShareAccountsDataValidator;
		this.appuserClientMapperReadService = appuserClientMapperReadService;
		this.productReadPlatformService = productReadPlatformService;
		this.chargeReadPlatformService = chargeReadPlatformService;
		this.appUserShareAccountsMapperReadPlatformService = appUserShareAccountsMapperReadPlatformService;

	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String template(@QueryParam("clientId") final Long clientId, @QueryParam("productId") final Long productId,
			@Context final UriInfo uriInfo) {
		
		validateAppuserClientsMapping(clientId);

		Collection<ProductData> productOptions = new ArrayList<ProductData>();
		if (productId != null) {
			final boolean includeTemplate = true;
			productOptions.add(productReadPlatformService.retrieveOne(productId, includeTemplate));
		} else {
			productOptions = productReadPlatformService.retrieveAllForLookup();
		}

		String clientName = null;

		final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSharesApplicableCharges();
		final ShareAccountData accountData = new ShareAccountData(clientId, clientName, productOptions, chargeOptions);

		return this.toApiJsonSerializer.serialize(accountData);

	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createAccount(final String apiRequestBodyAsJson) {
		HashMap<String, Object> attr = this.selfShareAccountsDataValidator
				.validateShareAccountApplication(apiRequestBodyAsJson);
		final Long clientId = (Long) attr.get(ShareAccountApiConstants.clientid_paramname);
		validateAppuserClientsMapping(clientId);
		String accountType = ShareAccountApiConstants.shareEntityType;
		return this.accountsApiResource.createAccount(accountType, apiRequestBodyAsJson);
	}

	@GET
	@Path("{accountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveShareAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {
		validateAppuserShareAccountMapping(accountId);
		final boolean includeTemplate = false;
		AccountData accountData = this.readPlatformService.retrieveOne(accountId, includeTemplate);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, accountData, readPlatformService.getResponseDataParams());
	}

	private void validateAppuserShareAccountMapping(final Long accountId) {
		AppUser user = this.context.authenticatedUser();
		final boolean isMapped = this.appUserShareAccountsMapperReadPlatformService
				.isShareAccountsMappedToUser(accountId, user.getId());
		if (!isMapped) {
			throw new ShareAccountNotFoundException(accountId);
		}
	}

	private void validateAppuserClientsMapping(final Long clientId) {
		AppUser user = this.context.authenticatedUser();
		final boolean mappedClientId = this.appuserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
		if (!mappedClientId) {
			throw new ClientNotFoundException(clientId);
		}
	}
}
