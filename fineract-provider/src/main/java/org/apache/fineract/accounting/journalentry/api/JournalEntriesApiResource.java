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
package org.apache.fineract.accounting.journalentry.api;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.*;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.journalentry.command.JournalEntryCommand;
import org.apache.fineract.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.data.OfficeOpeningBalancesData;
import org.apache.fineract.accounting.journalentry.service.JournalEntryReadPlatformService;
import org.apache.fineract.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/journalentries")
@Component
@Scope("singleton")
@Api(value = "Journal Entries", description = "A journal entry refers to the logging of transactions against general ledger accounts. A journal entry may consist of several line items, each of which is either a \"debit\" or a \"credit\". The total amount of the debits must equal the total amount of the credits or the journal entry is said to be \"unbalanced\" \n" + "\n" + "A journal entry directly changes the account balances on the general ledger")
public class JournalEntriesApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName",
            "glAccountName", "glAccountId", "glAccountCode", "glAccountType", "transactionDate", "entryType", "amount", "transactionId",
            "manualEntry", "entityType", "entityId", "createdByUserId", "createdDate", "createdByUserName", "comments", "reversed",
            "referenceNumber", "currency", "transactionDetails"));

    private final String resourceNameForPermission = "JOURNALENTRY";

    private final JournalEntryReadPlatformService journalEntryReadPlatformService;
    private final DefaultToApiJsonSerializer<Object> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;


    @Autowired
    public JournalEntriesApiResource(final PlatformSecurityContext context,
            final JournalEntryReadPlatformService journalEntryReadPlatformService,
            final DefaultToApiJsonSerializer<Object> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.journalEntryReadPlatformService = journalEntryReadPlatformService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Journal Entries", notes = "The list capability of journal entries can support pagination and sorting.\n\n" + "Example Requests:\n" + "\n" + "journalentries\n" + "\n" + "journalentries?transactionId=PB37X8Y21EQUY4S\n" + "\n" + "journalentries?officeId=1&manualEntriesOnly=true&fromDate=1 July 2013&toDate=15 July 2013&dateFormat=dd MMMM yyyy&locale=en\n" + "\n" + "journalentries?fields=officeName,glAccountName,transactionDate\n" + "\n" + "journalentries?offset=10&limit=50\n" + "\n" + "journalentries?orderBy=transactionId&sortOrder=DESC\n" + "\n" + "journalentries?runningBalance=true\n" + "\n" + "journalentries?transactionDetails=true\n" + "\n" + "journalentries?loanId=12\n" + "\n" + "journalentries?savingsId=24")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = JournalEntryData.class, responseContainer = "list")})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId,
            @QueryParam("glAccountId") @ApiParam(value = "glAccountId") final Long glAccountId, @QueryParam("manualEntriesOnly") @ApiParam(value = "manualEntriesOnly") final Boolean onlyManualEntries,
            @QueryParam("fromDate") @ApiParam(value = "fromDate") final DateParam fromDateParam, @QueryParam("toDate") @ApiParam(value = "toDate") final DateParam toDateParam,
            @QueryParam("transactionId") @ApiParam(value = "transactionId") final String transactionId, @QueryParam("entityType") @ApiParam(value = "entityType") final Integer entityType,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder,
            @QueryParam("locale") @ApiParam(value = "locale") final String locale, @QueryParam("dateFormat") @ApiParam(value = "dateFormat") final String dateFormat,
            @QueryParam("loanId") @ApiParam(value = "loanId") final Long loanId, @QueryParam("savingsId") @ApiParam(value = "savingsId") final Long savingsId,
            @QueryParam("runningBalance") @ApiParam(value = "runningBalance") final boolean runningBalance, @QueryParam("transactionDetails") @ApiParam(value = "transactionDetails") final boolean transactionDetails) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);

        Date fromDate = null;
        if (fromDateParam != null) {
            fromDate = fromDateParam.getDate("fromDate", dateFormat, locale);
        }
        Date toDate = null;
        if (toDateParam != null) {
            toDate = toDateParam.getDate("toDate", dateFormat, locale);
        }

        final SearchParameters searchParameters = SearchParameters.forJournalEntries(officeId, offset, limit, orderBy, sortOrder, loanId,
                savingsId);
        JournalEntryAssociationParametersData associationParametersData = new JournalEntryAssociationParametersData(transactionDetails,
                runningBalance);

        final Page<JournalEntryData> glJournalEntries = this.journalEntryReadPlatformService.retrieveAll(searchParameters, glAccountId,
                onlyManualEntries, fromDate, toDate, transactionId, entityType, associationParametersData);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glJournalEntries, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{journalEntryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a single Entry", notes = "Example Requests:\n" + "\n" + "journalentries/1\n" + "\n" + "\n" + "\n" + "journalentries/1?fields=officeName,glAccountId,entryType,amount\n" + "\n" + "journalentries/1?runningBalance=true\n" + "\n" + "journalentries/1?transactionDetails=true")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = JournalEntryData.class)})
    public String retreiveJournalEntryById(@PathParam("journalEntryId") @ApiParam(value = "journalEntryId") final Long journalEntryId, @Context final UriInfo uriInfo,
            @QueryParam("runningBalance") @ApiParam(value = "runningBalance") final boolean runningBalance, @QueryParam("transactionDetails") @ApiParam(value = "transactionDetails") final boolean transactionDetails) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
        JournalEntryAssociationParametersData associationParametersData = new JournalEntryAssociationParametersData(transactionDetails,
                runningBalance);
        final JournalEntryData glJournalEntryData = this.journalEntryReadPlatformService.retrieveGLJournalEntryById(journalEntryId,
                associationParametersData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glJournalEntryData, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create \"Balanced\" Journal Entries", notes = "Note: A Balanced (simple) Journal entry would have atleast one \"Debit\" and one \"Credit\" entry whose amounts are equal \n" + "Compound Journal entries may have \"n\" debits and \"m\" credits where both \"m\" and \"n\" are greater than 0 and the net sum or all debits and credits are equal \n\n" + "\n" + "Mandatory Fields\n" + "officeId, transactionDate\n\n" + "\ncredits- glAccountId, amount, comments\n\n " + "\ndebits-  glAccountId, amount, comments\n\n " + "\n" + "Optional Fields\n" + "paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", value = "body", dataType = "body", dataTypeClass = JournalEntryCommand.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = JournalEntriesApiResourceSwagger.PostJournalEntriesResponse.class)})
    public String createGLJournalEntry(@ApiParam(hidden = true) final String jsonRequestBody, @QueryParam("command") @ApiParam(value = "command") final String commandParam) {

        CommandProcessingResult result = null;
        if (is(commandParam, "updateRunningBalance")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRunningBalanceForJournalEntry()
                    .withJson(jsonRequestBody).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "defineOpeningBalance")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().defineOpeningBalanceForJournalEntry()
                    .withJson(jsonRequestBody).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().createJournalEntry().withJson(jsonRequestBody).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }
        return this.apiJsonSerializerService.serialize(result);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update Running balances for Journal Entries", notes = "This API calculates the running balances for office. If office ID not provided this API calculates running balances for all offices. \n" + "Mandatory Fields\n" + "officeId")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "body", value = "body", dataType = "body", dataTypeClass = JournalEntriesApiResourceSwagger.PostJournalEntriesTransactionIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = JournalEntriesApiResourceSwagger.PostJournalEntriesTransactionIdResponse.class)})
    public String createReversalJournalEntry(@ApiParam(hidden = true) final String jsonRequestBody, @PathParam("transactionId") @ApiParam(value = "transactionId") final String transactionId,
            @QueryParam("command") @ApiParam(value = "command") final String commandParam) {
        CommandProcessingResult result = null;
        if (is(commandParam, "reverse")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().reverseJournalEntry(transactionId).withJson(jsonRequestBody)
                    .build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        return this.apiJsonSerializerService.serialize(result);
    }

    @GET
    @Path("provisioning")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveJournalEntries(@QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit,
            @QueryParam("entryId") final Long entryId, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser();
        String transactionId = "P"+entryId ;
        SearchParameters params = SearchParameters.forPagination(offset, limit) ;
                Page<JournalEntryData> entries = this.journalEntryReadPlatformService.retrieveAll(params, null, null, null, null, transactionId, PortfolioProductType.PROVISIONING.getValue(), null) ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, entries, RESPONSE_DATA_PARAMETERS);
    }

    
    @GET
    @Path("openingbalance")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOpeningBalance(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("currencyCode") final String currencyCode) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermission);
        final OfficeOpeningBalancesData officeOpeningBalancesData = this.journalEntryReadPlatformService.retrieveOfficeOpeningBalances(
                officeId, currencyCode);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, officeOpeningBalancesData);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getJournalEntriesTemplate(@QueryParam("officeId") final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.GL_JOURNAL_ENTRIES.toString(),
                officeId,null,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postJournalEntriesTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId =this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.GL_JOURNAL_ENTRIES.toString(),
                uploadedInputStream,fileDetail, locale,dateFormat);
        return this.apiJsonSerializerService.serialize(importDocumentId);
    }
}