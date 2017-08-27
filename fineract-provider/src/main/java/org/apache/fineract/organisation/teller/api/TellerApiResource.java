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

import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.data.CashierTransactionData;
import org.apache.fineract.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.apache.fineract.organisation.teller.data.TellerData;
import org.apache.fineract.organisation.teller.data.TellerJournalData;
import org.apache.fineract.organisation.teller.data.TellerTransactionData;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.organisation.teller.util.DateRange;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("tellers")
@Component
@Scope("singleton")
@Api(value = "Teller Cash Management", description = "Teller cash management which will allow an organization to manage their cash transactions at branches or head office more effectively.")
public class TellerApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<TellerData> jsonSerializer;
    private final TellerManagementReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;

    @Autowired
    public TellerApiResource(PlatformSecurityContext securityContext, DefaultToApiJsonSerializer<TellerData> jsonSerializer,
            TellerManagementReadPlatformService readPlatformService,
            PortfolioCommandSourceWritePlatformService commandWritePlatformService) {
        super();
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.readPlatformService = readPlatformService;
        this.commandWritePlatformService = commandWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all tellers", notes = "Retrieves list tellers")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersResponse.class, responseContainer = "List")})
    public String getTellerData(@QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId) {
        final Collection<TellerData> foundTellers = this.readPlatformService.getTellers(officeId);

        return this.jsonSerializer.serialize(foundTellers);
    }

    @GET
    @Path("{tellerId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve tellers", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersResponse.class)})
    public String findTeller(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId) {
        final TellerData teller = this.readPlatformService.findTeller(tellerId);

        return this.jsonSerializer.serialize(teller);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create teller", notes = "Mandatory Fields\n" + "Teller name, OfficeId, Description, Start Date, Status\n" + "Optional Fields\n" + "End Date")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TellerApiResourceSwagger.PostTellersRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.PostTellersResponse.class)})
    public String createTeller(@ApiParam(hidden = true) final String tellerData) {
        final CommandWrapper request = new CommandWrapperBuilder().createTeller().withJson(tellerData).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{tellerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update teller", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TellerApiResourceSwagger.PutTellersRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.PutTellersResponse.class)})
    public String updateTeller(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @ApiParam(hidden = true) final String tellerData) {
        final CommandWrapper request = new CommandWrapperBuilder().updateTeller(tellerId).withJson(tellerData).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{tellerId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteTeller(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId) {
        final CommandWrapper request = new CommandWrapperBuilder().deleteTeller(tellerId).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);
    }

    @GET
    @Path("{tellerId}/cashiers")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List Cashiers", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersTellerIdCashiersResponse.class)})
    public String getCashierData(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @QueryParam("fromdate") @ApiParam(value = "fromdate") final String fromDateStr,
            @QueryParam("todate") @ApiParam(value = "todate") final String toDateStr) {
        final DateTimeFormatter dateFormatter = ISODateTimeFormat.basicDate();

        final Date fromDate = (fromDateStr != null ? dateFormatter.parseDateTime(fromDateStr).toDate() : new Date());
        final Date toDate = (toDateStr != null ? dateFormatter.parseDateTime(toDateStr).toDate() : new Date());

        final TellerData teller = this.readPlatformService.findTeller(tellerId);
        final Collection<CashierData> cashiers = this.readPlatformService.getCashiersForTeller(tellerId, fromDate, toDate);

        CashiersForTeller cashiersForTeller = new CashiersForTeller();
        cashiersForTeller.cashiers = cashiers;
        cashiersForTeller.tellerId = tellerId;
        cashiersForTeller.tellerName = teller.getName();
        cashiersForTeller.officeId = teller.getOfficeId();
        cashiersForTeller.officeName = teller.getOfficeName();

        return this.jsonSerializer.serialize(cashiersForTeller);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve a cashier", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashierIdResponse.class)})
    public String findCashierData(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId) {
        final CashierData cashier = this.readPlatformService.findCashier(cashierId);

        return this.jsonSerializer.serialize(cashier);
    }

    @GET
    @Path("{tellerId}/cashiers/template")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Find Cashiers", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersTellerIdCashiersTemplateResponse.class)})
    public String getCashierTemplate(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId) {

        final TellerData teller = this.readPlatformService.findTeller(tellerId);
        Long officeId = teller.getOfficeId();

        final CashierData cashier = this.readPlatformService.retrieveCashierTemplate(officeId, tellerId, true);

        return this.jsonSerializer.serialize(cashier);
    }

    @POST
    @Path("{tellerId}/cashiers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Cashiers", notes = "Mandatory Fields: \n" + "Cashier/staff, Fromm Date, To Date, Full Day or From time and To time\n" + "\n\n\n" + "Optional Fields: \n" + "Description/Notes")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TellerApiResourceSwagger.PostTellersTellerIdCashiersRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.PostTellersTellerIdCashiersResponse.class)})
    public String createCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @ApiParam(hidden = true) final String cashierData) {
        final CommandWrapper request = new CommandWrapperBuilder().allocateTeller(tellerId).withJson(cashierData).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{tellerId}/cashiers/{cashierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Cashier", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TellerApiResourceSwagger.PutTellersTellerIdCashiersCashierIdRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.PutTellersTellerIdCashiersCashierIdResponse.class)})
    public String updateCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId,
            @ApiParam(hidden = true) final String cashierDate) {
        final CommandWrapper request = new CommandWrapperBuilder().updateAllocationTeller(tellerId, cashierId).withJson(cashierDate)
                .build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{tellerId}/cashiers/{cashierId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete Cashier", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.DeleteTellersTellerIdCashiersCashierIdResponse.class)})
    public String deleteCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId) {
        final CommandWrapper request = new CommandWrapperBuilder().deleteAllocationTeller(tellerId, cashierId).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);
    }

    @POST
    @Path("{tellerId}/cashiers/{cashierId}/allocate")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Allocate Cash To Cashier", notes = "Mandatory Fields: \n" + "Date, Amount, Currency, Notes/Comments")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdAllocateRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdAllocateResponse.class)})
    public String allocateCashToCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId,
            @ApiParam(hidden = true) final String cashierTxnData) {
        final CommandWrapper request = new CommandWrapperBuilder().allocateCashToCashier(tellerId, cashierId).withJson(cashierTxnData)
                .build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);

    }

    @POST
    @Path("{tellerId}/cashiers/{cashierId}/settle")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Settle Cash From Cashier", notes = "Mandatory Fields\n" + "Date, Amount, Currency, Notes/Comments")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdSettleRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.PostTellersTellerIdCashiersCashierIdSettleResponse.class)})
    public String settleCashFromCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId,
            @ApiParam(hidden = true) final String cashierTxnData) {
        final CommandWrapper request = new CommandWrapperBuilder().settleCashFromCashier(tellerId, cashierId).withJson(cashierTxnData)
                .build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(request);

        return this.jsonSerializer.serialize(result);

    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Cashier Transaction", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashiersIdTransactionsResponse.class, responseContainer = "List")})
    public String getTransactionsForCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId,
            @QueryParam("currencyCode") @ApiParam(value = "currencyCode") final String currencyCode, @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {
        final TellerData teller = this.readPlatformService.findTeller(tellerId);
        final CashierData cashier = this.readPlatformService.findCashier(cashierId);

        final Date fromDate = null;
        final Date toDate = null;
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);
        final Page<CashierTransactionData> cashierTxns = this.readPlatformService.retrieveCashierTransactions(cashierId, false,
                fromDate, toDate, currencyCode, searchParameters);

        return this.jsonSerializer.serialize(cashierTxns);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}/summaryandtransactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Transactions Wtih Summary For Cashier", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse.class)})
    public String getTransactionsWtihSummaryForCashier(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId,
            @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId, @QueryParam("currencyCode") @ApiParam(value = "currencyCode") final String currencyCode,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder) {
        final TellerData teller = this.readPlatformService.findTeller(tellerId);
        final CashierData cashier = this.readPlatformService.findCashier(cashierId);

        final Date fromDate = null;
        final Date toDate = null;
        
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);

        final CashierTransactionsWithSummaryData cashierTxnWithSummary = this.readPlatformService.retrieveCashierTransactionsWithSummary(
                cashierId, false, fromDate, toDate, currencyCode, searchParameters);

        return this.jsonSerializer.serialize(cashierTxnWithSummary);
    }

    @GET
    @Path("{tellerId}/cashiers/{cashierId}/transactions/template")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Cashier Transaction Template", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = TellerApiResourceSwagger.GetTellersTellerIdCashiersCashiersIdTransactionsTemplateResponse.class)})
    public String getCashierTxnTemplate(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @PathParam("cashierId") @ApiParam(value = "cashierId") final Long cashierId) {

        final CashierTransactionData cashierTxnTemplate = this.readPlatformService.retrieveCashierTxnTemplate(cashierId);

        return this.jsonSerializer.serialize(cashierTxnTemplate);
    }

    @GET
    @Path("{tellerId}/transactions")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String getTransactionData(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @QueryParam("dateRange") @ApiParam(value = "dateRange") final String dateRange) {
        final DateRange dateRangeHolder = DateRange.fromString(dateRange);

        final Collection<TellerTransactionData> transactions = this.readPlatformService.fetchTellerTransactionsByTellerId(tellerId,
                dateRangeHolder.getStartDate(), dateRangeHolder.getEndDate());

        return this.jsonSerializer.serialize(transactions);
    }

    @GET
    @Path("{tellerId}/transactions/{transactionId}")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String findTransactionData(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerid, @PathParam("transactionId") @ApiParam(value = "transactionId") final Long transactionId) {
        final TellerTransactionData transaction = this.readPlatformService.findTellerTransaction(transactionId);

        return this.jsonSerializer.serialize(transaction);
    }

    @GET
    @Path("{tellerId}/journals")
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String getJournalData(@PathParam("tellerId") @ApiParam(value = "tellerId") final Long tellerId, @QueryParam("cashierId") @ApiParam(value = "cashierId") final Long cashierDate,
            @QueryParam("dateRange") @ApiParam(value = "dateRange") final String dateRange) {
        final DateRange dateRangeHolder = DateRange.fromString(dateRange);

        final Collection<TellerJournalData> journals = this.readPlatformService.fetchTellerJournals(tellerId, cashierDate,
                dateRangeHolder.getStartDate(), dateRangeHolder.getEndDate());

        return this.jsonSerializer.serialize(journals);
    }

    private class CashiersForTeller {

        public Long tellerId;
        public String tellerName;
        public Long officeId;
        public String officeName;
        public Collection<CashierData> cashiers;

    }
}
