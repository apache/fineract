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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import javax.validation.constraints.DecimalMin;
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
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Path("/savingsaccounts/{savingsId}/transactions")
@Component
@Scope("singleton")
@Tag(name = "Savings Account Transactions", description = "")
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
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Savings Account Transactions", description = "The list capability of savings account transactions can support pagination, sorting and filtering.\n\n"
            + "Example Requests:\n" + "\n" + "savingsaccounts/{savingsId}/transactions\n" + "\n"
            + "savingsaccounts/{savingsId}/transactions?offset=10&limit=50\n" + "\n"
            + "savingsaccounts/{savingsId}/transactions?orderBy=displayName&sortOrder=DESC\n" + "\n"
            + "savingsaccounts/{savingsId}/transactions?dateFormat=yyyy-MM-dd&locale=en&fromDate=2013-01-01&toDate=2013-12-01\n" + "\n"
            + "savingsaccounts/{savingsId}/transactions?fromAmount=500&toAmount=1000\n" + "\n"
            + "savingsaccounts/{savingsId}/transactions?transactionType=deposit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.GetSavingsAccountTransactionsResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @PathParam("savingsId") @Parameter(description = "savingsId") final Long savingsId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String dateFormat,
            @QueryParam("fromDate") @Parameter(description = "fromDate") final DateParam fromDateParam,
            @QueryParam("toDate") @Parameter(description = "toDate") final DateParam toDateParam,
            @QueryParam("fromAmount") @Parameter(description = "fromAmount") @DecimalMin(value = "0", message = "must be greater than or equal to 0") final BigDecimal fromAmount,
            @QueryParam("toAmount") @Parameter(description = "toAmount") @DecimalMin(value = "0", message = "must be greater than or equal to 0") final BigDecimal toAmount,
            @QueryParam("locale") @Parameter(description = "locale") final String locale,
            @QueryParam("transactionType") @Parameter(description = "transcationType") final String transactionType) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        LocalDate fromDate = null;
        if (fromDateParam != null) {
            fromDate = fromDateParam.getDate("fromDate", dateFormat, locale);
        }
        LocalDate toDate = null;
        if (toDateParam != null) {
            toDate = toDateParam.getDate("toDate", dateFormat, locale);
        }
        final SearchParameters searchParameters = SearchParameters.forSavingsAccountTranscations(offset, limit, orderBy, sortOrder,
                transactionType, fromAmount, toAmount, fromDate, toDate);
        final Page<SavingsAccountTransactionData> savingsAccountTransactionsData = savingsAccountReadPlatformService
                .retrieveAllTransactions(savingsId, DepositAccountType.SAVINGS_DEPOSIT, searchParameters);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings,
                new Page<>(savingsAccountTransactionsData.getPageItems(), savingsAccountTransactionsData.getTotalFilteredRecords()),
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
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
                SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountTransactionsResponse.class))) })
    public String transaction(@PathParam("savingsId") final Long savingsId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
        try {
            final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

            CommandProcessingResult result = null;
            if (is(commandParam, "deposit")) {
                final CommandWrapper commandRequest = builder.savingsAccountDeposit(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "gsimDeposit")) {
                final CommandWrapper commandRequest = builder.gsimSavingsAccountDeposit(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "withdrawal")) {
                final CommandWrapper commandRequest = builder.savingsAccountWithdrawal(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, "postInterestAsOn")) {
                final CommandWrapper commandRequest = builder.savingsAccountInterestPosting(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            } else if (is(commandParam, SavingsApiConstants.COMMAND_HOLD_AMOUNT)) {
                final CommandWrapper commandRequest = builder.holdAmount(savingsId).build();
                result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            }

            if (result == null) {
                //
                throw new UnrecognizedQueryParamException("command", commandParam,
                        new Object[] { "deposit", "withdrawal", SavingsApiConstants.COMMAND_HOLD_AMOUNT });
            }

            return this.toApiJsonSerializer.serialize(result);
        } catch (ObjectOptimisticLockingFailureException lockingFailureException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations",
                    "Concurrent Transactions being made on this savings account: " + lockingFailureException.getMessage(),
                    lockingFailureException);
        } catch (CannotAcquireLockException cannotAcquireLockException) {
            throw new PlatformDataIntegrityException("error.msg.savings.concurrent.operations.unable.to.acquire.lock",
                    "Unable to acquir lock for this transaction: " + cannotAcquireLockException.getMessage(), cannotAcquireLockException);
        }
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo/Reverse/Modify/Release Amount transaction API", description = "Undo/Reverse/Modify/Release Amount transaction API\n\n"
            + "Example Requests:\n" + "\n" + "\n" + "savingsaccounts/{savingsId}/transactions/{transactionId}?command=reverse\n" + "\n"
            + "Accepted command = undo, reverse, modify, releaseAmount")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountBulkReversalTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountBulkReversalTransactionsRequest.class)))) })
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
        } else if (is(commandParam, SavingsApiConstants.COMMAND_REVERSE_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.reverseSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_ADJUST_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.adjustSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, SavingsApiConstants.COMMAND_RELEASE_AMOUNT)) {
            final CommandWrapper commandRequest = builder.releaseAmount(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[] { SavingsApiConstants.COMMAND_UNDO_TRANSACTION, SavingsApiConstants.COMMAND_ADJUST_TRANSACTION,
                            SavingsApiConstants.COMMAND_RELEASE_AMOUNT, SavingsApiConstants.COMMAND_REVERSE_TRANSACTION });
        }

        return this.toApiJsonSerializer.serialize(result);
    }
}
