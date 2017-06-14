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
package org.apache.fineract.portfolio.self.savings.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.api.SavingsAccountChargesApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.savings.data.SelfSavingsDataValidator;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/savingsaccounts")
@Component
@Scope("singleton")
public class SelfSavingsApiResource {

	private final PlatformSecurityContext context;
	private final SavingsAccountsApiResource savingsAccountsApiResource;
	private final SavingsAccountChargesApiResource savingsAccountChargesApiResource;
	private final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource;
	private final AppuserSavingsMapperReadService appuserSavingsMapperReadService;
	private final SelfSavingsDataValidator dataValidator;

	@Autowired
	public SelfSavingsApiResource(
			final PlatformSecurityContext context,
			final SavingsAccountsApiResource savingsAccountsApiResource,
			final SavingsAccountChargesApiResource savingsAccountChargesApiResource,
			final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource,
			final AppuserSavingsMapperReadService appuserSavingsMapperReadService,
			final SelfSavingsDataValidator dataValidator) {
		this.context = context;
		this.savingsAccountsApiResource = savingsAccountsApiResource;
		this.savingsAccountChargesApiResource = savingsAccountChargesApiResource;
		this.savingsAccountTransactionsApiResource = savingsAccountTransactionsApiResource;
		this.appuserSavingsMapperReadService = appuserSavingsMapperReadService;
		this.dataValidator = dataValidator;
	}

	@GET
	@Path("{accountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSavings(
			@PathParam("accountId") final Long accountId,
			@DefaultValue("all") @QueryParam("chargeStatus") final String chargeStatus,
			@Context final UriInfo uriInfo) {

		this.dataValidator.validateRetrieveSavings(uriInfo);

		validateAppuserSavingsAccountMapping(accountId);

		final boolean staffInSelectedOfficeOnly = false;
		return this.savingsAccountsApiResource.retrieveOne(accountId,
				staffInSelectedOfficeOnly, chargeStatus, uriInfo);
	}

	@GET
	@Path("{accountId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSavingsTransaction(
			@PathParam("accountId") final Long accountId,
			@PathParam("transactionId") final Long transactionId,
			@Context final UriInfo uriInfo) {

		this.dataValidator.validateRetrieveSavingsTransaction(uriInfo);

		validateAppuserSavingsAccountMapping(accountId);

		return this.savingsAccountTransactionsApiResource.retrieveOne(
				accountId, transactionId, uriInfo);
	}

	@GET
	@Path("{accountId}/charges")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllSavingsAccountCharges(
			@PathParam("accountId") final Long accountId,
			@DefaultValue("all") @QueryParam("chargeStatus") final String chargeStatus,
			@Context final UriInfo uriInfo) {

		validateAppuserSavingsAccountMapping(accountId);

		return this.savingsAccountChargesApiResource
				.retrieveAllSavingsAccountCharges(accountId, chargeStatus,
						uriInfo);
	}

	@GET
	@Path("{accountId}/charges/{savingsAccountChargeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSavingsAccountCharge(
			@PathParam("accountId") final Long accountId,
			@PathParam("savingsAccountChargeId") final Long savingsAccountChargeId,
			@Context final UriInfo uriInfo) {

		validateAppuserSavingsAccountMapping(accountId);

		return this.savingsAccountChargesApiResource
				.retrieveSavingsAccountCharge(accountId,
						savingsAccountChargeId, uriInfo);
	}

	private void validateAppuserSavingsAccountMapping(final Long accountId) {
		AppUser user = this.context.authenticatedUser();
		final boolean isMappedSavings = this.appuserSavingsMapperReadService
				.isSavingsMappedToUser(accountId, user.getId());
		if (!isMappedSavings) {
			throw new SavingsAccountNotFoundException(accountId);
		}
	}

}
