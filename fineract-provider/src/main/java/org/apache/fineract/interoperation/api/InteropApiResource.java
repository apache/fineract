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
import static org.apache.fineract.interoperation.util.InteropUtil.ROOT_PATH;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.interoperation.data.InteropAccountData;
import org.apache.fineract.interoperation.data.InteropIdentifierAccountResponseData;
import org.apache.fineract.interoperation.data.InteropIdentifierRequestData;
import org.apache.fineract.interoperation.data.InteropIdentifiersResponseData;
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

@Path("/interoperation") //api/v1/
@Component
@Scope
@Api(value = ROOT_PATH, description = "")
public class InteropApiResource {

    private PlatformSecurityContext context;
    private ApiRequestParameterHelper apiRequestParameterHelper;

    private DefaultToApiJsonSerializer<CommandProcessingResult> jsonSerializer;

    private InteropService interopService;
    private PortfolioCommandSourceWritePlatformService commandsSourceService;

    @Autowired
    public InteropApiResource(PlatformSecurityContext context,
                              ApiRequestParameterHelper apiRequestParameterHelper,
                              DefaultToApiJsonSerializer<CommandProcessingResult> defaultToApiJsonSerializer,
                              InteropService interopService,
                              PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.jsonSerializer = defaultToApiJsonSerializer;
        this.interopService = interopService;
        this.commandsSourceService = portfolioCommandSourceWritePlatformService;
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("health")
    @ApiOperation(value = "Query Interoperation Health Request", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK")})
    public String health(@Context UriInfo uriInfo) {
        return "OK";
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("accounts/{accountId}")
    @ApiOperation(value = "Query Interoperation Account details", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropAccountData.class)})
    public String getAccountDetails(@PathParam("accountId") @ApiParam(value = "accountId") String accountId, @Context UriInfo uriInfo) {
        InteropAccountData result = interopService.getAccountDetails(accountId);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("accounts/{accountId}/transactions")
    @ApiOperation(value = "Query transactions by Account Id", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropTransactionsData.class)})
    public String getAccountTransactions(@PathParam("accountId") @ApiParam(value = "accountId") String accountId,
                                         @DefaultValue("true") @QueryParam("debit") @ApiParam(value = "debit") boolean debit,
                                         @DefaultValue("false") @QueryParam("credit") @ApiParam(value = "credit") boolean credit,
                                         @QueryParam("fromBookingDateTime") @ApiParam(value = "fromBookingDateTime") String fromBookingDateTime,
                                         @QueryParam("toBookingDateTime") @ApiParam(value = "toBookingDateTime") String toBookingDateTime,
                                         @Context UriInfo uriInfo) {
        LocalDateTime transactionsFrom = fromBookingDateTime == null ? null : LocalDateTime.parse(fromBookingDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime transactionsTo = toBookingDateTime == null ? null : LocalDateTime.parse(toBookingDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        InteropTransactionsData result = interopService.getAccountTransactions(accountId, debit, credit, transactionsFrom, transactionsTo);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("accounts/{accountId}/identifiers")
    @ApiOperation(value = "Query Interoperation secondary identifiers by Account Id", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifiersResponseData.class)})
    public String getAccountIdentifiers(@PathParam("accountId") @ApiParam(value = "accountId") String accountId, @Context UriInfo uriInfo) {
        InteropIdentifiersResponseData result = interopService.getAccountIdentifiers(accountId);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parties/{idType}/{idValue}")
    @ApiOperation(value = "Query Interoperation Account by secondary identifier", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifierAccountResponseData.class)})
    public String getAccountByIdentifier(@PathParam("idType") @ApiParam(value = "idType") InteropIdentifierType idType,
                                         @PathParam("idValue") @ApiParam(value = "idValue") String idValue,
                                         @Context UriInfo uriInfo) {
        InteropIdentifierAccountResponseData result = interopService.getAccountByIdentifier(idType, idValue, null);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parties/{idType}/{idValue}/{subIdOrType}")
    @ApiOperation(value = "Query Interoperation Account by secondary identifier", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifierAccountResponseData.class)})
    public String getAccountByIdentifier(@PathParam("idType") @ApiParam(value = "idType") InteropIdentifierType idType,
                                         @PathParam("idValue") @ApiParam(value = "idValue") String idValue,
                                         @PathParam("subIdOrType") @ApiParam(value = "subIdOrType") String subIdOrType,
                                         @Context UriInfo uriInfo) {
        InteropIdentifierAccountResponseData result = interopService.getAccountByIdentifier(idType, idValue, subIdOrType);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parties/{idType}/{idValue}")
    @ApiOperation(value = "Interoperation Identifier registration", httpMethod = "POST", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropIdentifierRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifierAccountResponseData.class)})
    public String registerAccountIdentifier(@PathParam("idType") @ApiParam(value = "idType") InteropIdentifierType idType,
                                            @PathParam("idValue") @ApiParam(value = "idValue") String idValue,
                                            @ApiParam(hidden = true) String identifierJson, @Context UriInfo uriInfo)
            throws Throwable {
        CommandWrapper commandRequest = new InteropWrapperBuilder().registerAccountIdentifier(idType, idValue, null).withJson(identifierJson).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parties/{idType}/{idValue}/{subIdOrType}")
    @ApiOperation(value = "Interoperation Identifier registration", httpMethod = "POST", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropIdentifierRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifierAccountResponseData.class)})
    public String registerAccountIdentifier(@PathParam("idType") @ApiParam(value = "idType") InteropIdentifierType idType,
                                            @PathParam("idValue") @ApiParam(value = "idValue") String idValue,
                                            @PathParam("subIdOrType") @ApiParam(value = "subIdOrType") String subIdOrType,
                                            @ApiParam(hidden = true) String identifierJson, @Context UriInfo uriInfo)
            throws Throwable {
        CommandWrapper commandRequest = new InteropWrapperBuilder().registerAccountIdentifier(idType, idValue, subIdOrType).withJson(identifierJson).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parties/{idType}/{idValue}")
    @ApiOperation(value = "Allow Interoperation Identifier registration", httpMethod = "DELETE", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropIdentifierRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifierAccountResponseData.class)})
    public String deleteAccountIdentifier(@PathParam("idType") @ApiParam(value = "idType") InteropIdentifierType idType,
                                          @PathParam("idValue") @ApiParam(value = "idValue") String idValue,
                                          @Context UriInfo uriInfo)
            throws Throwable {
        CommandWrapper commandRequest = new InteropWrapperBuilder().deleteAccountIdentifier(idType, idValue, null).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("parties/{idType}/{idValue}/{subIdOrType}")
    @ApiOperation(value = "Allow Interoperation Identifier registration", httpMethod = "DELETE", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropIdentifierRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropIdentifierAccountResponseData.class)})
    public String deleteAccountIdentifier(@PathParam("idType") @ApiParam(value = "idType") InteropIdentifierType idType,
                                          @PathParam("idValue") @ApiParam(value = "idValue") String idValue,
                                          @PathParam("subIdOrType") @ApiParam(value = "subIdOrType") String subIdOrType,
                                          @Context UriInfo uriInfo)
            throws Throwable {
        CommandWrapper commandRequest = new InteropWrapperBuilder().deleteAccountIdentifier(idType, idValue, subIdOrType).build();

        InteropIdentifierAccountResponseData result = (InteropIdentifierAccountResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transactions/{transactionCode}/requests/{requestCode}")
    @ApiOperation(value = "Query Interoperation Transaction Request", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropTransactionRequestResponseData.class)})
    public String getTransactionRequest(@PathParam("transactionCode") @ApiParam(value = "transactionCode") String transactionCode,
                                        @PathParam("requestCode") @ApiParam(value = "requestCode") String requestCode, @Context UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ENTITY_NAME_REQUEST);

        InteropTransactionRequestResponseData result = interopService.getTransactionRequest(transactionCode, requestCode);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("requests")
    @ApiOperation(value = "Allow Interoperation Transaction Request", httpMethod = "POST", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropTransactionRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropTransactionRequestResponseData.class)})
    public String createTransactionRequest(@ApiParam(hidden = true) String quotesJson, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().createTransactionRequest().withJson(quotesJson).build();

        InteropTransactionRequestResponseData result = (InteropTransactionRequestResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transactions/{transactionCode}/quotes/{quoteCode}")
    @ApiOperation(value = "Query Interoperation Quote", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropQuoteResponseData.class)})
    public String getQuote(@PathParam("transactionCode") @ApiParam(value = "transactionCode") String transactionCode,
                           @PathParam("quoteCode") @ApiParam(value = "quoteCode") String quoteCode,
                           @Context UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ENTITY_NAME_QUOTE);

        InteropQuoteResponseData result = interopService.getQuote(transactionCode, quoteCode);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("quotes")
    @ApiOperation(value = "Calculate Interoperation Quote", httpMethod = "POST", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropQuoteRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropQuoteResponseData.class)})
    public String createQuote(@ApiParam(hidden = true) String quotesJson, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().createQuotes().withJson(quotesJson).build();

        InteropQuoteResponseData result = (InteropQuoteResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transactions/{transactionCode}/transfers/{transferCode}")
    @ApiOperation(value = "Query Interoperation Transfer", httpMethod = "GET", notes = "")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropTransferResponseData.class)})
    public String getTransfer(@PathParam("transactionCode") @ApiParam(value = "transactionCode") String transactionCode,
                              @PathParam("transferCode") @ApiParam(value = "transferCode") String transferCode,
                              @Context UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(ENTITY_NAME_QUOTE);

        InteropTransferResponseData result = interopService.getTransfer(transactionCode, transferCode);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.jsonSerializer.serialize(settings, result);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("transfers")
    @ApiOperation(value = "Prepare Interoperation Transfer", httpMethod = "POST", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body",
            dataTypeClass = InteropTransferRequestData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = InteropTransferResponseData.class)})
    public String performTransfer(@QueryParam("action") @ApiParam(value = "action") String action,
                                  @ApiParam(hidden = true) String quotesJson, @Context UriInfo uriInfo) {
        CommandWrapper commandRequest = new InteropWrapperBuilder().performTransfer(InteropTransferActionType.valueOf(action)).withJson(quotesJson).build();

        InteropTransferResponseData result = (InteropTransferResponseData) commandsSourceService.logCommandSource(commandRequest);
        ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return jsonSerializer.serialize(settings, result);
    }
}