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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.ObligeeData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/clients")
@Component
@Tag(name = "Client", description = "Clients are people and businesses that have applied (or may apply) to an MFI for loans.\n" + "\n"
        + "Clients can be created in Pending or straight into Active state.")
@RequiredArgsConstructor
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
    private final SqlValidator sqlValidator;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Request:\n" + "\n" + "clients/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsTemplateResponse.class))) })
    public String retrieveTemplate(@Context final UriInfo uriInfo,
            @Parameter(description = "officeId") @QueryParam("officeId") final Long officeId,
            @QueryParam("commandParam") @Parameter(description = "commandParam") final String commandParam,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ClientData clientData = null;
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        if (CommandParameterUtil.is(commandParam, "close")) {
            clientData = clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_CLOSURE_REASON);
        } else if (CommandParameterUtil.is(commandParam, "acceptTransfer")) {
            clientData = clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_CLOSURE_REASON);
        } else if (CommandParameterUtil.is(commandParam, "reject")) {
            clientData = clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_REJECT_REASON);
        } else if (CommandParameterUtil.is(commandParam, "withdraw")) {
            clientData = clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_WITHDRAW_REASON);
        } else {
            clientData = clientReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly);
        }

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Clients", description = "The list capability of clients can support pagination and sorting.\n\n"
            + "Example Requests:\n" + "\n" + "clients\n" + "\n" + "clients?fields=displayName,officeName,timeline\n" + "\n"
            + "clients?offset=10&limit=50\n" + "\n" + "clients?orderBy=displayName&sortOrder=DESC")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("firstName") @Parameter(description = "firstName") final String firstname,
            @QueryParam("lastName") @Parameter(description = "lastName") final String lastname,
            @QueryParam("status") @Parameter(description = "status") final String status,
            @QueryParam("underHierarchy") @Parameter(description = "underHierarchy") final String hierarchy,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("orphansOnly") @Parameter(description = "orphansOnly") final Boolean orphansOnly) {

        return retrieveAll(uriInfo, officeId, externalId, displayName, firstname, lastname, status, hierarchy, offset, limit, orderBy,
                sortOrder, orphansOnly, false);
    }

    @GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = "Example Requests:\n" + "\n" + "clients/1\n" + "\n" + "\n"
            + "clients/1?template=true\n" + "\n" + "\n" + "clients/1?fields=id,displayName,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsClientIdResponse.class))) })
    public String retrieveOne(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {
        return retrieveClient(clientId, null, staffInSelectedOfficeOnly, uriInfo);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Client", description = "Note:\n\n"
            + "1. You can enter either:firstname/middlename/lastname - for a person (middlename is optional) OR fullname - for a business or organisation (or person known by one name).\n"
            + "\n" + "2.If address is enable(enable-address=true), then additional field called address has to be passed.\n\n"
            + "Mandatory Fields: firstname and lastname OR fullname, officeId, active=true and activationDate OR active=false, if(address enabled) address\n\n"
            + "Optional Fields: groupId, externalId, accountNo, staffId, mobileNo, savingsProductId, genderId, clientTypeId, clientClassificationId")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsResponse.class))) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createClient() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Client", description = "Note: You can update any of the basic attributes of a client (but not its associations) using this API.\n"
            + "\n"
            + "Changing the relationship between a client and its office is not supported through this API. An API specific to handling transfers of clients between offices is available for the same.\n"
            + "\n" + "The relationship between a client and a group must be removed through the Groups API.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PutClientsClientIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PutClientsClientIdResponse.class))) })
    public String update(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return updateClient(clientId, null, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Client", description = "If a client is in Pending state, you are allowed to Delete it. The delete is a 'hard delete' and cannot be recovered from. Once clients become active or have loans or savings associated with them, you cannot delete the client but you may Close the client if they have left the program.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.DeleteClientsClientIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.DeleteClientsClientIdResponse.class))) })
    public String delete(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        return deleteClient(clientId, null);
    }

    @POST
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a Client Transfer | Withdraw a Client Transfer | Reject a Client Transfer | Accept a Client Transfer | Propose and Accept a Client Transfer", description = "Activate a Client:\n\n"
            + "Clients can be created in a Pending state. This API exists to enable client activation (for when a client becomes an approved member of the financial Institution).\n"
            + "\n" + "If the client happens to be already active this API will result in an error.\n\n" + "Close a Client:\n\n"
            + "Clients can be closed if they do not have any non-closed loans/savingsAccount. This API exists to close a client .\n" + "\n"
            + "If the client have any active loans/savingsAccount this API will result in an error.\n\n" + "Reject a Client:\n\n"
            + "Clients can be rejected when client is in pending for activation status.\n" + "\n"
            + "If the client is any other status, this API throws an error.\n\n" + "Mandatory Fields: rejectionDate, rejectionReasonId\n\n"
            + "Withdraw a Client:\n\n" + "Client applications can be withdrawn when client is in a pending for activation status.\n" + "\n"
            + "If the client is any other status, this API throws an error.\n\n"
            + "Mandatory Fields: withdrawalDate, withdrawalReasonId\n\n"
            + "Reactivate a Client: Clients can be reactivated after they have been closed.\n" + "\n"
            + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reactivationDate\n\n"
            + "UndoReject a Client:\n\n" + "Clients can be reactivated after they have been rejected.\n" + "\n"
            + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reopenedDate"
            + "UndoWithdraw a Client:\n\n" + "Clients can be reactivated after they have been withdrawn.\n" + "\n"
            + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reopenedDate\n\n"
            + "Assign a Staff:\n\n" + "Allows you to assign a Staff for existed Client.\n" + "\n"
            + "The selected Staff should belong to the same office (or an officer higher up in the hierarchy) as the Client he manages.\n\n"
            + "Unassign a Staff:\n\n" + "Allows you to unassign the Staff assigned to a Client.\n\n" + "Update Default Savings Account:\n\n"
            + "Allows you to modify or assign a default savings account for an existing Client.\n" + "\n"
            + "The selected savings account should be one among the existing savings account for a particular customer.\n\n"
            + "Propose a Client Transfer:\n\n" + "Allows you to propose the transfer of a Client to a different Office.\n\n"
            + "Withdraw a Client Transfer:\n\n" + "Allows you to withdraw the proposed transfer of a Client to a different Office.\n" + "\n"
            + "Withdrawal can happen only if the destination Branch (to which the transfer was proposed) has not already accepted the transfer proposal\n\n"
            + "Reject a Client Transfer:\n\n" + "Allows the Destination Branch to reject the proposed Client Transfer.\n\n"
            + "Accept a Client Transfer:\n\n" + "Allows the Destination Branch to accept the proposed Client Transfer.\n" + "\n"
            + "The destination branch may also choose to link this client to a group (in which case, any existing active JLG loan of the client is rescheduled to match the meeting frequency of the group) and loan Officer at the time of accepting the transfer\n\n"
            + "Propose and Accept a Client Transfer:\n\n"
            + "Abstraction over the Propose and Accept Client Transfer API's which enable a user with Data Scope over both the Target and Destination Branches to directly transfer a Client to the destination Office.\n\n"
            + "Showing request/response for 'Reject a Client Transfer'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsClientIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsClientIdResponse.class))) })
    public String activate(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return applyCommandOverClient(clientId, null, commandParam, apiRequestBodyAsJson);
    }

    @GET
    @Path("{clientId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client accounts overview", description = "An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the community application.\n"
            + "It is quite reasonable to add resources like this to simplify User Interface development.\n" + "\n" + "Example Requests:\n "
            + "\n" + "clients/1/accounts\n" + "\n" + "clients/1/accounts?fields=loanAccounts,savingsAccounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsClientIdAccountsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveAssociatedAccounts(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        return retrieveClientAccounts(clientId, null, uriInfo);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getClientTemplate(@QueryParam("legalFormType") final String legalFormType, @QueryParam("officeId") final Long officeId,
            @QueryParam("staffId") final Long staffId, @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(legalFormType, officeId, staffId, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload client template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postClientTemplate(@QueryParam("legalFormType") final String legalFormType,
            @FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(legalFormType, uploadedInputStream, fileDetail, locale,
                dateFormat);
        return toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("{clientId}/obligeedetails")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client obligee details", description = "Retrieve client obligee details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientObligeeDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveObligeeDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {
        return retrieveClientObligeeDetails(clientId, null);
    }

    @GET
    @Path("{clientId}/transferproposaldate")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client transfer template", description = "Retrieve client transfer template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientTransferProposalDateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveTransferTemplate(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {
        return retrieveClientTransferTemplate(clientId, null);
    }

    @GET
    @Path("/external-id/{externalId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client by External Id", description = "Example Requests:\n" + "\n" + "clients/123-456\n" + "\n" + "\n"
            + "clients/123-456?template=true\n" + "\n" + "\n" + "clients/123-456?fields=id,displayName,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsClientIdResponse.class))) })
    public String retrieveOne(@PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {
        return retrieveClient(null, externalId, staffInSelectedOfficeOnly, uriInfo);
    }

    @GET
    @Path("/external-id/{externalId}/accounts")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client accounts overview", description = "An example of how a loan portfolio summary can be provided. This is requested in a specific use case of the community application.\n"
            + "It is quite reasonable to add resources like this to simplify User Interface development.\n" + "\n" + "Example Requests:\n "
            + "\n" + "clients/123-456/accounts\n" + "\n" + "clients/123-456/accounts?fields=loanAccounts,savingsAccounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsClientIdAccountsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveAssociatedAccounts(@PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @Context final UriInfo uriInfo) {
        return retrieveClientAccounts(null, externalId, uriInfo);
    }

    @PUT
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Client using the External Id", description = "Note: You can update any of the basic attributes of a client (but not its associations) using this API.\n"
            + "\n"
            + "Changing the relationship between a client and its office is not supported through this API. An API specific to handling transfers of clients between offices is available for the same.\n"
            + "\n" + "The relationship between a client and a group must be removed through the Groups API.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PutClientsClientIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PutClientsClientIdResponse.class))) })
    public String update(@Parameter(description = "externalId") @PathParam("externalId") final String externalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return updateClient(null, externalId, apiRequestBodyAsJson);
    }

    @POST
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Activate a Client | Close a Client | Reject a Client | Withdraw a Client | Reactivate a Client | UndoReject a Client | UndoWithdraw a Client | Assign a Staff | Unassign a Staff | Update Default Savings Account | Propose a Client Transfer | Withdraw a Client Transfer | Reject a Client Transfer | Accept a Client Transfer | Propose and Accept a Client Transfer", description = "Activate a Client:\n\n"
            + "Clients can be created in a Pending state. This API exists to enable client activation (for when a client becomes an approved member of the financial Institution).\n"
            + "\n" + "If the client happens to be already active this API will result in an error.\n\n" + "Close a Client:\n\n"
            + "Clients can be closed if they do not have any non-closed loans/savingsAccount. This API exists to close a client .\n" + "\n"
            + "If the client have any active loans/savingsAccount this API will result in an error.\n\n" + "Reject a Client:\n\n"
            + "Clients can be rejected when client is in pending for activation status.\n" + "\n"
            + "If the client is any other status, this API throws an error.\n\n" + "Mandatory Fields: rejectionDate, rejectionReasonId\n\n"
            + "Withdraw a Client:\n\n" + "Client applications can be withdrawn when client is in a pending for activation status.\n" + "\n"
            + "If the client is any other status, this API throws an error.\n\n"
            + "Mandatory Fields: withdrawalDate, withdrawalReasonId\n\n"
            + "Reactivate a Client: Clients can be reactivated after they have been closed.\n" + "\n"
            + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reactivationDate\n\n"
            + "UndoReject a Client:\n\n" + "Clients can be reactivated after they have been rejected.\n" + "\n"
            + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reopenedDate"
            + "UndoWithdraw a Client:\n\n" + "Clients can be reactivated after they have been withdrawn.\n" + "\n"
            + "Trying to reactivate a client in any other state throws an error.\n\n" + "Mandatory Fields: reopenedDate\n\n"
            + "Assign a Staff:\n\n" + "Allows you to assign a Staff for existed Client.\n" + "\n"
            + "The selected Staff should belong to the same office (or an officer higher up in the hierarchy) as the Client he manages.\n\n"
            + "Unassign a Staff:\n\n" + "Allows you to unassign the Staff assigned to a Client.\n\n" + "Update Default Savings Account:\n\n"
            + "Allows you to modify or assign a default savings account for an existing Client.\n" + "\n"
            + "The selected savings account should be one among the existing savings account for a particular customer.\n\n"
            + "Propose a Client Transfer:\n\n" + "Allows you to propose the transfer of a Client to a different Office.\n\n"
            + "Withdraw a Client Transfer:\n\n" + "Allows you to withdraw the proposed transfer of a Client to a different Office.\n" + "\n"
            + "Withdrawal can happen only if the destination Branch (to which the transfer was proposed) has not already accepted the transfer proposal\n\n"
            + "Reject a Client Transfer:\n\n" + "Allows the Destination Branch to reject the proposed Client Transfer.\n\n"
            + "Accept a Client Transfer:\n\n" + "Allows the Destination Branch to accept the proposed Client Transfer.\n" + "\n"
            + "The destination branch may also choose to link this client to a group (in which case, any existing active JLG loan of the client is rescheduled to match the meeting frequency of the group) and loan Officer at the time of accepting the transfer\n\n"
            + "Propose and Accept a Client Transfer:\n\n"
            + "Abstraction over the Propose and Accept Client Transfer API's which enable a user with Data Scope over both the Target and Destination Branches to directly transfer a Client to the destination Office.\n\n"
            + "Showing request/response for 'Reject a Client Transfer'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsClientIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsClientIdResponse.class))) })
    public String applyCommand(@PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        return applyCommandOverClient(null, externalId, commandParam, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Client", description = "If a client is in Pending state, you are allowed to Delete it. The delete is a 'hard delete' and cannot be recovered from. Once clients become active or have loans or savings associated with them, you cannot delete the client but you may Close the client if they have left the program.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.DeleteClientsClientIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.DeleteClientsClientIdResponse.class))) })
    public String delete(@PathParam("externalId") @Parameter(description = "externalId") final String externalId) {
        return deleteClient(null, externalId);
    }

    @GET
    @Path("/external-id/{externalId}/obligeedetails")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client obligee details", description = "Retrieve client obligee details using the client external Id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientObligeeDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveObligeeDetails(@PathParam("externalId") final String externalId, @Context final UriInfo uriInfo) {
        return retrieveClientObligeeDetails(null, externalId);
    }

    @GET
    @Path("/external-id/{externalId}/transferproposaldate")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client transfer template", description = "Retrieve client transfer template using the client external Id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientTransferProposalDateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveTransferTemplate(@PathParam("externalId") final String externalId, @Context final UriInfo uriInfo) {
        return retrieveClientTransferTemplate(null, externalId);
    }

    public String retrieveAll(final UriInfo uriInfo, final Long officeId, final String externalId, final String displayName,
            final String firstname, final String lastname, final String status, final String hierarchy, final Integer offset,
            final Integer limit, final String orderBy, final String sortOrder, final Boolean orphansOnly, final boolean isSelfUser) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        sqlValidator.validate(hierarchy);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).officeId(officeId).externalId(externalId)
                .name(displayName).hierarchy(hierarchy).firstname(firstname).lastname(lastname).status(status).orphansOnly(orphansOnly)
                .isSelfUser(isSelfUser).offset(offset).orderBy(orderBy).sortOrder(sortOrder).build();
        final Page<ClientData> clientData = clientReadPlatformService.retrieveAll(searchParameters);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    private ClientData retrieveClientData(final Long clientId, final boolean staffInSelectedOfficeOnly, final boolean isTemplate) {
        ClientData clientData = clientReadPlatformService.retrieveOne(clientId);
        if (isTemplate) {
            final ClientData templateData = clientReadPlatformService.retrieveTemplate(clientData.getOfficeId(), staffInSelectedOfficeOnly);
            clientData = ClientData.templateOnTop(clientData, templateData);
            Collection<SavingsAccountData> savingAccountOptions = savingsAccountReadPlatformService.retrieveForLookup(clientId, null);
            if (savingAccountOptions != null && savingAccountOptions.size() > 0) {
                clientData = ClientData.templateWithSavingAccountOptions(clientData, savingAccountOptions);
            }
        }
        return clientData;
    }

    private Long getResolvedClientId(final Long clientId, final ExternalId clientExternalId) {
        Long resolvedClientId = clientId;
        if (resolvedClientId == null) {
            clientExternalId.throwExceptionIfEmpty();
            resolvedClientId = clientReadPlatformService.retrieveClientIdByExternalId(clientExternalId);
            if (resolvedClientId == null) {
                throw new ClientNotFoundException(resolvedClientId);
            }
        }
        return resolvedClientId;
    }

    private CommandWrapper evaluateCommand(final Long clientId, final String commandParam, final CommandWrapperBuilder builder) {
        CommandWrapper commandRequest = null;
        if (CommandParameterUtil.is(commandParam, "activate")) {
            commandRequest = builder.activateClient(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "assignStaff")) {
            commandRequest = builder.assignClientStaff(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "unassignStaff")) {
            commandRequest = builder.unassignClientStaff(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "close")) {
            commandRequest = builder.closeClient(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "proposeTransfer")) {
            commandRequest = builder.proposeClientTransfer(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "proposeAndAcceptTransfer")) {
            commandRequest = builder.proposeAndAcceptClientTransfer(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "withdrawTransfer")) {
            commandRequest = builder.withdrawClientTransferRequest(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "acceptTransfer")) {
            commandRequest = builder.acceptClientTransfer(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "rejectTransfer")) {
            commandRequest = builder.rejectClientTransfer(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "updateSavingsAccount")) {
            commandRequest = builder.updateClientSavingsAccount(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "reject")) {
            commandRequest = builder.rejectClient(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "withdraw")) {
            commandRequest = builder.withdrawClient(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "reactivate")) {
            commandRequest = builder.reActivateClient(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "undoRejection")) {
            commandRequest = builder.undoRejection(clientId).build();
        } else if (CommandParameterUtil.is(commandParam, "undoWithdrawal")) {
            commandRequest = builder.undoWithdrawal(clientId).build();
        }

        if (commandRequest == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[] { "activate", "unassignStaff", "assignStaff", "close", "proposeTransfer", "withdrawTransfer",
                            "acceptTransfer", "rejectTransfer", "updateSavingsAccount", "reject", "withdraw", "reactivate" });
        }

        return commandRequest;
    }

    private String retrieveClient(Long clientId, final String externalId, final boolean staffInSelectedOfficeOnly, final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final ClientData clientData = retrieveClientData(clientId, staffInSelectedOfficeOnly, settings.isTemplate());
        return toApiJsonSerializer.serialize(settings, clientData, ClientApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    private String updateClient(Long clientId, final String externalId, final String jsonPayload) {
        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateClient(clientId) //
                .withJson(jsonPayload) //
                .build(); //

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    private String deleteClient(Long clientId, final String externalId) {
        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteClient(clientId) //
                .build(); //

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    private String retrieveClientAccounts(Long clientId, final String externalId, final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final AccountSummaryCollectionData clientAccount = accountDetailsReadPlatformService.retrieveClientAccountDetails(clientId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return clientAccountSummaryToApiJsonSerializer.serialize(settings, clientAccount,
                ClientApiConstants.CLIENT_ACCOUNTS_DATA_PARAMETERS);
    }

    private String applyCommandOverClient(Long clientId, final String externalId, final String command, final String jsonPayload) {
        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonPayload);
        final CommandWrapper commandRequest = evaluateCommand(clientId, command, builder);
        CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    private String retrieveClientObligeeDetails(Long clientId, final String externalId) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final List<ObligeeData> ObligeeList = guarantorReadPlatformService.retrieveObligeeDetails(clientId);
        return toApiJsonSerializer.serialize(ObligeeList);
    }

    private String retrieveClientTransferTemplate(Long clientId, final String externalId) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ExternalId clientExternalId = ExternalIdFactory.produce(externalId);
        clientId = getResolvedClientId(clientId, clientExternalId);

        final LocalDate transferDate = clientReadPlatformService.retrieveClientTransferProposalDate(clientId);
        return toApiJsonSerializer.serialize(transferDate);
    }

}
