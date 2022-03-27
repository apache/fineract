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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.apache.commons.lang3.StringUtils;
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
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.RecurringDepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.DepositAccountPreMatureCalculationPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/recurringdepositaccounts")
@Component
@Scope("singleton")
@Tag(name = "Recurring Deposit Account", description = "Recurring Deposit accounts are instances of a praticular recurring deposit product created. An application process around the creation of accounts is also supported.")
public class RecurringDepositAccountsApiResource {

    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<DepositAccountData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final DepositAccountPreMatureCalculationPlatformService accountPreMatureCalculationPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @Autowired
    public RecurringDepositAccountsApiResource(final DepositAccountReadPlatformService depositAccountReadPlatformService,
            final PlatformSecurityContext context, final DefaultToApiJsonSerializer<DepositAccountData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService, final FromJsonHelper fromJsonHelper,
            final DepositAccountPreMatureCalculationPlatformService accountPreMatureCalculationPlatformService,
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
        this.bulkImportWorkbookService = bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService = bulkImportWorkbookPopulatorService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve recurring Deposit Account Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for recurring deposit applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Requests:\n" + "\n"
            + "recurringdepositaccounts/template?clientId=1\n" + "\n" + "\n" + "recurringdepositaccounts/template?clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.GetRecurringDepositAccountsTemplateResponse.class))) })
    public String template(@QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        final DepositAccountData account = this.depositAccountReadPlatformService.retrieveTemplate(DepositAccountType.RECURRING_DEPOSIT,
                clientId, groupId, productId, staffInSelectedOfficeOnly);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, account,
                DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Recurring deposit applications/accounts", description = "Lists Recurring deposit applications/accounts\n\n"
            + "Example Requests:\n" + "\n" + "recurringdepositaccounts\n" + "\n" + "\n" + "recurringdepositaccounts?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.GetRecurringDepositAccountsResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("paged") @Parameter(description = "paged") final Boolean paged,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        final PaginationParameters paginationParameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (paginationParameters.isPaged()) {
            final Page<DepositAccountData> account = this.depositAccountReadPlatformService
                    .retrieveAllPaged(DepositAccountType.RECURRING_DEPOSIT, paginationParameters);
            return this.toApiJsonSerializer.serialize(settings, account,
                    DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<DepositAccountData> account = this.depositAccountReadPlatformService
                .retrieveAll(DepositAccountType.RECURRING_DEPOSIT, paginationParameters);

        return this.toApiJsonSerializer.serialize(settings, account,
                DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Submit new recurring deposit application", description = "Submits new recurring deposit application\n\n"
            + "Mandatory Fields: clientId or groupId, productId, submittedOnDate, depositAmount, depositPeriod, depositPeriodFrequencyId\n\n"
            + "Optional Fields: accountNo, externalId, fieldOfficerId,linkAccountId(if provided initial deposit amount will be collected from this account),transferInterestToSavings(By enabling this flag all interest postings will be transferred to linked saving account )\n\n"
            + "Inherited from Product (if not provided): interestCompoundingPeriodType, interestCalculationType, interestCalculationDaysInYearType, lockinPeriodFrequency, lockinPeriodFrequencyType, preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestOnTypeId, charts, withHoldTax")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.PostRecurringDepositAccountsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.PostRecurringDepositAccountsResponse.class))) })
    public String submitApplication(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createRecurringDepositAccount().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a recurring deposit application/account", description = "Retrieves a recurring deposit application/account\n\n"
            + "Example Requests :\n" + "\n" + "recurringdepositaccounts/1\n" + "\n" + "\n" + "recurringdepositaccounts/1?associations=all")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.GetRecurringDepositAccountsAccountIdResponse.class))) })
    public String retrieveOne(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) {
            throw new UnrecognizedQueryParamException("status", chargeStatus, new Object[] { "all", "active", "inactive" });
        }

        final RecurringDepositAccountData account = (RecurringDepositAccountData) this.depositAccountReadPlatformService
                .retrieveOneWithChartSlabs(DepositAccountType.RECURRING_DEPOSIT, accountId);

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final RecurringDepositAccountData accountTemplate = populateTemplateAndAssociations(accountId, account, staffInSelectedOfficeOnly,
                chargeStatus, uriInfo, mandatoryResponseParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, accountTemplate,
                DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    private RecurringDepositAccountData populateTemplateAndAssociations(final Long accountId,
            final RecurringDepositAccountData savingsAccount, final boolean staffInSelectedOfficeOnly, final String chargeStatus,
            final UriInfo uriInfo, final Set<String> mandatoryResponseParameters) {

        Collection<SavingsAccountTransactionData> transactions = null;
        Collection<SavingsAccountChargeData> charges = null;

        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList(SavingsApiConstants.transactions, SavingsApiConstants.charges));
            }

            if (associationParameters.contains(SavingsApiConstants.transactions)) {
                mandatoryResponseParameters.add(SavingsApiConstants.transactions);
                final Collection<SavingsAccountTransactionData> currentTransactions = this.depositAccountReadPlatformService
                        .retrieveAllTransactions(DepositAccountType.RECURRING_DEPOSIT, accountId);
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

        RecurringDepositAccountData templateData = null;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            templateData = (RecurringDepositAccountData) this.depositAccountReadPlatformService.retrieveTemplate(
                    DepositAccountType.RECURRING_DEPOSIT, savingsAccount.clientId(), savingsAccount.groupId(), savingsAccount.productId(),
                    staffInSelectedOfficeOnly);
        }

        return RecurringDepositAccountData.withTemplateOptions(savingsAccount, templateData, transactions, charges);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a recurring deposit application", description = "Recurring deposit application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.PutRecurringDepositAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.PutRecurringDepositAccountsAccountIdResponse.class))) })
    public String update(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRecurringDepositAccount(accountId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve recurring deposit application | Undo approval recurring deposit application | Reject recurring deposit application | Withdraw recurring deposit application | Activate a recurring deposit account | Update the recommended deposit amount for a recurring deposit account | Close a recurring deposit account | Premature Close a recurring deposit account | Calculate Premature amount on Recurring deposit account | Calculate Interest on recurring Deposit Account | Post Interest on recurring Deposit Account", description = "Approve recurring deposit application:\n\n"
            + "Approves recurring deposit application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Undo approval recurring deposit application:\n\n"
            + "Will move 'approved' recurring deposit application back to 'Submitted and pending approval' state.\n\n"
            + "Reject recurring deposit application\n\n"
            + "Rejects recurring deposit application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Withdraw recurring deposit application:\n\n"
            + "Used when an applicant withdraws from the recurring deposit application. It must be in 'Submitted and pending approval' state.\n\n"
            + "Activate a recurring deposit account:\n\n"
            + "Results in an approved recurring deposit application being converted into an 'active' recurring deposit account.\n\n"
            + "Update the recommended deposit amount for a recurring deposit account:\n\n"
            + "Updates the recommended deposit amount for a RD account as on the effective date.\n\n"
            + "Close a recurring deposit account\n\n"
            + "Results in a Matured recurring deposit account being converted into a 'closed' recurring deposit account.\n" + "\n"
            + "On account close allowed actions are." + "Premature Close a recurring deposit account:\n\n"
            + "Results in an Active recurring deposit account being converted into a 'Premature Closed' recurring deposit account with options to withdraw prematured amount. (premature amount is calculated using interest rate chart applicable along with penal interest if any.)\n"
            + "\n" + "On account premature closure allowed actions are.\n\n"
            + "Calculate Premature amount on Recurring deposit account:\n\n"
            + "Calculate premature amount on recurring deposit till premature close date. Premature amount is calculated based on interest chart and penal interest applicable if any.\n\n"
            + "Calculate Interest on recurring Deposit Account:\n\n"
            + "Calculates interest earned on a recurring deposit account based on todays date. It does not attempt to post or credit the interest on the account. That is responsibility of the Post Interest API that will likely be called by overnight process.\n\n"
            + "Post Interest on recurring Deposit Account:\n\n"
            + "Calculates and Posts interest earned on a recurring deposit account based on todays date and whether an interest posting or crediting event is due.\n\n"
            + "Showing request/response for 'Post Interest on recurring Deposit Account'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.PostRecurringDepositAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.PostRecurringDepositAccountsAccountIdResponse.class))) })
    public String handleCommands(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectRecurringDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawRecurringDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveRecurringDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoRecurringDepositAccountApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.recurringDepositAccountActivation(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculateInterest")) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().recurringDepositAccountInterestCalculation(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, DepositsApiConstants.UPDATE_DEPOSIT_AMOUNT)) {
            final CommandWrapper commandRequest = builder.updateDepositAmountForRecurringDepositAccount(accountId)
                    .withJson(apiRequestBodyAsJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "postInterest")) {
            final CommandWrapper commandRequest = builder.recurringDepositAccountInterestPosting(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeRecurringDepositAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "prematureClose")) {
            final CommandWrapper commandRequest = builder.prematureCloseRecurringDepositAccount(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculatePrematureAmount")) {
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final DepositAccountData account = this.accountPreMatureCalculationPlatformService.calculatePreMatureAmount(accountId, query,
                    DepositAccountType.RECURRING_DEPOSIT);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, account,
                    DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[] { "reject", "withdrawnByApplicant", "approve", "undoapproval", "activate", "calculateInterest",
                            "postInterest", "close", "prematureClose", "calculatePrematureAmount" });
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
    @Operation(summary = "Delete a recurring deposit application", description = "At present we support hard delete of recurring deposit application so long as its in 'Submitted and pending approval' state. One the application is moves past this state, it is not possible to do a 'hard' delete of the application or the account. An API endpoint will be added to close/de-activate the recurring deposit account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountsApiResourceSwagger.DeleteRecurringDepositAccountsResponse.class))) })
    public String delete(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteRecurringDepositAccount(accountId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String accountClosureTemplate(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        DepositAccountData account = null;
        if (is(commandParam, "close")) {
            account = this.depositAccountReadPlatformService.retrieveOneWithClosureTemplate(DepositAccountType.RECURRING_DEPOSIT,
                    accountId);

        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, account,
                DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getRecurringDepositTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS.toString(), officeId, staffId,
                dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload recurring deposit template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postRecurringDepositTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("transactions/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getRecurringDepositTransactionTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS.toString(), officeId,
                null, dateFormat);
    }

    @POST
    @Path("transactions/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload recurring deposit transactions template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postRecurringDepositTransactionsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(
                GlobalEntityType.RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS.toString(), uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
