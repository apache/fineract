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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
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
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients")
@Component
@Scope("singleton")
@Api(value = "Client Api", description = "Clients are people and businesses that have applied (or may apply) to an MFI for loans.\n" + "\n" + "Clients can be created in Pending or straight into Active state.")
public class ClientsApiResource {

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> clientAccountSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;

    @Autowired
    public ClientsApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final ToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final ToApiJsonSerializer<AccountSummaryCollectionData> clientAccountSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.clientAccountSummaryToApiJsonSerializer = clientAccountSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Client Details Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists" + "Example Request:\n" + "\n" + "clients/template")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = ClientData.class) })
    public String retrieveTemplate(@Context final UriInfo uriInfo, @ApiParam(value = "optional") @QueryParam("officeId") final Long officeId,
            @ApiParam(value = "optional If commandParam=close retrieves all closureReasons which are associated with \"ClientClosureReason\" value.", defaultValue = "false") @QueryParam("commandParam") final String commandParam,
            @DefaultValue("false") @ApiParam(value = "optional Defaults to false if not provided. If staffInSelectedOfficeOnly=true only staff who are associated with the selected branch are returned.") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

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
    @ApiOperation(value = "List Clients",
            notes = "Example Requests:\n" + "\n" + "clients\n" + "\n" + "clients?fields=displayName,officeName,timeline\n" + "\n" + "clients?offset=10&limit=50\n" + "\n" + "clients?orderBy=displayName&sortOrder=DESC" )
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ClientData.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Long officeId, @QueryParam("externalId") final String externalId,
            @QueryParam("displayName") final String displayName, @QueryParam("firstName") final String firstname,
            @QueryParam("lastName") final String lastname, @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("orderBy") final String orderBy, @QueryParam("sortOrder") final String sortOrder,
            @QueryParam("orphansOnly") final Boolean orphansOnly) {

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
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ClientData.class)})
    public String retrieveOne(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

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
    @ApiOperation(value = "Create a Client", httpMethod = "POST", notes = "Note:" + "\n" + "1. You can enter either:firstname/middlename/lastname - for a person (middlename is optional) OR fullname - for a business or organisation (or person known by one name).\n" + "\n" + "2.If address is enable(enable-address=true), then additional field called address has to be passed.", response = CommandProcessingResult.class, consumes="application/json")
    @ApiImplicitParams({@ApiImplicitParam( paramType = "body", dataType = "ClientData", required = true, type = "body", dataTypeClass = ClientData.class)})
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully Client Created", response = CommandProcessingResult.class) })
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
    @ApiOperation(value = "Update a Client", notes = "You can update any of the basic attributes of a client (but not its associations) using this API.\n" + "\n" + "Changing the relationship between a client and its office is not supported through this API. An API specific to handling transfers of clients between offices is available for the same.\n" + "\n" + "The relationship between a client and a group must be removed through the Groups API." )
    @ApiImplicitParams({@ApiImplicitParam( paramType = "body", dataType = "ClientData", required = true, type = "body", dataTypeClass = ClientData.class)})
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = CommandProcessingResult.class) })
    public String update(@ApiParam(value = "ClientId") @PathParam("clientId") final Long clientId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = CommandProcessingResult.class) })
    public String delete(@PathParam("clientId") final Long clientId) {

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
    @ApiOperation(value = "Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a Client Transfer | Withdraw a Client Transfer | Reject a Client Transfer | Accept a Client Transfer | Propose and Accept a Client Transfer | ",
            notes = "\n[Reject a Client] Mandatory Fields : rejectionDate, rejectionReasonId\n" + "\n[Withdraw a Client] Mandatory Fields : withdrawalDate, withdrawalReasonId\n" + "\n[Reactivate a Client] Mandatory Fields : reactivationDate\n" + "\n[UndoReject a Client] Mandatory Fields : reopenedDate\n" + "\n[UndoWithdraw a Client]Mandatory Fields :  reopenedDate\n" )
    @ApiImplicitParams({@ApiImplicitParam( paramType = "body", dataType = "body", required = true, type = "body")})
    public String activate(@PathParam("clientId") final Long clientId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

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
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = AccountSummaryCollectionData.class),
            @ApiResponse(code = 400, message = "Bad Request")
    })
    public String retrieveAssociatedAccounts(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final AccountSummaryCollectionData clientAccount = this.accountDetailsReadPlatformService.retrieveClientAccountDetails(clientId);

        final Set<String> CLIENT_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("loanAccounts", "savingsAccounts", "shareAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.clientAccountSummaryToApiJsonSerializer.serialize(settings, clientAccount, CLIENT_ACCOUNTS_DATA_PARAMETERS);
    }
}