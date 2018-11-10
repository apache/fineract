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

import com.google.gson.JsonElement;
import io.swagger.annotations.*;
import java.io.InputStream;
import java.math.BigDecimal;
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
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.FixedDepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.DepositAccountPreMatureCalculationPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Path("/fixeddepositaccounts")
@Component
@Scope("singleton")
@Api(value = "Fixed Deposit Account", description = "Fixed Deposit accounts are instances of a praticular fixed deposit product created. An application process around the creation of accounts is also supported.")
public class FixedDepositAccountsApiResource {

    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<DepositAccountData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final DepositAccountPreMatureCalculationPlatformService accountPreMatureCalculationPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;


    @Autowired
    public FixedDepositAccountsApiResource(final DepositAccountReadPlatformService depositAccountReadPlatformService,
            final PlatformSecurityContext context, final DefaultToApiJsonSerializer<DepositAccountData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService, final FromJsonHelper fromJsonHelper,
            final DepositAccountPreMatureCalculationPlatformService accountPreMatureCalculationPlatformService,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.depositAccountReadPlatformService = depositAccountReadPlatformService;
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.savingsAccountChargeReadPlatformService = savingsAccountChargeReadPlatformService;
        this.fromJsonHelper = fromJsonHelper;
        this.accountPreMatureCalculationPlatformService = accountPreMatureCalculationPlatformService;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Fixed Deposit Account Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for fixed deposit applications. The template data returned consists of any or all of:\n\n" + "\n\n" + "Field Defaults\n\n" + "Allowed Value Lists" + "Example Requests:\n\n" + "\n\n" + "fixeddepositaccounts/template?clientId=1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.GetFixedDepositAccountsTemplateResponse.class)})
    public String template(@QueryParam("clientId") @ApiParam(value = "clientId") final Long clientId, @QueryParam("groupId") @ApiParam(value = "groupId") final Long groupId,
                           @QueryParam("productId")@ApiParam(value = "productId") final Long productId,
                           @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
                           @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        final DepositAccountData account = this.depositAccountReadPlatformService.retrieveTemplate(DepositAccountType.FIXED_DEPOSIT,
                clientId, groupId, productId, staffInSelectedOfficeOnly);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, account, DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Fixed deposit applications/accounts", httpMethod = "GET", notes = "Lists Fixed Deposit Accounts\n\n" + "Example Requests:\n\n" + "\n\n" + "fixeddepositaccounts\n\n" + "\n\n" + "fixeddepositaccounts?fields=name")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.GetFixedDepositAccountsResponse.class, responseContainer = "List")})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("paged") @ApiParam(value = "paged") final Boolean paged,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        final PaginationParameters paginationParameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (paginationParameters.isPaged()) {
            final Page<DepositAccountData> account = this.depositAccountReadPlatformService.retrieveAllPaged(
                    DepositAccountType.FIXED_DEPOSIT, paginationParameters);
            return this.toApiJsonSerializer.serialize(settings, account,
                    DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<DepositAccountData> account = this.depositAccountReadPlatformService.retrieveAll(DepositAccountType.FIXED_DEPOSIT,
                paginationParameters);

        return this.toApiJsonSerializer.serialize(settings, account, DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Submit new fixed deposit application", httpMethod = "POST", notes = "Submits a new fixed deposit application"+"Mandatory Fields: clientId or groupId, productId, submittedOnDate, depositAmount, depositPeriod, depositPeriodFrequencyId\n\n" + "Optional Fields: accountNo, externalId, fieldOfficerId,linkAccountId(if provided initial deposit amount will be collected from this account),transferInterestToSavings(By enabling this flag all interest postings will be transferred to linked saving account )")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsResponse.class)})
    public String submitApplication(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createFixedDepositAccount().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a fixed deposit application/account", httpMethod = "GET", notes = "Retrieves a fixed deposit application/account\n\n" + "Example Requests :\n\n" + "\n\n" + "fixeddepositaccounts/1" + "\n\n" + "fixeddepositaccounts/1?associations=all" )
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.GetFixedDepositAccountsAccountIdResponse.class)})
    public String retrieveOne(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus")@ApiParam(value = "chargeStatus") final String chargeStatus, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) { throw new UnrecognizedQueryParamException(
                "status", chargeStatus, new Object[] { "all", "active", "inactive" }); }

        final FixedDepositAccountData account = (FixedDepositAccountData) this.depositAccountReadPlatformService.retrieveOneWithChartSlabs(
                DepositAccountType.FIXED_DEPOSIT, accountId);
        
        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final FixedDepositAccountData accountTemplate = populateTemplateAndAssociations(accountId, account, staffInSelectedOfficeOnly,
                chargeStatus, uriInfo, mandatoryResponseParameters);
        accountTemplate.setActivationCharge(getActivationCharge(accountId));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, accountTemplate,
                DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }
    
    private BigDecimal getActivationCharge(Long accountId){
        BigDecimal activationCharge = BigDecimal.ZERO;
        Collection<SavingsAccountChargeData> savingCharges = this.savingsAccountChargeReadPlatformService.retrieveSavingsAccountCharges(accountId, "active");
        for (SavingsAccountChargeData savingsAccountChargeData : savingCharges) {
            if(savingsAccountChargeData.isSavingsActivation()){
                activationCharge = activationCharge.add(savingsAccountChargeData.getAmountOutstanding());
            }
        }
        return activationCharge;
    }

    private FixedDepositAccountData populateTemplateAndAssociations(final Long accountId, final FixedDepositAccountData savingsAccount,
            final boolean staffInSelectedOfficeOnly, final String chargeStatus, final UriInfo uriInfo,
            final Set<String> mandatoryResponseParameters) {

        Collection<SavingsAccountTransactionData> transactions = null;
        Collection<SavingsAccountChargeData> charges = null;
        PortfolioAccountData linkedAccount = null;

        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList(SavingsApiConstants.transactions, SavingsApiConstants.charges,
                        SavingsApiConstants.linkedAccount));
            }

            if (associationParameters.contains(SavingsApiConstants.transactions)) {
                mandatoryResponseParameters.add(SavingsApiConstants.transactions);
                final Collection<SavingsAccountTransactionData> currentTransactions = this.depositAccountReadPlatformService
                        .retrieveAllTransactions(DepositAccountType.FIXED_DEPOSIT, accountId);
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

            if (associationParameters.contains(SavingsApiConstants.linkedAccount)) {
                mandatoryResponseParameters.add(SavingsApiConstants.linkedAccount);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveSavingsLinkedAssociation(accountId);
            }
        }

        FixedDepositAccountData templateData = null;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            templateData = (FixedDepositAccountData) this.depositAccountReadPlatformService.retrieveTemplate(
                    DepositAccountType.FIXED_DEPOSIT, savingsAccount.clientId(), savingsAccount.groupId(), savingsAccount.productId(),
                    staffInSelectedOfficeOnly);
        }

        return FixedDepositAccountData.associationsAndTemplate(savingsAccount, templateData, transactions, charges, linkedAccount);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Modify a fixed deposit application", httpMethod = "PUT", notes = "Fixed deposit application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = FixedDepositAccountsApiResourceSwagger.PutFixedDepositAccountsAccountIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.PutFixedDepositAccountsAccountIdResponse.class)})
    public String update(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFixedDepositAccount(accountId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Approve fixed deposit application | Undo approval fixed deposit application | Reject fixed deposit application | Withdraw fixed deposit application | Activate a fixed deposit account | Close a fixed deposit account | Premature Close a fixed deposit account | Calculate Premature amount on Fixed deposit account | Calculate Interest on Fixed Deposit Account | Post Interest on Fixed Deposit Account", httpMethod = "POST", notes = "Approve fixed deposit application:\n\n" + "Approves fixed deposit application so long as its in 'Submitted and pending approval' state.\n\n" + "Undo approval fixed deposit application:\n\n" + "Will move 'approved' fixed deposit application back to 'Submitted and pending approval' state.\n\n" + "Reject fixed deposit application:\n\n" + "Rejects fixed deposit application so long as its in 'Submitted and pending approval' state.\n\n" + "Withdraw fixed deposit application:\n\n" + "Used when an applicant withdraws from the fixed deposit application. It must be in 'Submitted and pending approval' state.\n\n" + "Close a fixed deposit account:\n\n" + "Results in a Matured fixed deposit account being converted into a 'closed' fixed deposit account.\n\n" + "Premature Close a fixed deposit account:\n\n" + "Results in an Active fixed deposit account being converted into a 'Premature Closed' fixed deposit account with options to withdraw prematured amount. (premature amount is calculated using interest rate chart applicable along with penal interest if any.)\n\n" + "Calculate Premature amount on Fixed deposit account:\n\n" + "Calculate premature amount on fixed deposit account till premature close date. Premature amount is calculated based on interest chart and penal interest applicable.\n\n" + "Calculate Interest on Fixed Deposit Account:\n\n" + "Calculates interest earned on a fixed deposit account based on todays date. It does not attempt to post or credit the interest on the account. That is responsibility of the Post Interest API that will likely be called by overnight process.\n\n" + "Post Interest on Fixed Deposit Account:\n\n" + "Calculates and Posts interest earned on a fixed deposit account based on today's date and whether an interest posting or crediting event is due.\n\n" + "Showing request/response for Calculate Interest on Fixed Deposit Account")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsAccountIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsAccountIdResponse.class)})
    public String handleCommands(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @Context final UriInfo uriInfo, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectFixedDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawFixedDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveFixedDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoFixedDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.fixedDepositAccountActivation(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculateInterest")) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().fixedDepositAccountInterestCalculation(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "postInterest")) {
            final CommandWrapper commandRequest = builder.fixedDepositAccountInterestPosting(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeFixedDepositAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "prematureClose")) {
            final CommandWrapper commandRequest = builder.prematureCloseFixedDepositAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculatePrematureAmount")) {
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final DepositAccountData account = this.accountPreMatureCalculationPlatformService.calculatePreMatureAmount(accountId, query,
                    DepositAccountType.FIXED_DEPOSIT);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, account,
                    DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
        }

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "reject",
                "withdrawnByApplicant", "approve", "undoapproval", "activate", "calculateInterest", "postInterest", "close",
                "prematureClose", "calculatePrematureAmount" }); }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a fixed deposit application", httpMethod = "DELETE", notes = "At present we support hard delete of fixed deposit application so long as its in 'Submitted and pending approval' state. One the application is moves past this state, it is not possible to do a 'hard' delete of the application or the account. An API endpoint will be added to close/de-activate the fixed deposit account.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = FixedDepositAccountsApiResourceSwagger.DeleteFixedDepositAccountsAccountIdResponse.class)})
    public String delete(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteFixedDepositAccount(accountId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String accountClosureTemplate(@PathParam("accountId") @ApiParam(value = "accountId") final Long accountId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        DepositAccountData account = null;
        if (is(commandParam, "close")) {
            account = this.depositAccountReadPlatformService.retrieveOneWithClosureTemplate(DepositAccountType.FIXED_DEPOSIT, accountId);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, account, DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getFixedDepositTemplate(@QueryParam("officeId")final Long officeId,
            @QueryParam("staffId")final Long staffId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate
                (GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS.toString(),officeId,staffId,dateFormat);
    }
    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postFixedDepositTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,@FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS.toString(),
                uploadedInputStream,fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("transaction/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getFixedDepositTransactionTemplate(@QueryParam("officeId")final Long officeId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS.toString(), officeId,null,dateFormat);
    }

    @POST
    @Path("transaction/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postFixedDepositTransactionTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,@FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS.toString(),
                uploadedInputStream,fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
