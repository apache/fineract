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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.ObligeeData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/clients")
@Component
@Scope("singleton")
@Api(value = "Client", description = "Clients are people and businesses that have applied (or may apply) to an MFI for loans.\n" + "\n" + "Clients can be created in Pending or straight into Active state.")
public class ClientsApiResource {

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> clientAccountSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;

    @Autowired
    public ClientsApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final ToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final ToApiJsonSerializer<AccountSummaryCollectionData> clientAccountSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService,
			final BulkImportWorkbookService bulkImportWorkbookService,
			final GuarantorReadPlatformService guarantorReadPlatformService) {
		this.context = context;
		this.clientReadPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
        this.clientAccountSummaryToApiJsonSerializer = clientAccountSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Client Details Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Request:\n" + "\n" + "clients/template")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.GetClientsTemplateResponse.class)})
    public String retrieveTemplate(@Context final UriInfo uriInfo, @ApiParam(value = "officeId") @QueryParam("officeId") final Long officeId, @QueryParam("commandParam") @ApiParam(value = "commandParam") final String commandParam,
            @DefaultValue("false")  @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly")final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ClientData clientData = null;
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        if (is(commandParam, "close")) {
            clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_CLOSURE_REASON);
        } else if (is(commandParam, "acceptTransfer")) {
            clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_CLOSURE_REASON);
        } else if (is(commandParam, "reject")) {
            clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_REJECT_REASON);
        } else if (is(commandParam, "withdraw")) {
            clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_WITHDRAW_REASON);
        } else {
            clientData = this.clientReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Clients", notes = "The list capability of clients can support pagination and sorting.\n\n" + "Example Requests:\n" + "\n" + "clients\n" + "\n" + "clients?fields=displayName,officeName,timeline\n" + "\n" + "clients?offset=10&limit=50\n" + "\n" + "clients?orderBy=displayName&sortOrder=DESC" )
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.GetClientsResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId, @QueryParam("externalId") @ApiParam(value = "externalId") final String externalId,
            @QueryParam("displayName") @ApiParam(value = "displayName") final String displayName, @QueryParam("firstName") @ApiParam(value = "firstName") final String firstname,
            @QueryParam("lastName") @ApiParam(value = "lastName") final String lastname, @QueryParam("underHierarchy") @ApiParam(value = "underHierarchy") final String hierarchy,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder,
            @QueryParam("orphansOnly") @ApiParam(value = "orphansOnly") final Boolean orphansOnly) {

        return this.retrieveAll(uriInfo, sqlSearch, officeId, externalId, displayName, firstname, 
        		lastname, hierarchy, offset, limit, orderBy, sortOrder, orphansOnly, false);
    }
    
    public String retrieveAll(final UriInfo uriInfo, final String sqlSearch,
            final Long officeId, final String externalId,
            final String displayName, final String firstname,
            final String lastname, final String hierarchy,
            final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder,
            final Boolean orphansOnly, final boolean isSelfUser) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forClients(sqlSearch, officeId, externalId, displayName, firstname,
                lastname, hierarchy, offset, limit, orderBy, sortOrder, orphansOnly, isSelfUser);

        final Page<ClientData> clientData = this.clientReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Client", notes = "Example Requests:\n" + "\n" + "clients/1\n" +"\n" + "\n" + "clients/1?template=true\n" + "\n" + "\n" + "clients/1?fields=id,displayName,officeName" )
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.GetClientsClientIdResponse.class)})
    public String retrieveOne(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId, @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
        if (settings.isTemplate()) {
            final ClientData templateData = this.clientReadPlatformService.retrieveTemplate(clientData.officeId(),
                    staffInSelectedOfficeOnly);
            clientData = ClientData.templateOnTop(clientData, templateData);
            Collection<SavingsAccountData> savingAccountOptions = this.savingsAccountReadPlatformService.retrieveForLookup(clientId, null);
            if (savingAccountOptions != null && savingAccountOptions.size() > 0) {
                clientData = ClientData.templateWithSavingAccountOptions(clientData, savingAccountOptions);
            }
        }

        return this.toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Client", httpMethod = "POST", notes = "Note:\n\n" + "1. You can enter either:firstname/middlename/lastname - for a person (middlename is optional) OR fullname - for a business or organisation (or person known by one name).\n" + "\n" + "2.If address is enable(enable-address=true), then additional field called address has to be passed.\n\n" + "Mandatory Fields: firstname and lastname OR fullname, officeId, active=true and activationDate OR active=false, if(address enabled) address\n\n" + "Optional Fields: groupId, externalId, accountNo, staffId, mobileNo, savingsProductId, genderId, clientTypeId, clientClassificationId")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", dataType = "ClientData", required = true, type = "body", dataTypeClass = ClientsApiResourceSwagger.PostClientsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.PostClientsResponse.class) })
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {


        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createClient() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Client", notes = "Note: You can update any of the basic attributes of a client (but not its associations) using this API.\n" + "\n" + "Changing the relationship between a client and its office is not supported through this API. An API specific to handling transfers of clients between offices is available for the same.\n" + "\n" + "The relationship between a client and a group must be removed through the Groups API." )
    @ApiImplicitParams({@ApiImplicitParam( paramType = "body", dataType = "ClientData", required = true, type = "body", dataTypeClass = ClientsApiResourceSwagger.PutClientsClientIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.PutClientsClientIdResponse.class) })
    public String update(@ApiParam(value = "clientId") @PathParam("clientId") final Long clientId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateClient(clientId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Client", notes = "If a client is in Pending state, you are allowed to Delete it. The delete is a 'hard delete' and cannot be recovered from. Once clients become active or have loans or savings associated with them, you cannot delete the client but you may Close the client if they have left the program.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ClientsApiResourceSwagger.DeleteClientsClientIdRequest.class)})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.DeleteClientsClientIdResponse.class) })
    public String delete(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteClient(clientId) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a Client Transfer | Withdraw a Client Transfer | Reject a Client Transfer | Accept a Client Transfer | Propose and Accept a Client Transfer", notes = "Activate a Client:\n\n" + "Clients can be created in a Pending state. This API exists to enable client activation (for when a client becomes an approved member of the financial Institution).\n" + "\n" + "If the client happens to be already active this API will result in an error.\n\n" + "Close a Client:\n\n" + "Clients can be closed if they do not have any non-closed loans/savingsAccount. This API exists to close a client .\n" + "\n" + "If the client have any active loans/savingsAccount this API will result in an error.\n\n" + "Reject a Client:\n\n" + "Clients can be rejected when client is in pending for activation status.\n" + "\n" + "If the client is any other status, this API throws an error.\n\n" + "Mandatory Fields: rejectionDate, rejectionReasonId\n\n" + "Withdraw a Client:\n\n" + "Client applications can be withdrawn when client is in a pending for activation status.\n" + "\n" + "If the client is any other status, this API throws an error.\n\n" + "Mandatory Fields: withdrawalDate, withdrawalReasonId\n\n" + "Reactivate a Client: Clients can be reactivated after they have been closed.\n" + "\n" + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reactivationDate\n\n" + "UndoReject a Client:\n\n" + "Clients can be reactivated after they have been rejected.\n" + "\n" + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reopenedDate" + "UndoWithdraw a Client:\n\n" + "Clients can be reactivated after they have been withdrawn.\n" + "\n" + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reopenedDate\n\n" + "Assign a Staff:\n\n" + "Allows you to assign a Staff for existed Client.\n" + "\n" + "The selected Staff should belong to the same office (or an officer higher up in the hierarchy) as the Client he manages.\n\n" + "Unassign a Staff:\n\n" + "Allows you to unassign the Staff assigned to a Client.\n\n" + "Update Default Savings Account:\n\n" + "Allows you to modify or assign a default savings account for an existing Client.\n" + "\n" + "The selected savings account should be one among the existing savings account for a particular customer.\n\n" + "Propose a Client Transfer:\n\n" + "Allows you to propose the transfer of a Client to a different Office.\n\n" + "Withdraw a Client Transfer:\n\n" + "Allows you to withdraw the proposed transfer of a Client to a different Office.\n" + "\n" + "Withdrawal can happen only if the destination Branch (to which the transfer was proposed) has not already accepted the transfer proposal\n\n" + "Reject a Client Transfer:\n\n" + "Allows the Destination Branch to reject the proposed Client Transfer.\n\n" + "Accept a Client Transfer:\n\n" + "Allows the Destination Branch to accept the proposed Client Transfer.\n" + "\n" + "The destination branch may also choose to link this client to a group (in which case, any existing active JLG loan of the client is rescheduled to match the meeting frequency of the group) and loan Officer at the time of accepting the transfer\n\n" + "Propose and Accept a Client Transfer:\n\n" + "Abstraction over the Propose and Accept Client Transfer API's which enable a user with Data Scope over both the Target and Destination Branches to directly transfer a Client to the destination Office.\n\n" + "Showing request/response for 'Reject a Client Transfer'")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ClientsApiResourceSwagger.PostClientsClientIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK",response = ClientsApiResourceSwagger.PostClientsClientIdResponse.class)})
    public String activate(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        CommandWrapper commandRequest = null;
        if (is(commandParam, "activate")) {
            commandRequest = builder.activateClient(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "assignStaff")) {
            commandRequest = builder.assignClientStaff(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignStaff")) {
            commandRequest = builder.unassignClientStaff(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "close")) {
            commandRequest = builder.closeClient(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "proposeTransfer")) {
            commandRequest = builder.proposeClientTransfer(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "proposeAndAcceptTransfer")) {
            commandRequest = builder.proposeAndAcceptClientTransfer(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawTransfer")) {
            commandRequest = builder.withdrawClientTransferRequest(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "acceptTransfer")) {
            commandRequest = builder.acceptClientTransfer(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "rejectTransfer")) {
            commandRequest = builder.rejectClientTransfer(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "updateSavingsAccount")) {
            commandRequest = builder.updateClientSavingsAccount(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "reject")) {
            commandRequest = builder.rejectClient(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdraw")) {
            commandRequest = builder.withdrawClient(clientId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		} else if (is(commandParam, "reactivate")) {
			commandRequest = builder.reActivateClient(clientId).build();
			result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		} else if (is(commandParam, "undoRejection")) {
			commandRequest = builder.undoRejection(clientId).build();
			result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		} else if (is(commandParam, "undoWithdrawal")) {
			commandRequest = builder.undoWithdrawal(clientId).build();
			result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		}

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate",
                "unassignStaff", "assignStaff", "close", "proposeTransfer", "withdrawTransfer", "acceptTransfer", "rejectTransfer",
                "updateSavingsAccount", "reject", "withdraw", "reactivate" }); }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{clientId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve client accounts overview", notes = "An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the community application.\n" + "It is quite reasonable to add resources like this to simplify User Interface development.\n" + "\n" + "Example Requests:\n " + "\n" + "clients/1/accounts\n"+ "\n" + "clients/1/accounts?fields=loanAccounts,savingsAccounts" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ClientsApiResourceSwagger.GetClientsClientIdAccountsResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    public String retrieveAssociatedAccounts(@PathParam("clientId") @ApiParam(value = "clientId") final Long clientId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final AccountSummaryCollectionData clientAccount = this.accountDetailsReadPlatformService.retrieveClientAccountDetails(clientId);

        final Set<String> CLIENT_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("loanAccounts", "savingsAccounts", "shareAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.clientAccountSummaryToApiJsonSerializer.serialize(settings, clientAccount, CLIENT_ACCOUNTS_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getClientTemplate(@QueryParam("legalFormType")final String legalFormType,
            @QueryParam("officeId")final Long officeId,@QueryParam("staffId")final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(legalFormType, officeId, staffId,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postClientTemplate(@QueryParam("legalFormType")final String legalFormType,@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(legalFormType, uploadedInputStream,fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

	@GET
	@Path("{clientId}/obligeedetails")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveObligeeDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

		final List<ObligeeData> ObligeeList = this.guarantorReadPlatformService.retrieveObligeeDetails(clientId);

		return this.toApiJsonSerializer.serialize(ObligeeList);
	}

	@Path("{clientId}/transferproposaldate")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTransferTemplate(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

		this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
		final Date transferDate = this.clientReadPlatformService.retrieveClientTransferProposalDate(clientId);
		if (transferDate != null) {
			return this.toApiJsonSerializer.serialize(new LocalDate(transferDate));
		}
		return this.toApiJsonSerializer.serialize(transferDate);
	}
}