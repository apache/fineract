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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.command.JournalEntryCommand;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.data.OfficeOpeningBalancesData;
import org.apache.fineract.accounting.journalentry.service.JournalEntryReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/journalentries")
@Component
@Tag(name = "Journal Entries", description = "A journal entry refers to the logging of transactions against general ledger accounts. A journal entry may consist of several line items, each of which is either a \"debit\" or a \"credit\". The total amount of the debits must equal the total amount of the credits or the journal entry is said to be \"unbalanced\" \n"
        + "\n" + "A journal entry directly changes the account balances on the general ledger")
@RequiredArgsConstructor
public class JournalEntriesApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName", "glAccountName",
            "glAccountId", "glAccountCode", "glAccountType", "transactionDate", "entryType", "amount", "transactionId", "manualEntry",
            "entityType", "entityId", "createdByUserId", "createdDate", "submittedOnDate", "createdByUserName", "comments", "reversed",
            "referenceNumber", "currency", "transactionDetails"));

    private static final String RESOURCE_NAME_FOR_PERMISSION = "JOURNALENTRY";

    private final PlatformSecurityContext context;
    private final JournalEntryReadPlatformService journalEntryReadPlatformService;
    private final DefaultToApiJsonSerializer<Object> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final SqlValidator sqlValidator;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Journal Entries", description = "The list capability of journal entries can support pagination and sorting.\n\n"
            + "Example Requests:\n" + "\n" + "journalentries\n" + "\n" + "journalentries?transactionId=PB37X8Y21EQUY4S\n" + "\n"
            + "journalentries?officeId=1&manualEntriesOnly=true&fromDate=1 July 2013&toDate=15 July 2013&dateFormat=dd MMMM yyyy&locale=en\n"
            + "\n" + "journalentries?fields=officeName,glAccountName,transactionDate\n" + "\n" + "journalentries?offset=10&limit=50\n"
            + "\n" + "journalentries?orderBy=transactionId&sortOrder=DESC\n" + "\n" + "journalentries?runningBalance=true\n" + "\n"
            + "journalentries?transactionDetails=true\n" + "\n" + "journalentries?loanId=12\n" + "\n" + "journalentries?savingsId=24")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = JournalEntriesApiResourceSwagger.GetJournalEntriesTransactionIdResponse.class))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("glAccountId") @Parameter(description = "glAccountId") final Long glAccountId,
            @QueryParam("manualEntriesOnly") @Parameter(description = "manualEntriesOnly") final Boolean onlyManualEntries,
            @QueryParam("fromDate") @Parameter(description = "fromDate") final DateParam fromDateParam,
            @QueryParam("toDate") @Parameter(description = "toDate") final DateParam toDateParam,
            @QueryParam("submittedOnDateFrom") @Parameter(description = "submittedOnDateFrom") final DateParam submittedOnDateFromParam,
            @QueryParam("submittedOnDateTo") @Parameter(description = "submittedOnDateTo") final DateParam submittedOnDateToParam,
            @QueryParam("transactionId") @Parameter(description = "transactionId") final String transactionId,
            @QueryParam("entityType") @Parameter(description = "entityType") final Integer entityType,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("locale") @Parameter(description = "locale") final String locale,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String rawDateFormat,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("savingsId") @Parameter(description = "savingsId") final Long savingsId,
            @QueryParam("runningBalance") @Parameter(description = "runningBalance") final boolean runningBalance,
            @QueryParam("transactionDetails") @Parameter(description = "transactionDetails") final boolean transactionDetails) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final DateFormat dateFormat = StringUtils.isBlank(rawDateFormat) ? null : new DateFormat(rawDateFormat);

        LocalDate fromDate = null;
        if (fromDateParam != null) {
            fromDate = fromDateParam.getDate("fromDate", dateFormat, locale);
        }
        LocalDate toDate = null;
        if (toDateParam != null) {
            toDate = toDateParam.getDate("toDate", dateFormat, locale);
        }

        LocalDate submittedOnDateFrom = null;
        if (submittedOnDateFromParam != null) {
            submittedOnDateFrom = submittedOnDateFromParam.getDate("submittedOnDateFrom", dateFormat, locale);
        }
        LocalDate submittedOnDateTo = null;
        if (submittedOnDateToParam != null) {
            submittedOnDateTo = submittedOnDateToParam.getDate("submittedOnDateTo", dateFormat, locale);
        }

        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).officeId(officeId).offset(offset).orderBy(orderBy)
                .sortOrder(sortOrder).loanId(loanId).savingsId(savingsId).build();
        JournalEntryAssociationParametersData associationParametersData = new JournalEntryAssociationParametersData(transactionDetails,
                runningBalance);

        final Page<JournalEntryData> glJournalEntries = this.journalEntryReadPlatformService.retrieveAll(searchParameters, glAccountId,
                onlyManualEntries, fromDate, toDate, submittedOnDateFrom, submittedOnDateTo, transactionId, entityType,
                associationParametersData);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, glJournalEntries, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{journalEntryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a single Entry", description = "Example Requests:\n" + "\n" + "journalentries/1\n" + "\n" + "\n" + "\n"
            + "journalentries/1?fields=officeName,glAccountId,entryType,amount\n" + "\n" + "journalentries/1?runningBalance=true\n" + "\n"
            + "journalentries/1?transactionDetails=true")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = JournalEntriesApiResourceSwagger.JournalEntryTransactionItem.class))) })
    public String retrieveJournalEntryById(
            @PathParam("journalEntryId") @Parameter(description = "journalEntryId") final Long journalEntryId,
            @Context final UriInfo uriInfo,
            @QueryParam("runningBalance") @Parameter(description = "runningBalance") final boolean runningBalance,
            @QueryParam("transactionDetails") @Parameter(description = "transactionDetails") final boolean transactionDetails) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
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
    @Operation(summary = "Create \"Balanced\" Journal Entries", description = "Note: A Balanced (simple) Journal entry would have atleast one \"Debit\" and one \"Credit\" entry whose amounts are equal \n"
            + "Compound Journal entries may have \"n\" debits and \"m\" credits where both \"m\" and \"n\" are greater than 0 and the net sum or all debits and credits are equal \n\n"
            + "\n" + "Mandatory Fields\n" + "officeId, transactionDate\n\n" + "\ncredits- glAccountId, amount, comments\n\n "
            + "\ndebits-  glAccountId, amount, comments\n\n " + "\n" + "Optional Fields\n"
            + "paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber")
    @RequestBody(content = @Content(schema = @Schema(implementation = JournalEntryCommand.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = JournalEntriesApiResourceSwagger.PostJournalEntriesResponse.class))) })
    public String createGLJournalEntry(@Parameter(hidden = true) final String jsonRequestBody,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {

        CommandProcessingResult result;
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
    @Operation(summary = "Update Running balances for Journal Entries", description = "This API calculates the running balances for office. If office ID not provided this API calculates running balances for all offices. \n"
            + "Mandatory Fields\n" + "officeId")
    @RequestBody(content = @Content(schema = @Schema(implementation = JournalEntriesApiResourceSwagger.PostJournalEntriesTransactionIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = JournalEntriesApiResourceSwagger.PostJournalEntriesTransactionIdResponse.class))) })
    public String createReversalJournalEntry(@Parameter(hidden = true) final String jsonRequestBody,
            @PathParam("transactionId") @Parameter(description = "transactionId") final String transactionId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam) {
        CommandProcessingResult result;
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
        String transactionId = "P" + entryId;
        SearchParameters params = SearchParameters.builder().limit(limit).offset(offset).build();
        Page<JournalEntryData> entries = this.journalEntryReadPlatformService.retrieveAll(params, null, null, null, null, null, null,
                transactionId, PortfolioProductType.PROVISIONING.getValue(), null);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, entries, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("openingbalance")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOpeningBalance(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
            @QueryParam("currencyCode") final String currencyCode) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);
        final OfficeOpeningBalancesData officeOpeningBalancesData = this.journalEntryReadPlatformService
                .retrieveOfficeOpeningBalances(officeId, currencyCode);
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
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.GL_JOURNAL_ENTRIES.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload journal entries template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postJournalEntriesTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.GL_JOURNAL_ENTRIES.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.apiJsonSerializerService.serialize(importDocumentId);
    }
}
