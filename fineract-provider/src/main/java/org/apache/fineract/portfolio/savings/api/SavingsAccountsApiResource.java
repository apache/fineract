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
package org.apache.fineract.portfolio.savings.api;

import io.swagger.annotations.*;
import java.io.InputStream;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/savingsaccounts")
@Component
@Scope("singleton")
@Api(value = "Savings Account", description = "Savings accounts are instances of a particular savings product created for an individual or group. An application process around the creation of accounts is also supported.")
public class SavingsAccountsApiResource {

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @Autowired
    public SavingsAccountsApiResource(final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final PlatformSecurityContext context, final DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.savingsAccountChargeReadPlatformService = savingsAccountChargeReadPlatformService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Savings Account Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Requests:\n" + "\n" + "savingsaccounts/template?clientId=1\n" + "\n" + "\n" + "savingsaccounts/template?clientId=1&productId=1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.GetSavingsAccountsTemplateResponse.class)})
    public String template(@QueryParam("clientId") @ApiParam(value = "clientId") final Long clientId, @QueryParam("groupId") @ApiParam(value = "groupId") final Long groupId,
                           @QueryParam("productId") @ApiParam(value = "productId") final Long productId,
                           @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
                           @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final SavingsAccountData savingsAccount = this.savingsAccountReadPlatformService.retrieveTemplate(clientId, groupId, productId,
                staffInSelectedOfficeOnly);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, savingsAccount,
				SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List savings applications/accounts", httpMethod = "GET", notes = "Lists savings applications/accounts\n\n" + "Example Requests:\n" + "\n" + "savingsaccounts\n" + "\n" + "\n" + "savingsaccounts?fields=name")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.GetSavingsAccountsResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("externalId") @ApiParam(value = "externalId") final String externalId,
            // @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forSavings(sqlSearch, externalId, offset, limit, orderBy, sortOrder);

        final Page<SavingsAccountData> products = this.savingsAccountReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, products,
				SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Submit new savings application", httpMethod = "POST", notes = "Submits new savings application\n\n" + "Mandatory Fields: clientId or groupId, productId, submittedOnDate\n\n" + "Optional Fields: accountNo, externalId, fieldOfficerId\n\n" + "Inherited from Product (if not provided): nominalAnnualInterestRate, interestCompoundingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, allowOverdraft, overdraftLimit, withHoldTax\n\n" + "Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type Savings: datatables")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SavingsAccountsApiResourceSwagger.PostSavingsAccountsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.PostSavingsAccountsResponse.class)})
    public String submitApplication(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccount().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a savings application/account", httpMethod = "GET", notes = "Retrieves a savings application/account\n\n" + "Example Requests :\n" + "\n" + "savingsaccounts/1\n" + "\n" + "\n" + "savingsaccounts/1?associations=all")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.GetSavingsAccountsAccountIdResponse.class)})
    public String retrieveOne(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") @ApiParam(value = "chargeStatus") final String chargeStatus, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) { throw new UnrecognizedQueryParamException(
                "status", chargeStatus, new Object[] { "all", "active", "inactive" }); }

        final SavingsAccountData savingsAccount = this.savingsAccountReadPlatformService.retrieveOne(accountId);

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final SavingsAccountData savingsAccountTemplate = populateTemplateAndAssociations(accountId, savingsAccount,
                staffInSelectedOfficeOnly, chargeStatus, uriInfo, mandatoryResponseParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, savingsAccountTemplate,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    private SavingsAccountData populateTemplateAndAssociations(final Long accountId, final SavingsAccountData savingsAccount,
            final boolean staffInSelectedOfficeOnly, final String chargeStatus, final UriInfo uriInfo,
            final Set<String> mandatoryResponseParameters) {

        Collection<SavingsAccountTransactionData> transactions = null;
        Collection<SavingsAccountChargeData> charges = null;

        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList(SavingsApiConstants.transactions, SavingsApiConstants.charges));
            }

            if (associationParameters.contains(SavingsApiConstants.transactions)) {
                mandatoryResponseParameters.add(SavingsApiConstants.transactions);
                final Collection<SavingsAccountTransactionData> currentTransactions = this.savingsAccountReadPlatformService
                        .retrieveAllTransactions(accountId, DepositAccountType.SAVINGS_DEPOSIT);
                if (!CollectionUtils.isEmpty(currentTransactions)) {
                    transactions = currentTransactions;
                }
            }

            if (associationParameters.contains(SavingsApiConstants.charges)) {
                mandatoryResponseParameters.add(SavingsApiConstants.charges);
                final Collection<SavingsAccountChargeData> currentCharges = this.savingsAccountChargeReadPlatformService
                        .retrieveSavingsAccountCharges(accountId, chargeStatus);
                if (!CollectionUtils.isEmpty(currentCharges)) {
                    charges = currentCharges;
                }
            }
        }

        SavingsAccountData templateData = null;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            templateData = this.savingsAccountReadPlatformService.retrieveTemplate(savingsAccount.clientId(), savingsAccount.groupId(),
                    savingsAccount.productId(), staffInSelectedOfficeOnly);
        }

        return SavingsAccountData.withTemplateOptions(savingsAccount, templateData, transactions, charges);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Modify a savings application | Modify savings account withhold tax applicability", httpMethod = "PUT", notes = "Modify a savings application:\n\n" + "Savings application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc\n\n" + "Modify savings account withhold tax applicability:\n\n"+ "Savings application's withhold tax can be modified when in 'Active' state. Once the application is activated, can modify the account withhold tax to post tax or vice-versa" +"Showing request/response for 'Modify a savings application'")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SavingsAccountsApiResourceSwagger.PutSavingsAccountsAccountIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.PutSavingsAccountsAccountIdResponse.class)})
    public String update(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @ApiParam(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("command") @ApiParam(value = "command") final String commandParam) {

        if (is(commandParam, "updateWithHoldTax")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).updateWithHoldTax(accountId)
                    .build();
            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingsAccount(accountId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Approve savings application | Undo approval savings application | Assign Savings Officer | Unassign Savings Officer | Reject savings application | Withdraw savings application | Activate a savings account | Close a savings account | Calculate Interest on Savings Account | Post Interest on Savings Account | Block Savings Account | Unblock Savings Account | Block Savings Account Credit transactions | Unblock Savings Account Credit transactions | Block Savings Account Debit transactions | Unblock Savings Account debit transactions", httpMethod = "POST", notes = "Approve savings application:\n\n" + "Approves savings application so long as its in 'Submitted and pending approval' state.\n\n" + "Undo approval savings application:\n\n" + "Will move 'approved' savings application back to 'Submitted and pending approval' state.\n\n" + "Assign Savings Officer:\n\n" + "Allows you to assign Savings Officer for existing Savings Account.\n\n" + "Unassign Savings Officer:\n\n" + "Allows you to unassign the Savings Officer.\n\n" + "Reject savings application:\n\n" + "Rejects savings application so long as its in 'Submitted and pending approval' state.\n\n" + "Withdraw savings application:\n\n" + "Used when an applicant withdraws from the savings application. It must be in 'Submitted and pending approval' state.\n\n" + "Activate a savings account:\n\n" + "Results in an approved savings application being converted into an 'active' savings account.\n\n" + "Close a savings account:\n\n" + "Results in an Activated savings application being converted into an 'closed' savings account.\n" + "\n" + "closedOnDate is closure date of savings account\n" + "\n" + "withdrawBalance is a boolean value, true value of this field performs a withdrawal transaction with account's running balance.\n\n" + "Mandatory Fields: dateFormat,locale,closedOnDate\n\n" + "Optional Fields: note, withdrawBalance, paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber\n\n" + "Calculate Interest on Savings Account:\n\n" + "Calculates interest earned on a savings account based on todays date. It does not attempt to post or credit the interest on the account. That is responsibility of the Post Interest API that will likely be called by overnight process.\n\n" + "Post Interest on Savings Account:\n\n" + "Calculates and Posts interest earned on a savings account based on today's date and whether an interest posting or crediting event is due.\n\n" + "Block Savings Account:\n\n" + "Blocks Savings account from all types of credit and debit transactions\n\n" + "Unblock Savings Account:\n\n" + "Unblock a blocked account. On unblocking account, user can perform debit and credit transactions\n\n" + "Block Savings Account Credit transactions:\n\n" + "Savings account will be blocked from all types of credit transactions.\n\n" + "Unblock Savings Account Credit transactions:\n\n" + "It unblocks the Saving account's credit operations. Now all types of credits can be transacted to Savings account\n\n" + "Block Savings Account Debit transactions:\n\n" + "All types of debit operations from Savings account wil be blocked\n\n" + "Unblock Savings Account debit transactions:\n\n" + "It unblocks the Saving account's debit operations. Now all types of debits can be transacted from Savings account\n\n" + "Showing request/response for 'Unassign Savings Officer'")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SavingsAccountsApiResourceSwagger.PostSavingsAccountsAccountIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.PostSavingsAccountsAccountIdResponse.class)})
    public String handleCommands(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
           @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectSavingsAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawSavingsAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveSavingsAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoSavingsAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.savingsAccountActivation(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculateInterest")) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().savingsAccountInterestCalculation(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "postInterest")) {
            final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "applyAnnualFees")) {
            final CommandWrapper commandRequest = builder.savingsAccountApplyAnnualFees(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeSavingsAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "assignSavingsOfficer")) {
            final CommandWrapper commandRequest = builder.assignSavingsOfficer(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignSavingsOfficer")) {
            final CommandWrapper commandRequest = builder.unassignSavingsOfficer(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_BLOCK_DEBIT)) {
            final CommandWrapper commandRequest = builder.blockDebitsFromSavingsAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_UNBLOCK_DEBIT)) {
            final CommandWrapper commandRequest = builder.unblockDebitsFromSavingsAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_BLOCK_CREDIT)) {
            final CommandWrapper commandRequest = builder.blockCreditsToSavingsAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_UNBLOCK_CREDIT)) {
            final CommandWrapper commandRequest = builder.unblockCreditsToSavingsAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_BLOCK_ACCOUNT)) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().blockSavingsAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_UNBLOCK_ACCOUNT)) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().unblockSavingsAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "reject", "withdrawnByApplicant", "approve",
                    "undoapproval", "activate", "calculateInterest", "postInterest", "close", "assignSavingsOfficer",
                    "unassignSavingsOfficer", SavingsApiConstants.COMMAND_BLOCK_DEBIT, SavingsApiConstants.COMMAND_UNBLOCK_DEBIT,
                    SavingsApiConstants.COMMAND_BLOCK_CREDIT, SavingsApiConstants.COMMAND_UNBLOCK_CREDIT,
                    SavingsApiConstants.COMMAND_BLOCK_ACCOUNT, SavingsApiConstants.COMMAND_UNBLOCK_ACCOUNT });
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a savings application", httpMethod = "DELETE", notes = "At present we support hard delete of savings application so long as its in 'Submitted and pending approval' state. One the application is moves past this state, it is not possible to do a 'hard' delete of the application or the account. An API endpoint will be added to close/de-activate the savings account.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountsApiResourceSwagger.DeleteSavingsAccountsAccountIdResponse.class)})
    public String delete(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingsAccount(accountId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSavingsTemplate(@QueryParam("officeId")final Long officeId,
            @QueryParam("staffId")final Long staffId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SAVINGS_ACCOUNT.toString(),officeId, staffId,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postSavingsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.SAVINGS_ACCOUNT.toString(), uploadedInputStream,
                fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("transactions/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSavingsTransactionTemplate(@QueryParam("officeId")final Long officeId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SAVINGS_TRANSACTIONS.toString(),officeId, null,dateFormat);
    }

    @POST
    @Path("transactions/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postSavingsTransactionTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.SAVINGS_TRANSACTIONS.toString(), uploadedInputStream,
                fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}