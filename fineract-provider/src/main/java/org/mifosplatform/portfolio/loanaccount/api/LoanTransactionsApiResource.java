/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.api;

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

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.journalentry.api.DateParam;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.service.LoanReadPlatformService;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/transactions")
@Component
@Scope("singleton")
public class LoanTransactionsApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "type", "date", "currency", "amount",
            "externalId"));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public LoanTransactionsApiResource(final PlatformSecurityContext context, final LoanReadPlatformService loanReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.context = context;
        this.loanReadPlatformService = loanReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTransactionTemplate(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo, @QueryParam("dateFormat") final String dateFormat,
            @QueryParam("transactionDate") final DateParam transactionDateParam, @QueryParam("locale") final String locale) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanTransactionData transactionData = null;
        if (is(commandParam, "repayment")) {
            transactionData = this.loanReadPlatformService.retrieveLoanTransactionTemplate(loanId);
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
        } else if (is(commandParam, "disburseToSavings")) {
            transactionData = this.loanReadPlatformService.retrieveDisbursalTemplate(loanId, false);
        } else if (is(commandParam, "recoverypayment")) {
            transactionData = this.loanReadPlatformService.retrieveRecoveryPaymentTemplate(loanId);
        } else if (is(commandParam, "prepayLoan")) {
            LocalDate transactionDate = null;
            if (transactionDateParam == null) {
                transactionDate = DateUtils.getLocalDateOfTenant();
            } else {
                transactionDate = LocalDate.fromDateFields(transactionDateParam.getDate("transactionDate", dateFormat, locale));
            }
            transactionData = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(loanId, transactionDate);
        } else if (is(commandParam, "refundbycash")) {
            transactionData = this.loanReadPlatformService.retrieveRefundByCashTemplate(loanId);
        } else if (is(commandParam, "refundbytransfer")) {
            transactionData = this.loanReadPlatformService.retrieveDisbursalTemplate(loanId, true);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, transactionData, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTransaction(@PathParam("loanId") final Long loanId, @PathParam("transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanTransactionData transactionData = this.loanReadPlatformService.retrieveLoanTransaction(loanId, transactionId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            transactionData = LoanTransactionData.templateOnTop(transactionData, paymentTypeOptions);
        }

        return this.toApiJsonSerializer.serialize(settings, transactionData, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String executeLoanTransaction(@PathParam("loanId") final Long loanId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "repayment")) {
            final CommandWrapper commandRequest = builder.loanRepaymentTransaction(loanId).build();
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
        }

        if (result == null) { throw new UnrecognizedQueryParamException("command", commandParam); }

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String adjustLoanTransaction(@PathParam("loanId") final Long loanId, @PathParam("transactionId") final Long transactionId,
            final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        final CommandWrapper commandRequest = builder.adjustTransaction(loanId, transactionId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}