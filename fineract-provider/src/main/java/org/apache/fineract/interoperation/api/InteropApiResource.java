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
package org.apache.fineract.interoperation.api;

import static org.apache.fineract.interoperation.util.InteropUtil.ENTITY_NAME_QUOTE;
import static org.apache.fineract.interoperation.util.InteropUtil.ENTITY_NAME_REQUEST;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.interoperation.data.InteropAccountData;
import org.apache.fineract.interoperation.data.InteropIdentifierAccountResponseData;
import org.apache.fineract.interoperation.data.InteropIdentifierRequestData;
import org.apache.fineract.interoperation.data.InteropIdentifiersResponseData;
import org.apache.fineract.interoperation.data.InteropKycResponseData;
import org.apache.fineract.interoperation.data.InteropQuoteRequestData;
import org.apache.fineract.interoperation.data.InteropQuoteResponseData;
import org.apache.fineract.interoperation.data.InteropTransactionRequestData;
import org.apache.fineract.interoperation.data.InteropTransactionRequestResponseData;
import org.apache.fineract.interoperation.data.InteropTransactionsData;
import org.apache.fineract.interoperation.data.InteropTransferRequestData;
import org.apache.fineract.interoperation.data.InteropTransferResponseData;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.apache.fineract.interoperation.domain.InteropTransferActionType;
import org.apache.fineract.interoperation.service.InteropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/interoperation") // api/v1/
@Component
@Scope("singleton")
@Tag(name = "Inter Operation", description = "")
public class InteropApiResource {

    private PlatformSecurityContext context;
    private ApiRequestParameterHelper apiRequestParameterHelper;

    private DefaultToApiJsonSerializer<CommandProcessingResult> jsonSerializer;

    private InteropService interopService;
    private PortfolioCommandSourceWritePlatformService commandsSourceService;

    @Autowired
    public InteropApiResource(PlatformSecurityContext context, ApiRequestParameterHelper apiRequestParameterHelper,
            DefaultToApiJsonSerializer<CommandProcessingResult> defaultToApiJsonSerializer, InteropService interopService,
            PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.jsonSerializer = defaultToApiJsonSerializer;
        this.interopService = interopService;
        this.commandsSourceService = portfolioCommandSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("health")
    @Operation(summary = "Query Interoperation Health Request", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String health(@Context UriInfo uriInfo) {
        return "OK";
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("accounts/{accountId}")
    @Operation(summary = "Query Interoperation Account details", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropAccountData.class))) })
    public String getAccountDetails(@PathParam("accountId") @Parameter(description = "accountId") String accountId,
            @Context UriInfo uriInfo) {
        InteropAccountData result = interopService.getAccountDetails(accountId);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("accounts/{accountId}/transactions")
    @Operation(summary = "Query transactions by Account Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropTransactionsData.class))) })
    public String getAccountTransactions(@PathParam("accountId") @Parameter(description = "accountId") String accountId,
            @DefaultValue("true") @QueryParam("debit") @Parameter(description = "debit") boolean debit,
            @DefaultValue("false") @QueryParam("credit") @Parameter(description = "credit") boolean credit,
            @QueryParam("fromBookingDateTime") @Parameter(description = "fromBookingDateTime") String fromBookingDateTime,
            @QueryParam("toBookingDateTime") @Parameter(description = "toBookingDateTime") String toBookingDateTime,
            @Context UriInfo uriInfo) {
        LocalDateTime transactionsFrom = fromBookingDateTime == null ? null
                : LocalDateTime.parse(fromBookingDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime transactionsTo = toBookingDateTime == null ? null
                : LocalDateTime.parse(toBookingDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        InteropTransactionsData result = interopService.getAccountTransactions(accountId, debit, credit, transactionsFrom, transactionsTo);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("accounts/{accountId}/identifiers")
    @Operation(summary = "Query Interoperation secondary identifiers by Account Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifiersResponseData.class))) })
    public String getAccountIdentifiers(@PathParam("accountId") @Parameter(description = "accountId") String accountId,
            @Context UriInfo uriInfo) {
        InteropIdentifiersResponseData result = interopService.getAccountIdentifiers(accountId);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("parties/{idType}/{idValue}")
    @Operation(summary = "Query Interoperation Account by secondary identifier", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifierAccountResponseData.class))) })
    public String getAccountByIdentifier(@PathParam("idType") @Parameter(description = "idType") InteropIdentifierType idType,
            @PathParam("idValue") @Parameter(description = "idValue") String idValue, @Context UriInfo uriInfo) {
        InteropIdentifierAccountResponseData result = interopService.getAccountByIdentifier(idType, idValue, null);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("parties/{idType}/{idValue}/{subIdOrType}")
    @Operation(summary = "Query Interoperation Account by secondary identifier", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifierAccountResponseData.class))) })
    public String getAccountByIdentifier(@PathParam("idType") @Parameter(description = "idType") InteropIdentifierType idType,
            @PathParam("idValue") @Parameter(description = "idValue") String idValue,
            @PathParam("subIdOrType") @Parameter(description = "subIdOrType") String subIdOrType, @Context UriInfo uriInfo) {
        InteropIdentifierAccountResponseData result = interopService.getAccountByIdentifier(idType, idValue, subIdOrType);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("parties/{idType}/{idValue}")
    @Operation(summary = "Interoperation Identifier registration", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropIdentifierRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifierAccountResponseData.class))) })
    public String registerAccountIdentifier(@PathParam("idType") @Parameter(description = "idType") InteropIdentifierType idType,
            @PathParam("idValue") @Parameter(description = "idValue") String idValue, @Parameter(hidden = true) String identifierJson,
            @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().registerAccountIdentifier(idType, idValue, null)
                .withJson(identifierJson).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService
                .logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("parties/{idType}/{idValue}/{subIdOrType}")
    @Operation(summary = "Interoperation Identifier registration", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropIdentifierRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifierAccountResponseData.class))) })
    public String registerAccountIdentifier(@PathParam("idType") @Parameter(description = "idType") InteropIdentifierType idType,
            @PathParam("idValue") @Parameter(description = "idValue") String idValue,
            @PathParam("subIdOrType") @Parameter(description = "subIdOrType") String subIdOrType,
            @Parameter(hidden = true) String identifierJson, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().registerAccountIdentifier(idType, idValue, subIdOrType)
                .withJson(identifierJson).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService
                .logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("parties/{idType}/{idValue}")
    @Operation(summary = "Allow Interoperation Identifier registration", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropIdentifierRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifierAccountResponseData.class))) })
    public String deleteAccountIdentifier(@PathParam("idType") @Parameter(description = "idType") InteropIdentifierType idType,
            @PathParam("idValue") @Parameter(description = "idValue") String idValue, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().deleteAccountIdentifier(idType, idValue, null).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService
                .logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("parties/{idType}/{idValue}/{subIdOrType}")
    @Operation(summary = "Allow Interoperation Identifier registration", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropIdentifierRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropIdentifierAccountResponseData.class))) })
    public String deleteAccountIdentifier(@PathParam("idType") @Parameter(description = "idType") InteropIdentifierType idType,
            @PathParam("idValue") @Parameter(description = "idValue") String idValue,
            @PathParam("subIdOrType") @Parameter(description = "subIdOrType") String subIdOrType, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().deleteAccountIdentifier(idType, idValue, subIdOrType).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService
                .logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transactions/{transactionCode}/requests/{requestCode}")
    @Operation(summary = "Query Interoperation Transaction Request", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropTransactionRequestResponseData.class))) })
    public String getTransactionRequest(@PathParam("transactionCode") @Parameter(description = "transactionCode") String transactionCode,
            @PathParam("requestCode") @Parameter(description = "requestCode") String requestCode, @Context UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ENTITY_NAME_REQUEST);

        InteropTransactionRequestResponseData result = interopService.getTransactionRequest(transactionCode, requestCode);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("requests")
    @Operation(summary = "Allow Interoperation Transaction Request", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropTransactionRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropTransactionRequestResponseData.class))) })
    public String createTransactionRequest(@Parameter(hidden = true) String quotesJson, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().createTransactionRequest().withJson(quotesJson).build();

        InteropTransactionRequestResponseData result = (InteropTransactionRequestResponseData) commandsSourceService
                .logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transactions/{transactionCode}/quotes/{quoteCode}")
    @Operation(summary = "Query Interoperation Quote", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropQuoteResponseData.class))) })
    public String getQuote(@PathParam("transactionCode") @Parameter(description = "transactionCode") String transactionCode,
            @PathParam("quoteCode") @Parameter(description = "quoteCode") String quoteCode, @Context UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ENTITY_NAME_QUOTE);

        InteropQuoteResponseData result = interopService.getQuote(transactionCode, quoteCode);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("quotes")
    @Operation(summary = "Calculate Interoperation Quote", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropQuoteRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropQuoteResponseData.class))) })
    public String createQuote(@Parameter(hidden = true) String quotesJson, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().createQuotes().withJson(quotesJson).build();

        InteropQuoteResponseData result = (InteropQuoteResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transactions/{transactionCode}/transfers/{transferCode}")
    @Operation(summary = "Query Interoperation Transfer", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropTransferResponseData.class))) })
    public String getTransfer(@PathParam("transactionCode") @Parameter(description = "transactionCode") String transactionCode,
            @PathParam("transferCode") @Parameter(description = "transferCode") String transferCode, @Context UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ENTITY_NAME_QUOTE);

        InteropTransferResponseData result = interopService.getTransfer(transactionCode, transferCode);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transfers")
    @Operation(summary = "Prepare Interoperation Transfer", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = InteropTransferRequestData.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropTransferResponseData.class))) })
    public String performTransfer(@QueryParam("action") @Parameter(description = "action") String action,
            @Parameter(hidden = true) String quotesJson, @Context UriInfo uriInfo) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("InteropApi");

        baseDataValidator.reset().parameter("action").value(action).notNull().isOneOfEnumValues(InteropTransferActionType.class);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        CommandWrapper commandRequest = new InteropWrapperBuilder().performTransfer(InteropTransferActionType.valueOf(action))
                .withJson(quotesJson).build();

        InteropTransferResponseData result = (InteropTransferResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("accounts/{accountId}/kyc")
    @Operation(summary = "Query KYC by Account Id", description = "")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = InteropKycResponseData.class))) })
    public String getClientKyc(@PathParam("accountId") @Parameter(description = "accountId") String accountId, @Context UriInfo uriInfo) {
        InteropKycResponseData result = interopService.getKyc(accountId);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transactions/{accountId}/disburse")
    @Operation(summary = "Disburse Loan by Account Id", description = "")
    public String disburseLoan(@PathParam("accountId") @Parameter(description = "accountId") String accountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson, @Context UriInfo uriInfo) {
        return interopService.disburseLoan(accountId, apiRequestBodyAsJson);
    }

}
