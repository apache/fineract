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

import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.PagedLocalRequest;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.search.SavingsAccountTransactionSearchService;
import org.apache.fineract.portfolio.search.data.AdvancedQueryRequest;
import org.apache.fineract.portfolio.search.data.TransactionSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Path("/v1/savingsaccounts/{savingsId}/transactions")
@Component
@Tag(name = "Savings Account Transactions", description = "")
@RequiredArgsConstructor
public class SavingsAccountTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final PaymentTypeReadService paymentTypeReadPlatformService;
    private final SavingsAccountTransactionSearchService transactionsSearchService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a savings account transaction template", operationId = "retrieveTemplateSavingsAccountTransaction")
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
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a savings account transaction", operationId = "retrieveOneSavingsAccountTransaction")
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

    @GET
    @Path("search")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Search Savings Account Transactions")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.SavingsAccountTransactionsSearchResponse.class)))
    public String searchTransactions(@PathParam("savingsId") @Parameter(description = "savings account id") final Long savingsId,
            @QueryParam("fromDate") @Parameter(description = "minimum value date (inclusive)", example = "2023-08-08") final String fromDate,
            @QueryParam("toDate") @Parameter(description = "maximum value date (inclusive)", example = "2023-08-15") final String toDate,
            @QueryParam("fromSubmittedDate") @Parameter(description = "minimum booking date (inclusive)", example = "2023-08-08") final String fromSubmittedDate,
            @QueryParam("toSubmittedDate") @Parameter(description = "maximum booking date (inclusive)", example = "2023-08-15") final String toSubmittedDate,
            @QueryParam("fromAmount") @Parameter(description = "minimum transaction amount (inclusive)", example = "1000") final BigDecimal fromAmount,
            @QueryParam("toAmount") @Parameter(description = "maximum transaction amount (inclusive)", example = "50000000") final BigDecimal toAmount,
            @QueryParam("types") @Parameter(description = "transaction types", example = "1,2,4,20,21") final String types,
            @QueryParam("credit") @Parameter(description = "credit") final Boolean credit,
            @QueryParam("debit") @Parameter(description = "debit") final Boolean debit,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "sort properties", example = "createdDate,transactionDate,id") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sort direction") final Sort.Direction sortOrder,
            @QueryParam("locale") @Parameter(description = "locale") final String localeString,
            @QueryParam("dateFormat") @Parameter(description = "date format", example = "yyyy-MM-dd") String dateFormat) {
        final Locale locale = localeString == null ? null : JsonParserHelper.localeFromString(localeString);
        TransactionSearchRequest searchParameters = new TransactionSearchRequest().accountId(savingsId)
                .fromDate(fromDate, dateFormat, locale).toDate(toDate, dateFormat, locale)
                .fromSubmittedDate(fromSubmittedDate, dateFormat, locale).toSubmittedDate(toSubmittedDate, dateFormat, locale)
                .fromAmount(fromAmount).toAmount(toAmount).types(types).credit(credit).debit(debit)
                .pageable(offset, limit, orderBy, sortOrder);
        Page<SavingsAccountTransactionData> transactionsData = transactionsSearchService.searchTransactions(savingsId, searchParameters);
        return toApiJsonSerializer.serialize(transactionsData);
    }

    @POST
    @Path("query")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Advanced search Savings Account Transactions", operationId = "advancedQuerySavingsAccountTransactions")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = List.class)))
    public String advancedQuery(@PathParam("savingsId") @Parameter(description = "savingsId") final Long savingsId,
            PagedLocalRequest<AdvancedQueryRequest> queryRequest, @Context final UriInfo uriInfo) {
        final Page<JsonObject> result = transactionsSearchService.queryAdvanced(savingsId, queryRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a savings account transaction", operationId = "createSavingsAccountTransaction")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountTransactionsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountTransactionsResponse.class)))
    public String transaction(@PathParam("savingsId") final Long savingsId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        final CommandWrapper commandRequest = switch (StringUtils.trimToEmpty(commandParam)) {
            case "deposit" -> builder.savingsAccountDeposit(savingsId).build();
            case "gsimDeposit" -> builder.gsimSavingsAccountDeposit(savingsId).build();
            case "withdrawal" -> builder.savingsAccountWithdrawal(savingsId).build();
            case "force-withdrawal" -> builder.savingsAccountForceWithdrawal(savingsId).build();
            case "postInterestAsOn" -> builder.savingsAccountInterestPosting(savingsId).build();
            case SavingsApiConstants.COMMAND_HOLD_AMOUNT -> builder.holdAmount(savingsId).build();
            default -> throw new UnrecognizedQueryParamException("command", commandParam, "deposit", "withdrawal", "force-withdrawal",
                    SavingsApiConstants.COMMAND_HOLD_AMOUNT);
        };

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo/Reverse/Modify/Release Amount transaction API", operationId = "adjustSavingsAccountTransaction", description = "Undo/Reverse/Modify/Release Amount transaction API\n\n"
            + "Example Requests:\n" + "\n" + "\n" + "savingsaccounts/{savingsId}/transactions/{transactionId}?command=reverse\n" + "\n"
            + "Accepted command = undo, reverse, modify, releaseAmount")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountBulkReversalTransactionsRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountBulkReversalTransactionsRequest.class))))
    public String adjustTransaction(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        final CommandWrapper commandRequest = switch (StringUtils.trimToEmpty(commandParam)) {
            case SavingsApiConstants.COMMAND_UNDO_TRANSACTION -> builder.undoSavingsAccountTransaction(savingsId, transactionId).build();
            case SavingsApiConstants.COMMAND_REVERSE_TRANSACTION ->
                builder.reverseSavingsAccountTransaction(savingsId, transactionId).build();
            case SavingsApiConstants.COMMAND_ADJUST_TRANSACTION ->
                builder.adjustSavingsAccountTransaction(savingsId, transactionId).build();
            case SavingsApiConstants.COMMAND_RELEASE_AMOUNT -> builder.releaseAmount(savingsId, transactionId).build();
            default -> throw new UnrecognizedQueryParamException("command", commandParam, SavingsApiConstants.COMMAND_UNDO_TRANSACTION,
                    SavingsApiConstants.COMMAND_ADJUST_TRANSACTION, SavingsApiConstants.COMMAND_RELEASE_AMOUNT,
                    SavingsApiConstants.COMMAND_REVERSE_TRANSACTION);
        };

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
