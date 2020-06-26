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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/recurringdepositaccounts/{recurringDepositAccountId}/transactions")
@Component
@Scope("singleton")
@Tag(name = "Recurring Deposit Account Transactions", description = "Transactions possible on a recurring deposit account.")
public class RecurringDepositAccountTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private static final Set<String> FIXED_DEPOSIT_TRANSACTION_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(DepositsApiConstants.idParamName, DepositsApiConstants.accountIdParamName,
                    DepositsApiConstants.accountNoParamName, DepositsApiConstants.currencyParamName, DepositsApiConstants.amountParamName,
                    DepositsApiConstants.dateParamName, DepositsApiConstants.paymentDetailDataParamName,
                    DepositsApiConstants.runningBalanceParamName, DepositsApiConstants.reversedParamName));

    @Autowired
    public RecurringDepositAccountTransactionsApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final DepositAccountReadPlatformService depositAccountReadPlatformService,
            final PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.depositAccountReadPlatformService = depositAccountReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Recurring Deposit Account Transaction Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Requests:\n" + "\n"
            + "recurringdepositaccounts/1/transactions/template?command=deposit\n" + "\n"
            + "recurringdepositaccounts/1/transactions/template?command=withdrawal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountTransactionsApiResourceSwagger.GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTemplateResponse.class))) })
    public String retrieveTemplate(
            @PathParam("recurringDepositAccountId") @Parameter(description = "recurringDepositAccountId") final Long recurringDepositAccountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        /***
         * Check @Param commandParam description for deposit or withdrawal
         */
        if (!(is(commandParam, "deposit") || is(commandParam, "withdrawal"))) {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "deposit", "withdrawal" });
        }

        /***
         * By default get the deposit template for deposits and withdrawal transactions
         */
        SavingsAccountTransactionData savingsAccount = this.depositAccountReadPlatformService
                .retrieveRecurringAccountDepositTransactionTemplate(recurringDepositAccountId);

        /***
         * Update transaction date and transaction type if transaction type is withdrawal
         */
        if (is(commandParam, "withdrawal")) {
            savingsAccount = SavingsAccountTransactionData.withWithDrawalTransactionDetails(savingsAccount);
        }

        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        savingsAccount = SavingsAccountTransactionData.templateOnTop(savingsAccount, paymentTypeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccount,
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Recurring Deposit Account Transaction", description = "Retrieves Recurring Deposit Account Transaction\n\n"
            + "Example Requests:\n" + "\n" + "recurringdepositaccounts/1/transactions/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountTransactionsApiResourceSwagger.GetRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse.class))) })
    public String retrieveOne(
            @PathParam("recurringDepositAccountId") @Parameter(description = "recurringDepositAccountId") final Long recurringDepositAccountId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        SavingsAccountTransactionData transactionData = this.savingsAccountReadPlatformService
                .retrieveSavingsTransaction(recurringDepositAccountId, transactionId, DepositAccountType.RECURRING_DEPOSIT);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            transactionData = SavingsAccountTransactionData.templateOnTop(transactionData, paymentTypeOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, transactionData, FIXED_DEPOSIT_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Deposit Transaction | Withdrawal Transaction", description = "Deposit Transaction:\n\n"
            + "Used for a deposit transaction\n\n" + "Withdrawal Transaction:\n\n" + "Used for a Withdrawal Transaction\n\n"
            + "Showing request/response for Deposit Transaction")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RecurringDepositAccountTransactionsApiResourceSwagger.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountTransactionsApiResourceSwagger.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsResponse.class))) })
    public String transaction(
            @PathParam("recurringDepositAccountId") @Parameter(description = "recurringDepositAccountId") final Long recurringDepositAccountId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;

        if (is(commandParam, "deposit")) {
            final CommandWrapper commandRequest = builder.recurringAccountDeposit(recurringDepositAccountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawal")) {
            final CommandWrapper commandRequest = builder.recurringAccountWithdrawal(recurringDepositAccountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "deposit", "withdrawal" });
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Adjust Transaction | Undo transaction", description = "Adjust Transaction:\n\n"
            + "This command modifies the given transaction.\n\n" + "Undo transaction:\n\n"
            + "This command reverses the given transaction.\n\n" + "Showing request/response for 'Adjust Transaction'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = RecurringDepositAccountTransactionsApiResourceSwagger.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RecurringDepositAccountTransactionsApiResourceSwagger.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsTransactionIdResponse.class))) })
    public String handleTransactionCommands(
            @PathParam("recurringDepositAccountId") @Parameter(description = "recurringDepositAccountId") final Long recurringDepositAccountId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, DepositsApiConstants.COMMAND_UNDO_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.undoRecurringAccountTransaction(recurringDepositAccountId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, DepositsApiConstants.COMMAND_ADJUST_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.adjustRecurringAccountTransaction(recurringDepositAccountId, transactionId)
                    .build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "undo", "modify" });
        }

        return this.toApiJsonSerializer.serialize(result);
    }
}
