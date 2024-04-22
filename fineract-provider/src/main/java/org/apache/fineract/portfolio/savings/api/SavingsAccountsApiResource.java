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
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/v1/savingsaccounts")
@Component
@Tag(name = "Savings Account", description = "Savings accounts are instances of a particular savings product created for an individual or group. An application process around the creation of accounts is also supported.")
@RequiredArgsConstructor
public class SavingsAccountsApiResource {

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final SqlValidator sqlValidator;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Savings Account Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Requests:\n" + "\n" + "savingsaccounts/template?clientId=1\n"
            + "\n" + "\n" + "savingsaccounts/template?clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.GetSavingsAccountsTemplateResponse.class))) })
    public String template(@QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final SavingsAccountData savingsAccount = savingsAccountReadPlatformService.retrieveTemplate(clientId, groupId, productId,
                staffInSelectedOfficeOnly);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, savingsAccount, SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List savings applications/accounts", description = "Lists savings applications/accounts\n\n"
            + "Example Requests:\n" + "\n" + "savingsaccounts\n" + "\n" + "\n" + "savingsaccounts?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.GetSavingsAccountsResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            // @QueryParam("underHierarchy") final String hierarchy,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).externalId(externalId).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();

        final Page<SavingsAccountData> products = savingsAccountReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, products, SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Submit new savings application", description = "Submits new savings application\n\n"
            + "Mandatory Fields: clientId or groupId, productId, submittedOnDate\n\n"
            + "Optional Fields: accountNo, externalId, fieldOfficerId\n\n"
            + "Inherited from Product (if not provided): nominalAnnualInterestRate, interestCompoundingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, allowOverdraft, overdraftLimit, withHoldTax\n\n"
            + "Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type Savings: datatables")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsResponse.class))) })
    public String submitApplication(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccount().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("/gsim")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String submitGSIMApplication(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createGSIMAccount().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a savings application/account", description = "Retrieves a savings application/account\n\n"
            + "Example Requests :\n" + "\n" + "savingsaccounts/1\n" + "\n" + "\n" + "savingsaccounts/1?associations=all")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.GetSavingsAccountsAccountIdResponse.class))) })
    public String retrieveOne(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        return retrieveSavingAccount(accountId, null, staffInSelectedOfficeOnly, chargeStatus, uriInfo);
    }

    @GET
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a savings application/account by external id", description = "Retrieves a savings application/account by external id\n\n"
            + "Example Requests :\n" + "\n" + "savingsaccounts/external-id/ExternalId1\n" + "\n" + "\n"
            + "savingsaccounts/external-id/ExternalId1?associations=all")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.GetSavingsAccountsAccountIdResponse.class))) })
    public String retrieveOne(@PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        return retrieveSavingAccount(null, externalId, staffInSelectedOfficeOnly, chargeStatus, uriInfo);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a savings application | Modify savings account withhold tax applicability", description = "Modify a savings application:\n\n"
            + "Savings application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc\n\n"
            + "Modify savings account withhold tax applicability:\n\n"
            + "Savings application's withhold tax can be modified when in 'Active' state. Once the application is activated, can modify the account withhold tax to post tax or vice-versa"
            + "Showing request/response for 'Modify a savings application'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PutSavingsAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PutSavingsAccountsAccountIdResponse.class))) })
    public String update(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        return updateSavingAccount(accountId, null, apiRequestBodyAsJson, commandParam);
    }

    @PUT
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Modify a savings application | Modify savings account withhold tax applicability", description = "Modify a savings application:\n\n"
            + "Savings application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method. Specific api endpoints will be created to allow change of interest detail such as rate, compounding period, posting period etc\n\n"
            + "Modify savings account withhold tax applicability:\n\n"
            + "Savings application's withhold tax can be modified when in 'Active' state. Once the application is activated, can modify the account withhold tax to post tax or vice-versa"
            + "Showing request/response for 'Modify a savings application'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PutSavingsAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PutSavingsAccountsAccountIdResponse.class))) })
    public String update(@PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        return updateSavingAccount(null, externalId, apiRequestBodyAsJson, commandParam);
    }

    @PUT
    @Path("/gsim/{parentAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGsim(@PathParam("parentAccountId") final Long parentAccountId, final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateGSIMAccount(parentAccountId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("/gsimcommands/{parentAccountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String handleGSIMCommands(@PathParam("parentAccountId") final Long parentAccountId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectGSIMAccountApplication(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawSavingsAccountApplication(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveGSIMAccountApplication(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoGSIMApplicationApproval(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.gsimAccountActivation(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculateInterest")) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().savingsAccountInterestCalculation(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "postInterest")) {
            final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "applyAnnualFees")) {
            final CommandWrapper commandRequest = builder.savingsAccountApplyAnnualFees(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeGSIMApplication(parentAccountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[] { "reject", "withdrawnByApplicant", "approve", "undoapproval", "activate", "calculateInterest",
                            "postInterest", "close", "assignSavingsOfficer", "unassignSavingsOfficer",
                            SavingsApiConstants.COMMAND_BLOCK_DEBIT, SavingsApiConstants.COMMAND_UNBLOCK_DEBIT,
                            SavingsApiConstants.COMMAND_BLOCK_CREDIT, SavingsApiConstants.COMMAND_UNBLOCK_CREDIT,
                            SavingsApiConstants.COMMAND_BLOCK_ACCOUNT, SavingsApiConstants.COMMAND_UNBLOCK_ACCOUNT });
        }

        return toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve savings application | Undo approval savings application | Assign Savings Officer | Unassign Savings Officer | Reject savings application | Withdraw savings application | Activate a savings account | Close a savings account | Calculate Interest on Savings Account | Post Interest on Savings Account | Block Savings Account | Unblock Savings Account | Block Savings Account Credit transactions | Unblock Savings Account Credit transactions | Block Savings Account Debit transactions | Unblock Savings Account debit transactions", description = "Approve savings application:\n\n"
            + "Approves savings application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Undo approval savings application:\n\n"
            + "Will move 'approved' savings application back to 'Submitted and pending approval' state.\n\n" + "Assign Savings Officer:\n\n"
            + "Allows you to assign Savings Officer for existing Savings Account.\n\n" + "Unassign Savings Officer:\n\n"
            + "Allows you to unassign the Savings Officer.\n\n" + "Reject savings application:\n\n"
            + "Rejects savings application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Withdraw savings application:\n\n"
            + "Used when an applicant withdraws from the savings application. It must be in 'Submitted and pending approval' state.\n\n"
            + "Activate a savings account:\n\n"
            + "Results in an approved savings application being converted into an 'active' savings account.\n\n"
            + "Close a savings account:\n\n"
            + "Results in an Activated savings application being converted into an 'closed' savings account.\n" + "\n"
            + "closedOnDate is closure date of savings account\n" + "\n"
            + "withdrawBalance is a boolean description, true value of this field performs a withdrawal transaction with account's running balance.\n\n"
            + "Mandatory Fields: dateFormat,locale,closedOnDate\n\n"
            + "Optional Fields: note, withdrawBalance, paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber\n\n"
            + "Calculate Interest on Savings Account:\n\n"
            + "Calculates interest earned on a savings account based on todays date. It does not attempt to post or credit the interest on the account. That is responsibility of the Post Interest API that will likely be called by overnight process.\n\n"
            + "Post Interest on Savings Account:\n\n"
            + "Calculates and Posts interest earned on a savings account based on today's date and whether an interest posting or crediting event is due.\n\n"
            + "Block Savings Account:\n\n" + "Blocks Savings account from all types of credit and debit transactions\n\n"
            + "Unblock Savings Account:\n\n"
            + "Unblock a blocked account. On unblocking account, user can perform debit and credit transactions\n\n"
            + "Block Savings Account Credit transactions:\n\n"
            + "Savings account will be blocked from all types of credit transactions.\n\n"
            + "Unblock Savings Account Credit transactions:\n\n"
            + "It unblocks the Saving account's credit operations. Now all types of credits can be transacted to Savings account\n\n"
            + "Block Savings Account Debit transactions:\n\n" + "All types of debit operations from Savings account wil be blocked\n\n"
            + "Unblock Savings Account debit transactions:\n\n"
            + "It unblocks the Saving account's debit operations. Now all types of debits can be transacted from Savings account\n\n"
            + "Showing request/response for 'Unassign Savings Officer'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsAccountIdResponse.class))) })
    public String handleCommands(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleCommands(accountId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Approve savings application | Undo approval savings application | Assign Savings Officer | Unassign Savings Officer | Reject savings application | Withdraw savings application | Activate a savings account | Close a savings account | Calculate Interest on Savings Account | Post Interest on Savings Account | Block Savings Account | Unblock Savings Account | Block Savings Account Credit transactions | Unblock Savings Account Credit transactions | Block Savings Account Debit transactions | Unblock Savings Account debit transactions", description = "Approve savings application:\n\n"
            + "Approves savings application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Undo approval savings application:\n\n"
            + "Will move 'approved' savings application back to 'Submitted and pending approval' state.\n\n" + "Assign Savings Officer:\n\n"
            + "Allows you to assign Savings Officer for existing Savings Account.\n\n" + "Unassign Savings Officer:\n\n"
            + "Allows you to unassign the Savings Officer.\n\n" + "Reject savings application:\n\n"
            + "Rejects savings application so long as its in 'Submitted and pending approval' state.\n\n"
            + "Withdraw savings application:\n\n"
            + "Used when an applicant withdraws from the savings application. It must be in 'Submitted and pending approval' state.\n\n"
            + "Activate a savings account:\n\n"
            + "Results in an approved savings application being converted into an 'active' savings account.\n\n"
            + "Close a savings account:\n\n"
            + "Results in an Activated savings application being converted into an 'closed' savings account.\n" + "\n"
            + "closedOnDate is closure date of savings account\n" + "\n"
            + "withdrawBalance is a boolean description, true value of this field performs a withdrawal transaction with account's running balance.\n\n"
            + "Mandatory Fields: dateFormat,locale,closedOnDate\n\n"
            + "Optional Fields: note, withdrawBalance, paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber\n\n"
            + "Calculate Interest on Savings Account:\n\n"
            + "Calculates interest earned on a savings account based on todays date. It does not attempt to post or credit the interest on the account. That is responsibility of the Post Interest API that will likely be called by overnight process.\n\n"
            + "Post Interest on Savings Account:\n\n"
            + "Calculates and Posts interest earned on a savings account based on today's date and whether an interest posting or crediting event is due.\n\n"
            + "Block Savings Account:\n\n" + "Blocks Savings account from all types of credit and debit transactions\n\n"
            + "Unblock Savings Account:\n\n"
            + "Unblock a blocked account. On unblocking account, user can perform debit and credit transactions\n\n"
            + "Block Savings Account Credit transactions:\n\n"
            + "Savings account will be blocked from all types of credit transactions.\n\n"
            + "Unblock Savings Account Credit transactions:\n\n"
            + "It unblocks the Saving account's credit operations. Now all types of credits can be transacted to Savings account\n\n"
            + "Block Savings Account Debit transactions:\n\n" + "All types of debit operations from Savings account wil be blocked\n\n"
            + "Unblock Savings Account debit transactions:\n\n"
            + "It unblocks the Saving account's debit operations. Now all types of debits can be transacted from Savings account\n\n"
            + "Showing request/response for 'Unassign Savings Officer'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsAccountIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsAccountIdResponse.class))) })
    public String handleCommands(@PathParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleCommands(null, externalId, commandParam, apiRequestBodyAsJson);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @DELETE
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a savings application", description = "At present we support hard delete of savings application so long as its in 'Submitted and pending approval' state. One the application is moves past this state, it is not possible to do a 'hard' delete of the application or the account. An API endpoint will be added to close/de-activate the savings account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.DeleteSavingsAccountsAccountIdResponse.class))) })
    public String delete(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId) {

        return deleteSavingAccount(accountId, null);
    }

    @DELETE
    @Path("/external-id/{externalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a savings application", description = "At present we support hard delete of savings application so long as its in 'Submitted and pending approval' state. One the application is moves past this state, it is not possible to do a 'hard' delete of the application or the account. An API endpoint will be added to close/de-activate the savings account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.DeleteSavingsAccountsAccountIdResponse.class))) })
    public String delete(@PathParam("externalId") @Parameter(description = "externalId") final String externalId) {

        return deleteSavingAccount(null, externalId);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSavingsTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SAVINGS_ACCOUNT.toString(), officeId, staffId, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload savings template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postSavingsTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.SAVINGS_ACCOUNT.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return toApiJsonSerializer.serialize(importDocumentId);
    }

    @GET
    @Path("transactions/downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getSavingsTransactionTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.SAVINGS_TRANSACTIONS.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("transactions/uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload savings transaction template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postSavingsTransactionTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.SAVINGS_TRANSACTIONS.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return toApiJsonSerializer.serialize(importDocumentId);
    }

    private String retrieveSavingAccount(Long accountId, String externalId, boolean staffInSelectedOfficeOnly, String chargeStatus,
            UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) {
            throw new UnrecognizedQueryParamException("status", chargeStatus, new Object[] { "all", "active", "inactive" });
        }

        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);
        final SavingsAccountData savingsAccount = savingsAccountReadPlatformService.retrieveOne(accountId);

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final SavingsAccountData savingsAccountTemplate = populateTemplateAndAssociations(accountId, savingsAccount,
                staffInSelectedOfficeOnly, chargeStatus, uriInfo, mandatoryResponseParameters);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return toApiJsonSerializer.serialize(settings, savingsAccountTemplate,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_RESPONSE_DATA_PARAMETERS);
    }

    private String updateSavingAccount(Long accountId, String externalId, String apiRequestBodyAsJson, String commandParam) {
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);

        if (is(commandParam, "updateWithHoldTax")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).updateWithHoldTax(accountId)
                    .build();
            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return toApiJsonSerializer.serialize(result);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingsAccount(accountId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    private String handleCommands(Long accountId, String externalId, String commandParam, String apiRequestBodyAsJson) {
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, "reject")) {
            final CommandWrapper commandRequest = builder.rejectSavingsAccountApplication(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawnByApplicant")) {
            final CommandWrapper commandRequest = builder.withdrawSavingsAccountApplication(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            final CommandWrapper commandRequest = builder.approveSavingsAccountApplication(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoSavingsAccountApplication(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.savingsAccountActivation(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "calculateInterest")) {
            final CommandWrapper commandRequest = builder.withNoJsonBody().savingsAccountInterestCalculation(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "postInterest")) {
            final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "applyAnnualFees")) {
            final CommandWrapper commandRequest = builder.savingsAccountApplyAnnualFees(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeSavingsAccountApplication(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "assignSavingsOfficer")) {
            final CommandWrapper commandRequest = builder.assignSavingsOfficer(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignSavingsOfficer")) {
            final CommandWrapper commandRequest = builder.unassignSavingsOfficer(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_BLOCK_DEBIT)) {
            final CommandWrapper commandRequest = builder.blockDebitsFromSavingsAccount(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_UNBLOCK_DEBIT)) {
            final CommandWrapper commandRequest = builder.unblockDebitsFromSavingsAccount(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_BLOCK_CREDIT)) {
            final CommandWrapper commandRequest = builder.blockCreditsToSavingsAccount(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_UNBLOCK_CREDIT)) {
            final CommandWrapper commandRequest = builder.unblockCreditsToSavingsAccount(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_BLOCK_ACCOUNT)) {
            final CommandWrapper commandRequest = builder.blockSavingsAccount(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_UNBLOCK_ACCOUNT)) {
            final CommandWrapper commandRequest = builder.unblockSavingsAccount(accountId).build();
            result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[] { "reject", "withdrawnByApplicant", "approve", "undoapproval", "activate", "calculateInterest",
                            "postInterest", "close", "assignSavingsOfficer", "unassignSavingsOfficer",
                            SavingsApiConstants.COMMAND_BLOCK_DEBIT, SavingsApiConstants.COMMAND_UNBLOCK_DEBIT,
                            SavingsApiConstants.COMMAND_BLOCK_CREDIT, SavingsApiConstants.COMMAND_UNBLOCK_CREDIT,
                            SavingsApiConstants.COMMAND_BLOCK_ACCOUNT, SavingsApiConstants.COMMAND_UNBLOCK_ACCOUNT });
        }

        return toApiJsonSerializer.serialize(result);
    }

    private String deleteSavingAccount(Long accountId, String externalId) {
        ExternalId accountExternalId = ExternalIdFactory.produce(externalId);
        accountId = getResolvedAccountId(accountId, accountExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingsAccount(accountId).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return toApiJsonSerializer.serialize(result);
    }

    private Long getResolvedAccountId(Long accountId, ExternalId accountExternalId) {
        Long resolvedAccountId = accountId;
        if (resolvedAccountId == null) {
            accountExternalId.throwExceptionIfEmpty();
            resolvedAccountId = savingsAccountReadPlatformService.retrieveAccountIdByExternalId(accountExternalId);
            if (resolvedAccountId == null) {
                throw new SavingsAccountNotFoundException(resolvedAccountId);
            }
        }
        return resolvedAccountId;
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
                final Collection<SavingsAccountTransactionData> currentTransactions = savingsAccountReadPlatformService
                        .retrieveAllTransactions(accountId, DepositAccountType.SAVINGS_DEPOSIT);
                if (!CollectionUtils.isEmpty(currentTransactions)) {
                    transactions = currentTransactions;
                }
            }

            if (associationParameters.contains(SavingsApiConstants.charges)) {
                mandatoryResponseParameters.add(SavingsApiConstants.charges);
                final Collection<SavingsAccountChargeData> currentCharges = savingsAccountChargeReadPlatformService
                        .retrieveSavingsAccountCharges(accountId, chargeStatus);
                if (!CollectionUtils.isEmpty(currentCharges)) {
                    charges = currentCharges;
                }
            }
        }

        SavingsAccountData templateData = null;
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            templateData = savingsAccountReadPlatformService.retrieveTemplate(savingsAccount.getClientId(), savingsAccount.getGroupId(),
                    savingsAccount.getSavingsProductId(), staffInSelectedOfficeOnly);
        }

        return SavingsAccountData.withTemplateOptions(savingsAccount, templateData, transactions, charges);
    }
}
