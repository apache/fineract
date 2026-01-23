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
package org.apache.fineract.accounting.reconciliation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.reconciliation.data.BankStatementImportData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationRuleData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationSummaryData;
import org.apache.fineract.accounting.reconciliation.data.UnreconciledGLEntryData;
import org.apache.fineract.accounting.reconciliation.service.ReconciliationReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/accounting/reconciliation")
@Component
@Tag(name = "Account Reconciliation", description = """
        The Account Reconciliation module enables reconciliation of bank statements with GL account entries.
        It supports importing bank transactions, automatic and manual matching of transactions,
        adjustments, and completion/approval workflows.
        """)
@RequiredArgsConstructor
public class ReconciliationApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSION = "ACCOUNTRECONCILIATION";

    private final PlatformSecurityContext context;
    private final ReconciliationReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "List Reconciliations", description = """
            Retrieve a list of all reconciliations with optional filters.
            
            Example Requests:
            
            accounting/reconciliation
            
            accounting/reconciliation?glAccountId=1
            
            accounting/reconciliation?status=PENDING&offset=0&limit=10
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public Page<BankStatementImportData> retrieveAll(@QueryParam("glAccountId") @Parameter(description = "GL Account ID") final Long glAccountId,
            @QueryParam("fromDate") @Parameter(description = "From Date") final String fromDateStr,
            @QueryParam("toDate") @Parameter(description = "To Date") final String toDateStr,
            @QueryParam("status") @Parameter(description = "Status") final String status,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final LocalDate fromDate = fromDateStr != null ? LocalDate.parse(fromDateStr, DateUtils.DEFAULT_DATE_FORMATTER) : null;
        final LocalDate toDate = toDateStr != null ? LocalDate.parse(toDateStr, DateUtils.DEFAULT_DATE_FORMATTER) : null;

        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).build();
        return this.readPlatformService.retrieveAll(glAccountId, fromDate, toDate, status, searchParameters);
    }

    @GET
    @Path("{importId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Retrieve a Reconciliation", description = """
            Retrieve details of a specific reconciliation import.
            
            Example Request:
            
            accounting/reconciliation/1
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public BankStatementImportData retrieveOne(@PathParam("importId") @Parameter(description = "importId") final Long importId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        return this.readPlatformService.retrieveOne(importId);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Create Reconciliation Import", description = """
            Create a new reconciliation import for a GL account.
            
            Mandatory Fields:
            glAccountId, importDate
            
            Example Request:
            
            {
              "glAccountId": 1,
              "importDate": "2024-01-15",
              "description": "January 2024 Bank Statement"
            }
            """)
    @RequestBody(required = true)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult createImport(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createReconciliationImport()
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("{importId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Import Bank Transactions", description = """
            Import bank transactions for a reconciliation import.
            
            Mandatory Fields:
            transactions (array)
            
            Example Request:
            
            {
              "transactions": [
                {
                  "transactionDate": "2024-01-10",
                  "description": "Payment received",
                  "amount": 1000.00,
                  "type": "CREDIT"
                }
              ]
            }
            """)
    @RequestBody(required = true)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult importTransactions(@PathParam("importId") @Parameter(description = "importId") final Long importId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().importReconciliationTransactions(importId)
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("{importId}/auto-match")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Auto-Match Transactions", description = """
            Automatically match bank transactions with GL entries using configured rules.
            
            Example Request:
            
            POST accounting/reconciliation/1/auto-match
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult autoMatch(@PathParam("importId") @Parameter(description = "importId") final Long importId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().autoMatchReconciliation(importId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("{importId}/matches")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Create Manual Match", description = """
            Manually create a match between a bank transaction and GL entry.
            
            Mandatory Fields:
            bankTransactionId, glEntryId
            
            Example Request:
            
            {
              "bankTransactionId": 1,
              "glEntryId": 100
            }
            """)
    @RequestBody(required = true)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult createMatch(@PathParam("importId") @Parameter(description = "importId") final Long importId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createReconciliationMatch(importId)
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("{importId}/matches/{matchId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Remove Match", description = """
            Remove a reconciliation match.
            
            Example Request:
            
            DELETE accounting/reconciliation/1/matches/5
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult removeMatch(@PathParam("importId") @Parameter(description = "importId") final Long importId,
            @PathParam("matchId") @Parameter(description = "matchId") final Long matchId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().removeReconciliationMatch(importId, matchId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("{importId}/adjustments")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Create Adjustment", description = """
            Create a reconciliation adjustment entry.
            
            Mandatory Fields:
            adjustmentDate, amount, type, description
            
            Example Request:
            
            {
              "adjustmentDate": "2024-01-15",
              "amount": 50.00,
              "type": "OUTSTANDING_CHEQUE",
              "description": "Cheque #12345 not yet cleared"
            }
            """)
    @RequestBody(required = true)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult createAdjustment(@PathParam("importId") @Parameter(description = "importId") final Long importId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createReconciliationAdjustment(importId)
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("{importId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Complete or Approve Reconciliation", description = """
            Complete or approve a reconciliation import.
            
            Supported Commands:
            - complete: Mark reconciliation as complete
            - approve: Approve a completed reconciliation
            
            Example Requests:
            
            POST accounting/reconciliation/1?command=complete
            
            POST accounting/reconciliation/1?command=approve
            """)
    @RequestBody(required = false)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult handleCommand(@PathParam("importId") @Parameter(description = "importId") final Long importId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, final String apiRequestBodyAsJson) {

        CommandWrapper commandRequest = null;

        if ("complete".equalsIgnoreCase(commandParam)) {
            commandRequest = new CommandWrapperBuilder().completeReconciliation(importId).withJson(apiRequestBodyAsJson).build();
        } else if ("approve".equalsIgnoreCase(commandParam)) {
            commandRequest = new CommandWrapperBuilder().approveReconciliation(importId).withJson(apiRequestBodyAsJson).build();
        }

        if (commandRequest == null) {
            throw new IllegalArgumentException(
                    "Unsupported command: " + commandParam + ". Supported commands are: complete, approve");
        }

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("{importId}/summary")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Get Reconciliation Summary", description = """
            Retrieve summary information for a reconciliation import.
            
            Example Request:
            
            accounting/reconciliation/1/summary
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public ReconciliationSummaryData retrieveSummary(@PathParam("importId") @Parameter(description = "importId") final Long importId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        return this.readPlatformService.retrieveSummary(importId);
    }

    @GET
    @Path("unreconciled-entries")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Get Unreconciled GL Entries", description = """
            Retrieve unreconciled GL entries for a GL account.
            
            Mandatory Parameters:
            glAccountId
            
            Example Requests:
            
            accounting/reconciliation/unreconciled-entries?glAccountId=1
            
            accounting/reconciliation/unreconciled-entries?glAccountId=1&fromDate=2024-01-01&toDate=2024-01-31
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public List<UnreconciledGLEntryData> retrieveUnreconciledEntries(
            @QueryParam("glAccountId") @Parameter(description = "glAccountId") final Long glAccountId,
            @QueryParam("fromDate") @Parameter(description = "fromDate") final String fromDateStr,
            @QueryParam("toDate") @Parameter(description = "toDate") final String toDateStr) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        final LocalDate fromDate = fromDateStr != null ? LocalDate.parse(fromDateStr, DateUtils.DEFAULT_DATE_FORMATTER) : null;
        final LocalDate toDate = toDateStr != null ? LocalDate.parse(toDateStr, DateUtils.DEFAULT_DATE_FORMATTER) : null;

        return this.readPlatformService.retrieveUnreconciledGLEntries(glAccountId, fromDate, toDate);
    }

    @DELETE
    @Path("{importId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Delete Reconciliation", description = """
            Delete a reconciliation import.
            
            Example Request:
            
            DELETE accounting/reconciliation/1
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult deleteImport(@PathParam("importId") @Parameter(description = "importId") final Long importId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteReconciliationImport(importId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GET
    @Path("rules")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "List Reconciliation Rules", description = """
            Retrieve all reconciliation rules, optionally filtered by GL account.
            
            Example Requests:
            
            accounting/reconciliation/rules
            
            accounting/reconciliation/rules?glAccountId=1
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public List<ReconciliationRuleData> retrieveAllRules(
            @QueryParam("glAccountId") @Parameter(description = "glAccountId") final Long glAccountId) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSION);

        return this.readPlatformService.retrieveAllRules(glAccountId);
    }

    @POST
    @Path("rules")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Create Reconciliation Rule", description = """
            Create a new reconciliation matching rule.
            
            Mandatory Fields:
            name, matchType, priority
            
            Example Request:
            
            {
              "name": "Exact Amount Match",
              "matchType": "EXACT_AMOUNT",
              "priority": 1,
              "tolerance": 0.00
            }
            """)
    @RequestBody(required = true)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult createRule(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createReconciliationRule().withJson(apiRequestBodyAsJson)
                .build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @PUT
    @Path("rules/{ruleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Update Reconciliation Rule", description = """
            Update a reconciliation rule.
            
            Example Request:
            
            {
              "priority": 2,
              "tolerance": 1.00
            }
            """)
    @RequestBody(required = true)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult updateRule(@PathParam("ruleId") @Parameter(description = "ruleId") final Long ruleId,
            final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateReconciliationRule(ruleId)
                .withJson(apiRequestBodyAsJson).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @DELETE
    @Path("rules/{ruleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(tags = { "Account Reconciliation" }, summary = "Delete Reconciliation Rule", description = """
            Delete a reconciliation rule.
            
            Example Request:
            
            DELETE accounting/reconciliation/rules/1
            """)
    @ApiResponse(responseCode = "200", description = "OK")
    public CommandProcessingResult deleteRule(@PathParam("ruleId") @Parameter(description = "ruleId") final Long ruleId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteReconciliationRule(ruleId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
