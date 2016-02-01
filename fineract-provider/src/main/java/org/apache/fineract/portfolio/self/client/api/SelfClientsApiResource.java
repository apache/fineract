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
package org.apache.fineract.portfolio.self.client.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.documentmanagement.api.ImagesApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.ClientChargesApiResource;
import org.apache.fineract.portfolio.client.api.ClientTransactionsApiResource;
import org.apache.fineract.portfolio.client.api.ClientsApiResource;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.data.SelfClientDataValidator;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/clients")
@Component
@Scope("singleton")
public class SelfClientsApiResource {

	private final PlatformSecurityContext context;
	private final ClientsApiResource clientApiResource;
	private final ImagesApiResource imagesApiResource;
	private final ClientChargesApiResource clientChargesApiResource;
	private final ClientTransactionsApiResource clientTransactionsApiResource;
	private final AppuserClientMapperReadService appUserClientMapperReadService;
	private final SelfClientDataValidator dataValidator;

	@Autowired
	public SelfClientsApiResource(
			final PlatformSecurityContext context,
			final ClientsApiResource clientApiResource,
			final ImagesApiResource imagesApiResource,
			final ClientChargesApiResource clientChargesApiResource,
			final ClientTransactionsApiResource clientTransactionsApiResource,
			final AppuserClientMapperReadService appUserClientMapperReadService,
			final SelfClientDataValidator dataValidator) {
		this.context = context;
		this.clientApiResource = clientApiResource;
		this.imagesApiResource = imagesApiResource;
		this.clientChargesApiResource = clientChargesApiResource;
		this.clientTransactionsApiResource = clientTransactionsApiResource;
		this.appUserClientMapperReadService = appUserClientMapperReadService;
		this.dataValidator = dataValidator;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAll(@Context final UriInfo uriInfo,
			@QueryParam("displayName") final String displayName,
			@QueryParam("firstName") final String firstname,
			@QueryParam("lastName") final String lastname,
			@QueryParam("offset") final Integer offset,
			@QueryParam("limit") final Integer limit,
			@QueryParam("orderBy") final String orderBy,
			@QueryParam("sortOrder") final String sortOrder) {

		final String sqlSearch = null;
		final Long officeId = null;
		final String externalId = null;
		final String hierarchy = null;
		final Boolean orphansOnly = null;
		return this.clientApiResource.retrieveAll(uriInfo, sqlSearch, officeId,
				externalId, displayName, firstname, lastname, hierarchy,
				offset, limit, orderBy, sortOrder, orphansOnly, true);
	}

	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOne(@PathParam("clientId") final Long clientId,
			@Context final UriInfo uriInfo) {

		this.dataValidator.validateRetrieveOne(uriInfo);

		validateAppuserClientsMapping(clientId);

		final boolean staffInSelectedOfficeOnly = false;
		return this.clientApiResource.retrieveOne(clientId, uriInfo,
				staffInSelectedOfficeOnly);
	}

	@GET
	@Path("{clientId}/accounts")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAssociatedAccounts(
			@PathParam("clientId") final Long clientId,
			@Context final UriInfo uriInfo) {

		validateAppuserClientsMapping(clientId);

		return this.clientApiResource.retrieveAssociatedAccounts(clientId,
				uriInfo);
	}

	@GET
	@Path("{clientId}/images")
	@Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML,
			MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public Response retrieveImage(@PathParam("clientId") final Long clientId,
			@QueryParam("maxWidth") final Integer maxWidth,
			@QueryParam("maxHeight") final Integer maxHeight,
			@QueryParam("output") final String output) {

		validateAppuserClientsMapping(clientId);

		return this.imagesApiResource.retrieveImage("clients", clientId,
				maxWidth, maxHeight, output);
	}

	@GET
	@Path("{clientId}/charges")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientCharges(
			@PathParam("clientId") final Long clientId,
			@DefaultValue(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL) @QueryParam(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS) final String chargeStatus,
			@QueryParam("pendingPayment") final Boolean pendingPayment,
			@Context final UriInfo uriInfo,
			@QueryParam("limit") final Integer limit,
			@QueryParam("offset") final Integer offset) {

		validateAppuserClientsMapping(clientId);

		return this.clientChargesApiResource.retrieveAllClientCharges(clientId,
				chargeStatus, pendingPayment, uriInfo, limit, offset);
	}

	@GET
	@Path("{clientId}/charges/{chargeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientCharge(
			@PathParam("clientId") final Long clientId,
			@PathParam("chargeId") final Long chargeId,
			@Context final UriInfo uriInfo) {

		this.dataValidator.validateClientCharges(uriInfo);

		validateAppuserClientsMapping(clientId);

		return this.clientChargesApiResource.retrieveClientCharge(clientId,
				chargeId, uriInfo);
	}

	@GET
	@Path("{clientId}/transactions")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllClientTransactions(
			@PathParam("clientId") final Long clientId,
			@Context final UriInfo uriInfo,
			@QueryParam("offset") final Integer offset,
			@QueryParam("limit") final Integer limit) {

		validateAppuserClientsMapping(clientId);

		return this.clientTransactionsApiResource
				.retrieveAllClientTransactions(clientId, uriInfo, offset, limit);
	}

	@GET
	@Path("{clientId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientTransaction(
			@PathParam("clientId") final Long clientId,
			@PathParam("transactionId") final Long transactionId,
			@Context final UriInfo uriInfo) {

		validateAppuserClientsMapping(clientId);

		return this.clientTransactionsApiResource.retrieveClientTransaction(
				clientId, transactionId, uriInfo);
	}

	private void validateAppuserClientsMapping(final Long clientId) {
		AppUser user = this.context.authenticatedUser();
		final boolean mappedClientId = this.appUserClientMapperReadService
				.isClientMappedToUser(clientId, user.getId());
		if (!mappedClientId) {
			throw new ClientNotFoundException(clientId);
		}
	}

}