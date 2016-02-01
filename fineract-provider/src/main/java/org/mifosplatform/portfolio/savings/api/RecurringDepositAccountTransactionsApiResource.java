/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.api;

import java.util.Collection;

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

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.DepositsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionData;
import org.mifosplatform.portfolio.savings.service.DepositAccountReadPlatformService;
import org.mifosplatform.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/recurringdepositaccounts/{recurringDepositAccountId}/transactions")
@Component
@Scope("singleton")
public class RecurringDepositAccountTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

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
    public String retrieveTemplate(@PathParam("recurringDepositAccountId") final Long recurringDepositAccountId,
            @QueryParam("command") final String commandParam, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);

        /***
         * Check @Param commandParam value for deposit or withdrawal
         */
        if (!(is(commandParam, "deposit") || is(commandParam, "withdrawal"))) { throw new UnrecognizedQueryParamException("command",
                commandParam, new Object[] { "deposit", "withdrawal" }); }

        /***
         * By default get the deposit template for deposits and withdrawal
         * transactions
         */
        SavingsAccountTransactionData savingsAccount = this.depositAccountReadPlatformService
                .retrieveRecurringAccountDepositTransactionTemplate(recurringDepositAccountId);

        /***
         * Update transaction date and transaction type if transaction type is
         * withdrawal
         */
        if (is(commandParam, "withdrawal")) {
            savingsAccount = SavingsAccountTransactionData.withWithDrawalTransactionDetails(savingsAccount);
        }

        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        savingsAccount = SavingsAccountTransactionData.templateOnTop(savingsAccount, paymentTypeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccount,
                SavingsApiConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("recurringDepositAccountId") final Long recurringDepositAccountId,
            @PathParam("transactionId") final Long transactionId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_ACCOUNT_RESOURCE_NAME);
        SavingsAccountTransactionData transactionData = this.savingsAccountReadPlatformService.retrieveSavingsTransaction(
                recurringDepositAccountId, transactionId, DepositAccountType.RECURRING_DEPOSIT);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            transactionData = SavingsAccountTransactionData.templateOnTop(transactionData, paymentTypeOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, transactionData,
                DepositsApiConstants.FIXED_DEPOSIT_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String transaction(@PathParam("recurringDepositAccountId") final Long recurringDepositAccountId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

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
    public String handleTransactionCommands(@PathParam("recurringDepositAccountId") final Long recurringDepositAccountId,
            @PathParam("transactionId") final Long transactionId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

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