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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentScheduleInstallmentData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargePaidByReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/transactions")
@Component
@Scope("singleton")
@Tag(name = "Loan Transactions", description = "Capabilities include loan repayment's, interest waivers and the ability to 'adjust' an existing transaction. An 'adjustment' of a transaction is really a 'reversal' of existing transaction followed by creation of a new transaction with the provided details.")
public class LoanTransactionsApiResource {

    private final Set<String> responseDataParameters = new HashSet<>(
            Arrays.asList("id", "type", "date", "currency", "amount", "externalId"));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final LoanChargePaidByReadPlatformService loanChargePaidByReadPlatformService;

    @Autowired
    public LoanTransactionsApiResource(final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            LoanChargePaidByReadPlatformService loanChargePaidByReadPlatformService) {
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.loanChargePaidByReadPlatformService = loanChargePaidByReadPlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Transaction Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n\n" + "Example Requests:\n" + "\n"
            + "loans/1/transactions/template?command=repayment" + "loans/1/transactions/template?command=merchantIssuedRefund"
            + "loans/1/transactions/template?command=payoutRefund" + "loans/1/transactions/template?command=goodwillCredit" + "\n"
            + "loans/1/transactions/template?command=waiveinterest" + "\n" + "loans/1/transactions/template?command=writeoff" + "\n"
            + "loans/1/transactions/template?command=close-rescheduled" + "\n" + "loans/1/transactions/template?command=close" + "\n"
            + "loans/1/transactions/template?command=disburse" + "\n" + "loans/1/transactions/template?command=disburseToSavings" + "\n"
            + "loans/1/transactions/template?command=recoverypayment" + "\n" + "loans/1/transactions/template?command=prepayLoan" + "\n"
            + "loans/1/transactions/template?command=refundbycash" + "\n" + "loans/1/transactions/template?command=refundbytransfer" + "\n"
            + "loans/1/transactions/template?command=foreclosure" + "\n"
            + "loans/1/transactions/template?command=creditBalanceRefund (returned 'amount' field will have the overpaid value")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.GetLoansLoanIdTransactionsTemplateResponse.class))) })
    public String retrieveTransactionTemplate(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String dateFormat,
            @QueryParam("transactionDate") @Parameter(description = "transactionDate") final DateParam transactionDateParam,
            @QueryParam("locale") @Parameter(description = "locale") final String locale) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanTransactionData transactionData = null;
        if (is(commandParam, "repayment")) {
            transactionData = this.loanReadPlatformService.retrieveLoanTransactionTemplate(loanId);
        } else if (is(commandParam, "merchantIssuedRefund")) {
            LocalDate transactionDate = DateUtils.getLocalDateOfTenant();
            transactionData = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(LoanTransactionType.MERCHANT_ISSUED_REFUND,
                    loanId, transactionDate);
        } else if (is(commandParam, "payoutRefund")) {
            LocalDate transactionDate = DateUtils.getLocalDateOfTenant();
            transactionData = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(LoanTransactionType.PAYOUT_REFUND, loanId,
                    transactionDate);
        } else if (is(commandParam, "goodwillCredit")) {
            LocalDate transactionDate = DateUtils.getLocalDateOfTenant();
            transactionData = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(LoanTransactionType.GOODWILL_CREDIT, loanId,
                    transactionDate);
        } else if (is(commandParam, "waiveinterest")) {
            transactionData = this.loanReadPlatformService.retrieveWaiveInterestDetails(loanId);
        } else if (is(commandParam, "writeoff")) {
            transactionData = this.loanReadPlatformService.retrieveLoanWriteoffTemplate(loanId);
        } else if (is(commandParam, "close-rescheduled")) {
            transactionData = this.loanReadPlatformService.retrieveNewClosureDetails();
        } else if (is(commandParam, "close")) {
            transactionData = this.loanReadPlatformService.retrieveNewClosureDetails();
        } else if (is(commandParam, "disburse")) {
            transactionData = this.loanReadPlatformService.retrieveDisbursalTemplate(loanId, true);
            transactionData.setNumberOfRepayments(this.loanReadPlatformService.retrieveNumberOfRepayments(loanId));
            final List<LoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallmentData = this.loanReadPlatformService
                    .getRepaymentDataResponse(loanId);
            transactionData.setLoanRepaymentScheduleInstallments(loanRepaymentScheduleInstallmentData);
        } else if (is(commandParam, "disburseToSavings")) {
            transactionData = this.loanReadPlatformService.retrieveDisbursalTemplate(loanId, false);
        } else if (is(commandParam, "recoverypayment")) {
            transactionData = this.loanReadPlatformService.retrieveRecoveryPaymentTemplate(loanId);
        } else if (is(commandParam, "prepayLoan")) {
            LocalDate transactionDate = null;
            if (transactionDateParam == null) {
                transactionDate = DateUtils.getLocalDateOfTenant();
            } else {
                transactionDate = LocalDate.ofInstant(transactionDateParam.getDate("transactionDate", dateFormat, locale).toInstant(),
                        DateUtils.getDateTimeZoneOfTenant());
            }
            transactionData = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT, loanId,
                    transactionDate);
        } else if (is(commandParam, "refundbycash")) {
            transactionData = this.loanReadPlatformService.retrieveRefundByCashTemplate(loanId);
        } else if (is(commandParam, "refundbytransfer")) {
            transactionData = this.loanReadPlatformService.retrieveDisbursalTemplate(loanId, true);
        } else if (is(commandParam, "foreclosure")) {
            LocalDate transactionDate = null;
            if (transactionDateParam == null) {
                transactionDate = DateUtils.getLocalDateOfTenant();
            } else {
                transactionDate = LocalDate.ofInstant(transactionDateParam.getDate("transactionDate", dateFormat, locale).toInstant(),
                        DateUtils.getDateTimeZoneOfTenant());
            }
            transactionData = this.loanReadPlatformService.retrieveLoanForeclosureTemplate(loanId, transactionDate);
        } else if (is(commandParam, "creditBalanceRefund")) {
            transactionData = this.loanReadPlatformService.retrieveCreditBalanceRefundTemplate(loanId);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, transactionData, this.responseDataParameters);
    }

    @GET
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Transaction Details", description = "Retrieves a Transaction Details\n\n" + "Example Request:\n" + "\n"
            + "loans/5/transactions/3")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.GetLoansLoanIdTransactionsTransactionIdResponse.class))) })
    public String retrieveTransaction(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanTransactionData transactionData = this.loanReadPlatformService.retrieveLoanTransaction(loanId, transactionId);
        transactionData.setLoanChargePaidByList(this.loanChargePaidByReadPlatformService.getLoanChargesPaidByTransactionId(transactionId));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            transactionData = LoanTransactionData.templateOnTop(transactionData, paymentTypeOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, transactionData, this.responseDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Significant Loan Transactions", description = "This API covers the major loan transaction functionality\n\n"
            + "Example Requests:\n" + "\n" + "loans/1/transactions?command=repayment" + " | Make a Repayment | \n"
            + "loans/1/transactions?command=merchantIssuedRefund" + " | Merchant Issued Refund | \n"
            + "loans/1/transactions?command=payoutRefund" + " | Payout Refund | \n" + "loans/1/transactions?command=goodwillCredit"
            + " | Goodwil Credit | \n" + "loans/1/transactions?command=waiveinterest" + " | Waive Interest | \n"
            + "loans/1/transactions?command=writeoff" + " | Write-off Loan | \n" + "loans/1/transactions?command=close-rescheduled"
            + " | Close Rescheduled Loan | \n" + "loans/1/transactions?command=close" + " | Close Loan | \n"
            + "loans/1/transactions?command=undowriteoff" + " | Undo Loan Write-off | \n" + "loans/1/transactions?command=recoverypayment"
            + " | Make Recovery Payment | \n" + "loans/1/transactions?command=refundByCash"
            + " | Make a Refund of an Active Loan by Cash | \n" + "loans/1/transactions?command=foreclosure"
            + " | Foreclosure of an Active Loan | \n" + "loans/1/transactions?command=creditBalanceRefund" + " | Credit Balance Refund"
            + " |  \n")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.PostLoansLoanIdTransactionsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.PostLoansLoanIdTransactionsResponse.class))) })
    public String executeLoanTransaction(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "repayment")) {
            final CommandWrapper commandRequest = builder.loanRepaymentTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "merchantIssuedRefund")) {
            final CommandWrapper commandRequest = builder.loanMerchantIssuedRefundTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "payoutRefund")) {
            final CommandWrapper commandRequest = builder.loanPayoutRefundTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "goodwillCredit")) {
            final CommandWrapper commandRequest = builder.loanGoodwillCreditTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "waiveinterest")) {
            final CommandWrapper commandRequest = builder.waiveInterestPortionTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "writeoff")) {
            final CommandWrapper commandRequest = builder.writeOffLoanTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close-rescheduled")) {
            final CommandWrapper commandRequest = builder.closeLoanAsRescheduledTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeLoanTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undowriteoff")) {
            final CommandWrapper commandRequest = builder.undoWriteOffLoanTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "recoverypayment")) {
            final CommandWrapper commandRequest = builder.loanRecoveryPaymentTransaction(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "refundByCash")) {
            final CommandWrapper commandRequest = builder.refundLoanTransactionByCash(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "foreclosure")) {
            final CommandWrapper commandRequest = builder.loanForeclosure(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "creditBalanceRefund")) {
            final CommandWrapper commandRequest = builder.creditBalanceRefund(loanId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Adjust a Transaction", description = "Note: there is no need to specify command={transactionType} parameter.\n\n"
            + "Mandatory Fields: transactionDate, transactionAmount")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.PostLoansLoanIdTransactionsTransactionIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.PostLoansLoanIdTransactionsTransactionIdResponse.class))) })
    public String adjustLoanTransaction(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        final CommandWrapper commandRequest = builder.adjustTransaction(loanId, transactionId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo a Waive Charge Transaction", description = "Undo a Waive Charge Transaction")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.PutChargeTransactionChangesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanTransactionsApiResourceSwagger.PutChargeTransactionChangesResponse.class))) })
    public String undoWaiveCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().undoWaiveChargeTransaction(loanId, transactionId).build();
        CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
