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
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionData;
import org.mifosplatform.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Path("/savingsaccounts/{savingsId}/transactions")
@Component
@Scope("singleton")
public class SavingsAccountTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public SavingsAccountTransactionsApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@PathParam("savingsId") final Long savingsId,
    // @QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        // FIXME - KW - for now just send back generic default information for
        // both deposit/withdrawal templates
        SavingsAccountTransactionData savingsAccount = this.savingsAccountReadPlatformService.retrieveDepositTransactionTemplate(savingsId,
                DepositAccountType.SAVINGS_DEPOSIT);
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
    public String retrieveOne(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        SavingsAccountTransactionData transactionData = this.savingsAccountReadPlatformService.retrieveSavingsTransaction(savingsId,
                transactionId, DepositAccountType.SAVINGS_DEPOSIT);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            transactionData = SavingsAccountTransactionData.templateOnTop(transactionData, paymentTypeOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, transactionData,
                SavingsApiConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String transaction(@PathParam("savingsId") final Long savingsId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
        try {
            final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

            CommandProcessingResult result = null;
            if (is(commandParam, "deposit")) {
                final CommandWrapper commandRequest = builder.savingsAccountDeposit(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "withdrawal")) {
                final CommandWrapper commandRequest = builder.savingsAccountWithdrawal(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            }

            if (result == null) {
                //
                throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "deposit", "withdrawal" });
            }

            return this.toApiJsonSerializer.serialize(result);
        } catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage());
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage());
        }
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String adjustTransaction(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, SavingsApiConstants.COMMAND_UNDO_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.undoSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_ADJUST_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.adjustSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "undo" });
        }

        return this.toApiJsonSerializer.serialize(result);
    }
}