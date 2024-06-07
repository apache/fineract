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
package org.apache.fineract.portfolio.accounts.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.exception.ResourceNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.accounts.constants.AccountsApiConstants;
import org.apache.fineract.portfolio.accounts.data.AccountData;
import org.apache.fineract.portfolio.accounts.service.AccountReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Path("/v1/accounts/{type}")
@Component
@Tag(name = "Share Account", description = "Share accounts are instances of a praticular share product created for an individual. An application process around the creation of accounts is also supported.")
@RequiredArgsConstructor
public class AccountsApiResource {

    private final ApplicationContext applicationContext;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Share Account Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Requests:\n" + "\n" + "accounts/share/template?clientId=1\n"
            + "\n" + "\n" + "accounts/share/template?clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.GetAccountsTypeTemplateResponse.class))) })
    public String template(@PathParam("type") @Parameter(description = "type") final String accountType,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId, @Context final UriInfo uriInfo) {
        try {
            this.platformSecurityContext.authenticatedUser();
            String serviceName = accountType + AccountsApiConstants.READPLATFORM_NAME;
            AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName);
            final AccountData accountData = service.retrieveTemplate(clientId, productId);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, accountData, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a share application/account", description = "Retrieves a share application/account\n\n"
            + "Example Requests :\n" + "\n" + "shareaccount/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.GetAccountsTypeAccountIdResponse.class))) })
    public String retrieveAccount(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @PathParam("type") @Parameter(description = "type") final String accountType, @Context final UriInfo uriInfo) {
        try {
            String serviceName = accountType + AccountsApiConstants.READPLATFORM_NAME;
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName);
            AccountData data = service.retrieveOne(accountId, settings.isTemplate());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List share applications/accounts", description = "Lists share applications/accounts\n\n" + "Example Requests:\n"
            + "\n" + "shareaccount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.GetAccountsTypeResponse.class))) })
    public String retrieveAllAccounts(@PathParam("type") @Parameter(description = "type") final String accountType,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit, @Context final UriInfo uriInfo) {
        try {
            String serviceName = accountType + AccountsApiConstants.READPLATFORM_NAME;
            AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName);
            Page<AccountData> data = service.retrieveAll(offset, limit);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
        } catch (BeansException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Submit new share application", description = "Submits new share application\n\n"
            + "Mandatory Fields: clientId, productId, submittedDate, savingsAccountId, requestedShares, applicationDate\n\n"
            + "Optional Fields: accountNo, externalId\n\n"
            + "Inherited from Product (if not provided): minimumActivePeriod, minimumActivePeriodFrequencyType, lockinPeriodFrequency, lockinPeriodFrequencyType")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.PostAccountsTypeRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.PostAccountsTypeResponse.class))) })
    public String createAccount(@PathParam("type") @Parameter(description = "type") final String accountType,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createAccount(accountType).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve share application | Undo approval share application | Reject share application | Activate a share account | Close a share account | Apply additional shares on a share account | Approve additional shares request on a share account | Reject additional shares request on a share account | Redeem shares on a share account", description = "Approve share application:\n\n"
            + "Approves share application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Undo approval share application:\n\n"
            + "Will move 'approved' share application back to 'Submitted and pending approval' state.\n\n" + "Reject share application:\n\n"
            + "Rejects share application so long as its in 'Submitted and pending approval' state.\n\n" + "Activate a share account:\n\n"
            + "Results in an approved share application being converted into an 'active' share account.\n\n" + "Close a share account:\n\n"
            + "Results in an Activated share application being converted into an 'closed' share account.\n" + "\n"
            + "closedDate is closure date of share account\n\n" + "Mandatory Fields: dateFormat,locale,closedDate\n\n"
            + "Apply additional shares on a share account:\n\n" + "requestedDate is requsted date of share purchase\n" + "\n"
            + "requestedShares is number of shares to be purchase\n\n"
            + "Mandatory Fields: dateFormat,locale,requestedDate, requestedShares\n\n"
            + "Approve additional shares request on a share account\n\n" + "requestedShares is Share purchase transaction ids\n\n"
            + "Mandatory Fields: requestedShares\n\n" + "Reject additional shares request on a share account:\n\n"
            + "requestedShares is Share purchase transaction ids\n\n" + "Mandatory Fields: requestedShares\n\n"
            + "Redeem shares on a share account:\n\n" + "Results redeem some/all shares from share account.\n" + "\n"
            + "requestedDate is requsted date of shares redeem\n" + "\n" + "requestedShares is number of shares to be redeemed\n\n"
            + "Mandatory Fields: dateFormat,locale,requestedDate,requestedShares\n\n"
            + "Showing request/response for 'Reject additional shares request on a share account'\n\n"
            + "For more info visit this link - https://fineract.apache.org/legacy-docs/apiLive.htm#shareaccounts")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.PostAccountsTypeAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.PostAccountsTypeAccountIdResponse.class))) })
    public String handleCommands(@PathParam("type") @Parameter(description = "type") final String accountType,
            @PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createAccountCommand(accountType, accountId, commandParam)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a share application", description = "Share application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.PutAccountsTypeAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountsApiResourceSwagger.PutAccountsTypeAccountIdResponse.class))) })
    public String updateAccount(@PathParam("type") @Parameter(description = "type") final String accountType,
            @PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAccount(accountType, accountId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSharedAccountsTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat,
            @PathParam("type") @Parameter(description = "type") final String accountType) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SHARE_ACCOUNTS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload shared accounts template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postSharedAccountsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat,
            @PathParam("type") @Parameter(description = "type") final String accountType) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.SHARE_ACCOUNTS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
