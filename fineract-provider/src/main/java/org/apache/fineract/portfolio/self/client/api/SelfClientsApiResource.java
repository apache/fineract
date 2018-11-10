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

import io.swagger.annotations.*;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/self/clients")
@Component
@Scope("singleton")
@Api(value = "Self Client", description = "")
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
	@ApiOperation(value = "List Clients associated to the user", httpMethod = "GET", notes = "The list capability of clients can support pagination and sorting.\n\n" + "Example Requests:\n" + "\n" + "self/clients\n" + "\n" + "self/clients?fields=displayName,officeName\n" + "\n" + "self/clients?offset=10&limit=50\n" + "\n" + "self/clients?orderBy=displayName&sortOrder=DESC")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsResponse.class)})
	public String retrieveAll(@Context final UriInfo uriInfo,
			@QueryParam("displayName") @ApiParam(value = "displayName") final String displayName,
			@QueryParam("firstName") @ApiParam(value = "firstName") final String firstname,
			@QueryParam("lastName") @ApiParam(value = "lastName") final String lastname,
			@QueryParam("offset") @ApiParam(value = "offset") final Integer offset,
			@QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
			@QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy,
			@QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {

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
	@ApiOperation(value = "Retrieve a Client", httpMethod = "GET", notes = "Retrieves a Client\n\n" + "Example Requests:\n" + "\n" + "self/clients/1\n" + "\n" + "self/clients/1?fields=id,displayName,officeName")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsClientIdResponse.class)})
	public String retrieveOne(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
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
	@ApiOperation(value = "Retrieve client accounts overview", httpMethod = "GET", notes = "An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the community application.\n" + "It is quite reasonable to add resources like this to simplify User Interface development.\n" + "\n" + "Example Requests:\n" + "\n" + "self/clients/1/accounts\n" + "\n" + "\n" + "self/clients/1/accounts?fields=loanAccounts,savingsAccounts")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsClientIdAccountsResponse.class)})
	public String retrieveAssociatedAccounts(
			@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
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
	@ApiOperation(value = "Retrieve Client Image", httpMethod = "GET", notes = "Optional arguments are identical to those of Get Image associated with an Entity (Binary file)\n" + "\n" + "Example Requests:\n" + "\n" + "self/clients/1/images")
	@ApiResponses({@ApiResponse(code = 200, message = "OK")})
	public Response retrieveImage(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
			@QueryParam("maxWidth") @ApiParam(example = "maxWidth") final Integer maxWidth,
			@QueryParam("maxHeight") @ApiParam(example = "maxHeight") final Integer maxHeight,
			@QueryParam("output") @ApiParam(example = "output") final String output) {

		validateAppuserClientsMapping(clientId);

		return this.imagesApiResource.retrieveImage("clients", clientId,
				maxWidth, maxHeight, output);
	}

	@GET
	@Path("{clientId}/charges")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "List Client Charges", httpMethod = "GET", notes = "The list capability of client charges supports pagination.\n\n" + "Example Requests:\n" + "\n" + "self/clients/1/charges\n\n" + "self/clients/1/charges?offset=0&limit=5")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsClientIdChargesResponse.class)})
	public String retrieveAllClientCharges(
			@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
			@DefaultValue(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL) @QueryParam(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS) @ApiParam(value = "chargeStatus") final String chargeStatus,
			@QueryParam("pendingPayment") @ApiParam(value = "pendingPayment") final Boolean pendingPayment,
			@Context final UriInfo uriInfo,
			@QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
			@QueryParam("offset") @ApiParam(value = "offset") final Integer offset) {

		validateAppuserClientsMapping(clientId);

		return this.clientChargesApiResource.retrieveAllClientCharges(clientId,
				chargeStatus, pendingPayment, uriInfo, limit, offset);
	}

	@GET
	@Path("{clientId}/charges/{chargeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Retrieve a Client Charge", httpMethod = "GET", notes = "Retrieves a Client Charge\n\n" + "Example Requests:\n" + "\n" + "self/clients/1/charges/1\n" + "\n" + "\n" + "self/clients/1/charges/1?fields=name,id")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsClientIdChargesChargeIdResponse.class)})
	public String retrieveClientCharge(
			@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
			@PathParam("chargeId") @ApiParam(value = "chargeId") final Long chargeId,
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
	@ApiOperation(value = "List Client Transactions", httpMethod = "GET", notes = "The list capability of client transaction can support pagination.\n\n" + "Example Requests:\n" + "\n" + "self/clients/189/transactions\n\n" + "self/clients/189/transactions?offset=10&limit=50")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsClientIdTransactionsResponse.class)})
	public String retrieveAllClientTransactions(
			@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
			@Context final UriInfo uriInfo,
			@QueryParam("offset") @ApiParam(value = "offset") final Integer offset,
			@QueryParam("limit") @ApiParam(value = "limit") final Integer limit) {

		validateAppuserClientsMapping(clientId);

		return this.clientTransactionsApiResource
				.retrieveAllClientTransactions(clientId, uriInfo, offset, limit);
	}

	@GET
	@Path("{clientId}/transactions/{transactionId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Retrieve a Client Transaction", httpMethod = "GET", notes = "Retrieves a Client Transaction" + "Example Requests:\n" + "\n" + "self/clients/1/transactions/1\n" + "\n" + "\n" + "self/clients/1/transactions/1?fields=id,officeName")
	@ApiResponses({@ApiResponse(code = 200, message = "OK", response = SelfClientsApiResourceSwagger.GetSelfClientsClientIdTransactionsTransactionIdResponse.class)})
	public String retrieveClientTransaction(
			@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId,
			@PathParam("transactionId") @ApiParam(value = "transactionId") final Long transactionId,
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
	
	@POST
	@Path("{clientId}/images")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addNewClientImage(@PathParam("clientId") final Long clientId,
			@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
			@FormDataParam("file") final FormDataContentDisposition fileDetails,
			@FormDataParam("file") final FormDataBodyPart bodyPart) {

		validateAppuserClientsMapping(clientId);
		return this.imagesApiResource.addNewClientImage(ClientApiConstants.clientEntityName, clientId, fileSize,
				inputStream, fileDetails, bodyPart);

	}
	
	@POST
	@Path("{clientId}/images")
	@Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addNewClientImage(@PathParam("entity") final String entityName,
			@PathParam("clientId") final Long clientId, final String jsonRequestBody) {
		validateAppuserClientsMapping(clientId);
		return this.imagesApiResource.addNewClientImage(ClientApiConstants.clientEntityName, clientId, jsonRequestBody);

	}

	@DELETE
	@Path("{clientId}/images")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteClientImage(@PathParam("clientId") final Long clientId) {
		
		validateAppuserClientsMapping(clientId);
		return this.imagesApiResource.deleteClientImage(ClientApiConstants.clientEntityName, clientId);

	}

	@GET
	@Path("{clientId}/obligeedetails")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveObligeeDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

		validateAppuserClientsMapping(clientId);

		return this.clientApiResource.retrieveObligeeDetails(clientId, uriInfo);
	}

}