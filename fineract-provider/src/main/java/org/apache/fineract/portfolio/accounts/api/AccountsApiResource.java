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

import io.swagger.annotations.*;
import javax.ws.rs.Consumes;
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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.accounts.constants.AccountsApiConstants;
import org.apache.fineract.portfolio.accounts.constants.ShareAccountApiConstants;
import org.apache.fineract.portfolio.accounts.data.AccountData;
import org.apache.fineract.portfolio.accounts.service.AccountReadPlatformService;
import org.apache.fineract.portfolio.products.exception.ResourceNotFoundException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;


@Path("/accounts/{type}")
@Component
@Scope("singleton")
@Api(value = "Share Account", description = "Share accounts are instances of a praticular share product created for an individual. An application process around the creation of accounts is also supported.")
public class AccountsApiResource {

    private final ApplicationContext applicationContext ;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    
    @Autowired
    public AccountsApiResource(final ApplicationContext applicationContext,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer,
            final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.applicationContext = applicationContext ;
        this.apiRequestParameterHelper = apiRequestParameterHelper ;
        this.toApiJsonSerializer = toApiJsonSerializer ;
        this.platformSecurityContext = platformSecurityContext ; 
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService ;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }
    
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Share Account Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Requests:\n" + "\n" + "accounts/share/template?clientId=1\n" + "\n" + "\n" + "accounts/share/template?clientId=1&productId=1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = AccountsApiResourceSwagger.GetAccountsTypeTemplateResponse.class)})
    public String template(@PathParam("type") @ApiParam(value = "type") final String accountType, @QueryParam("clientId") @ApiParam(value = "clientId") final Long clientId,
                           @QueryParam("productId") @ApiParam(value = "productId") final Long productId,
                           @Context final UriInfo uriInfo) {
        try {
            this.platformSecurityContext.authenticatedUser() ;
            String serviceName = accountType+AccountsApiConstants.READPLATFORM_NAME ;
            AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName) ;
            final AccountData accountData = service.retrieveTemplate(clientId, productId);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, accountData, service.getResponseDataParams());    
        }catch(BeansException e) {
            throw new ResourceNotFoundException();
        }
    }
    
    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a share application/account", httpMethod = "GET", notes = "Retrieves a share application/account\n\n" + "Example Requests :\n" + "\n" + "shareaccount/1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = AccountsApiResourceSwagger.GetAccountsTypeAccountIdResponse.class)})
    public String retrieveAccount(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @PathParam("type") @ApiParam(value = "type") final String accountType,
            @Context final UriInfo uriInfo) {
        try {
            String serviceName = accountType+AccountsApiConstants.READPLATFORM_NAME ;
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName) ;
            AccountData data = service.retrieveOne(accountId, settings.isTemplate()) ;
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());    
        }catch(BeansException e) {
            throw new ResourceNotFoundException();
        }
    }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List share applications/accounts", httpMethod = "GET", notes = "Lists share applications/accounts\n\n" + "Example Requests:\n" + "\n" + "shareaccount")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = AccountsApiResourceSwagger.GetAccountsTypeResponse.class)})
    public String retrieveAllAccounts(@PathParam("type") @ApiParam(value = "type") final String accountType, @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit, @Context final UriInfo uriInfo) {
        try {
            String serviceName = accountType+AccountsApiConstants.READPLATFORM_NAME ;
            AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName) ;
            Page<AccountData> data = service.retrieveAll(offset, limit) ;
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());    
        }catch(BeansException e) {
            throw new ResourceNotFoundException();
        }
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Submit new share application", httpMethod = "POST", notes = "Submits new share application\n\n" + "Mandatory Fields: clientId, productId, submittedDate, savingsAccountId, requestedShares, applicationDate\n\n" + "Optional Fields: accountNo, externalId\n\n" + "Inherited from Product (if not provided): minimumActivePeriod, minimumActivePeriodFrequencyType, lockinPeriodFrequency, lockinPeriodFrequencyType")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = AccountsApiResourceSwagger.PostAccountsTypeRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = AccountsApiResourceSwagger.PostAccountsTypeResponse.class)})
    public String createAccount(@PathParam("type") @ApiParam(value = "type") final String accountType, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
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
    @ApiOperation(value = "Approve share application | Undo approval share application | Reject share application | Activate a share account | Close a share account | Apply additional shares on a share account | Approve additional shares request on a share account | Reject additional shares request on a share account | Redeem shares on a share account", httpMethod = "POST", notes = "Approve share application:\n\n" + "Approves share application so long as its in 'Submitted and pending approval' state.\n\n" + "Undo approval share application:\n\n" + "Will move 'approved' share application back to 'Submitted and pending approval' state.\n\n" + "Reject share application:\n\n" + "Rejects share application so long as its in 'Submitted and pending approval' state.\n\n" + "Activate a share account:\n\n" + "Results in an approved share application being converted into an 'active' share account.\n\n" + "Close a share account:\n\n" + "Results in an Activated share application being converted into an 'closed' share account.\n" + "\n" + "closedDate is closure date of share account\n\n" + "Mandatory Fields: dateFormat,locale,closedDate\n\n" + "Apply additional shares on a share account:\n\n" + "requestedDate is requsted date of share purchase\n" + "\n" + "requestedShares is number of shares to be purchase\n\n" + "Mandatory Fields: dateFormat,locale,requestedDate, requestedShares\n\n" + "Approve additional shares request on a share account\n\n" + "requestedShares is Share purchase transaction ids\n\n" + "Mandatory Fields: requestedShares\n\n" + "Reject additional shares request on a share account:\n\n" + "requestedShares is Share purchase transaction ids\n\n" + "Mandatory Fields: requestedShares\n\n" + "Redeem shares on a share account:\n\n" + "Results redeem some/all shares from share account.\n" + "\n" + "requestedDate is requsted date of shares redeem\n" + "\n" + "requestedShares is number of shares to be redeemed\n\n" + "Mandatory Fields: dateFormat,locale,requestedDate,requestedShares\n\n" + "Showing request/response for 'Reject additional shares request on a share account'\n\n" + "For more info visit this link - https://demo.openmf.org/api-docs/apiLive.htm#shareaccounts")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = AccountsApiResourceSwagger.PostAccountsTypeAccountIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = AccountsApiResourceSwagger.PostAccountsTypeAccountIdResponse.class)})
    public String handleCommands(@PathParam("type") @ApiParam(value = "type") final String accountType, @PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createAccountCommand(accountType, accountId, commandParam).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Modify a share application", httpMethod = "PUT", notes = "Share application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = AccountsApiResourceSwagger.PutAccountsTypeAccountIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = AccountsApiResourceSwagger.PutAccountsTypeAccountIdResponse.class)})
    public String updateAccount(@PathParam("type") @ApiParam(value = "type") final String accountType, @PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {
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
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SHARE_ACCOUNTS.toString(),officeId, null,dateFormat);
    }
    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postSharedAccountsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.SHARE_ACCOUNTS.toString(), uploadedInputStream,
                fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
