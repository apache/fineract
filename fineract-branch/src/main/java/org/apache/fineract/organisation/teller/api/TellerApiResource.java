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
package org.apache.fineract.organisation.teller.api;

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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.data.CashierTransactionData;
import org.apache.fineract.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.apache.fineract.organisation.teller.data.TellerData;
import org.apache.fineract.organisation.teller.data.TellerJournalData;
import org.apache.fineract.organisation.teller.data.TellerTransactionData;
import org.apache.fineract.organisation.teller.domain.model.CashiersForTeller;
import org.apache.fineract.organisation.teller.domain.model.request.CashierRequest;
import org.apache.fineract.organisation.teller.domain.model.request.CashierTransactionRequest;
import org.apache.fineract.organisation.teller.domain.model.request.TellerRequest;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.organisation.teller.util.DateRange;
import org.springframework.stereotype.Component;

@Path("/v1/tellers")
@Component
@Tag(name = "Teller Cash Management", description = "Teller cash management which will allow an organization to manage their cash transactions at branches or head office more effectively.")
@RequiredArgsConstructor
public class TellerApiResource {

    private final TellerManagementReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final DefaultToApiJsonSerializer<String> apiJsonSerializer;

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List all tellers", description = "Retrieves list tellers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersResponse.class)))) })
    public Collection<TellerData> getTellerData(@QueryParam("officeId") @Parameter(description = "officeId") final Long officeId) {
        return readPlatformService.getTellers(officeId);
    }

    @GET
    @Path("{tellerId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve tellers", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersResponse.class))) })
    public TellerData findTeller(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId) {
        return readPlatformService.findTeller(tellerId);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create teller", description = "Mandatory Fields\n" + "Teller name, OfficeId, Description, Start Date, Status\n"
            + "Optional Fields\n" + "End Date")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersResponse.class))) })
    public CommandProcessingResult createTeller(@Parameter(hidden = true) TellerRequest tellerData) {
        final CommandWrapper request = new CommandWrapperBuilder().createTeller().withJson(apiJsonSerializer.serialize(tellerData)).build();

        return this.commandWritePlatformService.logCommandSource(request);
    }

    @PUT
    @Path("{tellerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update teller", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PutTellersRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PutTellersResponse.class))) })
    public CommandProcessingResult updateTeller(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @Parameter(hidden = true) TellerRequest tellerData) {
        final CommandWrapper request = new CommandWrapperBuilder().updateTeller(tellerId).withJson(apiJsonSerializer.serialize(tellerData))
                .build();

        return this.commandWritePlatformService.logCommandSource(request);
    }

    @DELETE
    @Path("{tellerId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public CommandProcessingResult deleteTeller(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId) {
        final CommandWrapper request = new CommandWrapperBuilder().deleteTeller(tellerId).build();
        return this.commandWritePlatformService.logCommandSource(request);
    }

    @GET
    @Path("{tellerId}/cashiers")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Cashiers", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersTellerIdCashiersResponse.class))) })
    public CashiersForTeller getCashierData(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @QueryParam("fromdate") @Parameter(description = "fromdate") final String fromDateStr,
            @QueryParam("todate") @Parameter(description = "todate") final String toDateStr) {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;

        final LocalDate fromDate = fromDateStr != null ? LocalDate.parse(fromDateStr, dateFormatter) : DateUtils.getBusinessLocalDate();
        final LocalDate toDate = toDateStr != null ? LocalDate.parse(toDateStr, dateFormatter) : DateUtils.getBusinessLocalDate();

        final TellerData teller = this.readPlatformService.findTeller(tellerId);
        final Collection<CashierData> cashiers = this.readPlatformService.getCashiersForTeller(tellerId, fromDate, toDate);

        return new CashiersForTeller(tellerId, teller.getName(), teller.getOfficeId(), teller.getOfficeName(), cashiers);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a cashier", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashierIdResponse.class))) })
    public CashierData findCashierData(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId) {
        return readPlatformService.findCashier(cashierId);
    }

    @GET
    @Path("{tellerId}/cashiers/template")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Find Cashiers", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersTellerIdCashiersTemplateResponse.class))) })
    public CashierData getCashierTemplate(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId) {

        final TellerData teller = this.readPlatformService.findTeller(tellerId);
        Long officeId = teller.getOfficeId();

        return this.readPlatformService.retrieveCashierTemplate(officeId, tellerId, true);
    }

    @POST
    @Path("{tellerId}/cashiers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create Cashiers", description = "Mandatory Fields: \n"
            + "Cashier/staff, Fromm Date, To Date, Full Day or From time and To time\n" + "\n\n\n" + "Optional Fields: \n"
            + "Description/Notes")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersTellerIdCashiersRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersTellerIdCashiersResponse.class))) })
    public CommandProcessingResult createCashier(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @Parameter(hidden = true) final CashierRequest cashierData) {
        final CommandWrapper request = new CommandWrapperBuilder().allocateTeller(tellerId)
                .withJson(apiJsonSerializer.serialize(cashierData)).build();

        return this.commandWritePlatformService.logCommandSource(request);
    }

    @PUT
    @Path("{tellerId}/cashiers/{cashierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update Cashier", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PutTellersTellerIdCashiersCashierIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PutTellersTellerIdCashiersCashierIdResponse.class))) })
    public CommandProcessingResult updateCashier(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @Parameter(hidden = true) final CashierRequest cashierDate) {
        final CommandWrapper request = new CommandWrapperBuilder().updateAllocationTeller(tellerId, cashierId)
                .withJson(apiJsonSerializer.serialize(cashierDate)).build();

        return this.commandWritePlatformService.logCommandSource(request);

    }

    @DELETE
    @Path("{tellerId}/cashiers/{cashierId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete Cashier", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.DeleteTellersTellerIdCashiersCashierIdResponse.class))) })
    public CommandProcessingResult deleteCashier(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId) {
        final CommandWrapper request = new CommandWrapperBuilder().deleteAllocationTeller(tellerId, cashierId).build();

        return this.commandWritePlatformService.logCommandSource(request);

    }

    @POST
    @Path("{tellerId}/cashiers/{cashierId}/allocate")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Allocate Cash To Cashier", description = "Mandatory Fields: \n" + "Date, Amount, Currency, Notes/Comments")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdAllocateRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdAllocateResponse.class))) })
    public CommandProcessingResult allocateCashToCashier(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @Parameter(hidden = true) CashierTransactionRequest cashierTxnData) {
        final CommandWrapper request = new CommandWrapperBuilder().allocateCashToCashier(tellerId, cashierId)
                .withJson(apiJsonSerializer.serialize(cashierTxnData)).build();

        return this.commandWritePlatformService.logCommandSource(request);

    }

    @POST
    @Path("{tellerId}/cashiers/{cashierId}/settle")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Settle Cash From Cashier", description = "Mandatory Fields\n" + "Date, Amount, Currency, Notes/Comments")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdSettleRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdSettleResponse.class))) })
    public CommandProcessingResult settleCashFromCashier(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @Parameter(hidden = true) CashierTransactionRequest cashierTxnData) {
        final CommandWrapper request = new CommandWrapperBuilder().settleCashFromCashier(tellerId, cashierId)
                .withJson(apiJsonSerializer.serialize(cashierTxnData)).build();

        return this.commandWritePlatformService.logCommandSource(request);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Cashier Transactions", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashiersIdTransactionsResponse.class))) })
    public Page<CashierTransactionData> getTransactionsForCashier(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @QueryParam("currencyCode") @Parameter(description = "currencyCode") final String currencyCode,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();
        return this.readPlatformService.retrieveCashierTransactions(cashierId, false, null, null, currencyCode, searchParameters);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}/summaryandtransactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Transactions With Summary For Cashier", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse.class))) })
    public CashierTransactionsWithSummaryData getTransactionsWithSummaryForCashier(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId,
            @QueryParam("currencyCode") @Parameter(description = "currencyCode") final String currencyCode,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).build();

        return this.readPlatformService.retrieveCashierTransactionsWithSummary(cashierId, false, null, null, currencyCode,
                searchParameters);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}/transactions/template")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Cashier Transaction Template", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashiersIdTransactionsTemplateResponse.class))) })
    public CashierTransactionData getCashierTxnTemplate(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @PathParam("cashierId") @Parameter(description = "cashierId") final Long cashierId) {

        return this.readPlatformService.retrieveCashierTxnTemplate(cashierId);
    }

    @GET
    @Path("{tellerId}/transactions")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<TellerTransactionData> getTransactionData(
            @PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @QueryParam("dateRange") @Parameter(description = "dateRange") final String dateRange) {
        final DateRange dateRangeHolder = DateRange.fromString(dateRange);

        return this.readPlatformService.fetchTellerTransactionsByTellerId(tellerId, dateRangeHolder.getStartDate(),
                dateRangeHolder.getEndDate());
    }

    @GET
    @Path("{tellerId}/transactions/{transactionId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public TellerTransactionData findTransactionData(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerid,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId) {
        return this.readPlatformService.findTellerTransaction(transactionId);
    }

    @GET
    @Path("{tellerId}/journals")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<TellerJournalData> getJournalData(@PathParam("tellerId") @Parameter(description = "tellerId") final Long tellerId,
            @QueryParam("cashierId") @Parameter(description = "cashierId") final Long cashierDate,
            @QueryParam("dateRange") @Parameter(description = "dateRange") final String dateRange) {
        final DateRange dateRangeHolder = DateRange.fromString(dateRange);

        return this.readPlatformService.fetchTellerJournals(tellerId, cashierDate, dateRangeHolder.getStartDate(),
                dateRangeHolder.getEndDate());
    }
}
