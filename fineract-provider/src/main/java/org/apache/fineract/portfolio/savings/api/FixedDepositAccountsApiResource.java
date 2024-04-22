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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
import org.apache.fineract.infrastructure.security.service.SqlValidator;
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
import org.apache.fineract.portfolio.savings.service.FixedDepositAccountInterestCalculationService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/v1/fixeddepositaccounts")
@Component
@Tag(name = "Fixed Deposit Account", description = "Fixed Deposit accounts are instances of a praticular fixed deposit product created. An application process around the creation of accounts is also supported.")
@RequiredArgsConstructor
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
    private final FixedDepositAccountInterestCalculationService fixedDepositAccountInterestCalculationService;
    private final SqlValidator sqlValidator;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Fixed Deposit Account Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for fixed deposit applications. The template data returned consists of any or all of:\n\n"
            + "\n\n" + "Field Defaults\n\n" + "Allowed Value Lists" + "Example Requests:\n\n" + "\n\n"
            + "fixeddepositaccounts/template?clientId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.GetFixedDepositAccountsTemplateResponse.class))) })
    public String template(@QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
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
    @Operation(summary = "List Fixed deposit applications/accounts", description = "Lists Fixed Deposit Accounts\n\n"
            + "Example Requests:\n\n" + "\n\n" + "fixeddepositaccounts\n\n" + "\n\n" + "fixeddepositaccounts?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.GetFixedDepositAccountsResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("paged") @Parameter(description = "paged") final Boolean paged,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final PaginationParameters paginationParameters = PaginationParameters.builder().paged(Boolean.TRUE.equals(paged)).limit(limit)
                .offset(offset).orderBy(orderBy).sortOrder(sortOrder).build();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (paginationParameters.isPaged()) {
            final Page<DepositAccountData> account = this.depositAccountReadPlatformService
                    .retrieveAllPaged(DepositAccountType.FIXED_DEPOSIT, paginationParameters);
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
    @Operation(summary = "Submit new fixed deposit application", description = "Submits a new fixed deposit application"
            + "Mandatory Fields: clientId or groupId, productId, submittedOnDate, depositAmount, depositPeriod, depositPeriodFrequencyId\n\n"
            + "Optional Fields: accountNo, externalId, fieldOfficerId,linkAccountId(if provided initial deposit amount will be collected from this account),transferInterestToSavings(By enabling this flag all interest postings will be transferred to linked saving account )")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsResponse.class))) })
    public String submitApplication(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createFixedDepositAccount().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a fixed deposit application/account", description = "Retrieves a fixed deposit application/account\n\n"
            + "Example Requests :\n\n" + "\n\n" + "fixeddepositaccounts/1" + "\n\n" + "fixeddepositaccounts/1?associations=all")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.GetFixedDepositAccountsAccountIdResponse.class))) })
    public String retrieveOne(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) {
            throw new UnrecognizedQueryParamException("status", chargeStatus, new Object[] { "all", "active", "inactive" });
        }

        final FixedDepositAccountData account = (FixedDepositAccountData) this.depositAccountReadPlatformService
                .retrieveOneWithChartSlabs(DepositAccountType.FIXED_DEPOSIT, accountId);

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final FixedDepositAccountData accountTemplate = populateTemplateAndAssociations(accountId, account, staffInSelectedOfficeOnly,
                chargeStatus, uriInfo, mandatoryResponseParameters);
        accountTemplate.setActivationCharge(getActivationCharge(accountId));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, accountTemplate,
                DepositsApiConstants.FIXED_DEPOSIT_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("calculate-fd-interest")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.CalculateFixedDepositInterestResponse.class))) })
    public String calculateFixedDepositInterest(@Context final UriInfo uriInfo,
            @QueryParam("principalAmount") @Parameter(description = "BigDecimal principalAmount") final BigDecimal principalAmount,
            @QueryParam("annualInterestRate") @Parameter(description = "annualInterestRate") final BigDecimal annualInterestRate,
            @QueryParam("tenureInMonths") @Parameter(description = "tenureInMonths") final Long tenureInMonths,
            @QueryParam("interestCompoundingPeriodInMonths") @Parameter(description = "interestCompoundingPeriodInMonths") final Long interestCompoundingPeriodInMonths,
            @QueryParam("interestPostingPeriodInMonths") @Parameter(description = "interestPostingPeriodInMonths") final Long interestPostingPeriodInMonths) {
        HashMap request = new HashMap<>();
        request.put("annualInterestRate", annualInterestRate);
        request.put("tenureInMonths", tenureInMonths);
        request.put("interestCompoundingPeriodInMonths", interestCompoundingPeriodInMonths);
        request.put("interestPostingPeriodInMonths", interestPostingPeriodInMonths);
        request.put("principalAmount", principalAmount);
        String apiRequestBodyAsJson = toApiJsonSerializer.serialize(request);
        JsonElement jsonElement = fromJsonHelper.parse(apiRequestBodyAsJson);
        HashMap result = fixedDepositAccountInterestCalculationService
                .calculateInterest(new JsonQuery(apiRequestBodyAsJson, jsonElement, fromJsonHelper));

        return toApiJsonSerializer.serializeResult(result);

    }

    private BigDecimal getActivationCharge(Long accountId) {
        BigDecimal activationCharge = BigDecimal.ZERO;
        Collection<SavingsAccountChargeData> savingCharges = this.savingsAccountChargeReadPlatformService
                .retrieveSavingsAccountCharges(accountId, "active");
        for (SavingsAccountChargeData savingsAccountChargeData : savingCharges) {
            if (savingsAccountChargeData.isSavingsActivation()) {
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
        PortfolioAccountData transferToSavingsAccount = null;

        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {

            if (associationParameters.contains("all")) {
                associationParameters.addAll(
                        Arrays.asList(SavingsApiConstants.transactions, SavingsApiConstants.charges, SavingsApiConstants.linkedAccount));
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

        if (savingsAccount.getTransferToSavingsId() != null) {
            transferToSavingsAccount = this.accountAssociationsReadPlatformService
                    .retriveSavingsAccount(savingsAccount.getTransferToSavingsId());
        }

        FixedDepositAccountData templateData = null;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            templateData = (FixedDepositAccountData) this.depositAccountReadPlatformService.retrieveTemplate(
                    DepositAccountType.FIXED_DEPOSIT, savingsAccount.getClientId(), savingsAccount.getGroupId(),
                    savingsAccount.getDepositProductId(), staffInSelectedOfficeOnly);
        }

        return FixedDepositAccountData.associationsAndTemplate(savingsAccount, templateData, transactions, charges, linkedAccount,
                transferToSavingsAccount);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a fixed deposit application", description = "Fixed deposit application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.PutFixedDepositAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.PutFixedDepositAccountsAccountIdResponse.class))) })
    public String update(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFixedDepositAccount(accountId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve fixed deposit application | Undo approval fixed deposit application | Reject fixed deposit application | Withdraw fixed deposit application | Activate a fixed deposit account | Close a fixed deposit account | Premature Close a fixed deposit account | Calculate Premature amount on Fixed deposit account | Calculate Interest on Fixed Deposit Account | Post Interest on Fixed Deposit Account", description = "Approve fixed deposit application:\n\n"
            + "Approves fixed deposit application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Undo approval fixed deposit application:\n\n"
            + "Will move 'approved' fixed deposit application back to 'Submitted and pending approval' state.\n\n"
            + "Reject fixed deposit application:\n\n"
            + "Rejects fixed deposit application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Withdraw fixed deposit application:\n\n"
            + "Used when an applicant withdraws from the fixed deposit application. It must be in 'Submitted and pending approval' state.\n\n"
            + "Close a fixed deposit account:\n\n"
            + "Results in a Matured fixed deposit account being converted into a 'closed' fixed deposit account.\n\n"
            + "Premature Close a fixed deposit account:\n\n"
            + "Results in an Active fixed deposit account being converted into a 'Premature Closed' fixed deposit account with options to withdraw prematured amount. (premature amount is calculated using interest rate chart applicable along with penal interest if any.)\n\n"
            + "Calculate Premature amount on Fixed deposit account:\n\n"
            + "Calculate premature amount on fixed deposit account till premature close date. Premature amount is calculated based on interest chart and penal interest applicable.\n\n"
            + "Calculate Interest on Fixed Deposit Account:\n\n"
            + "Calculates interest earned on a fixed deposit account based on todays date. It does not attempt to post or credit the interest on the account. That is responsibility of the Post Interest API that will likely be called by overnight process.\n\n"
            + "Post Interest on Fixed Deposit Account:\n\n"
            + "Calculates and Posts interest earned on a fixed deposit account based on today's date and whether an interest posting or crediting event is due.\n\n"
            + "Showing request/response for Calculate Interest on Fixed Deposit Account")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.PostFixedDepositAccountsAccountIdResponse.class))) })
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
    @Operation(summary = "Delete a fixed deposit application", description = "At present we support hard delete of fixed deposit application so long as its in 'Submitted and pending approval' state. One the application is moves past this state, it is not possible to do a 'hard' delete of the application or the account. An API endpoint will be added to close/de-activate the fixed deposit account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FixedDepositAccountsApiResourceSwagger.DeleteFixedDepositAccountsAccountIdResponse.class))) })
    public String delete(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteFixedDepositAccount(accountId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String accountClosureTemplate(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo) {

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
    public Response getFixedDepositTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS.toString(), officeId, staffId,
                dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload fixed deposit template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postFixedDepositTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.FIXED_DEPOSIT_ACCOUNTS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("transaction/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getFixedDepositTransactionTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS.toString(), officeId, null,
                dateFormat);
    }

    @POST
    @Path("transaction/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload fixed deposit transaction template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postFixedDepositTransactionTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.FIXED_DEPOSIT_TRANSACTIONS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
