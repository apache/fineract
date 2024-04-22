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
package org.apache.fineract.portfolio.client.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import jakarta.ws.rs.core.UriInfo;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.client.exception.ClientTransactionNotFoundException;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientTransactionReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/clients")
@Component
@Tag(name = "Client Transaction", description = "Client Transactions refer to transactions made directly against a Client's internal account. Currently, these transactions are only created as a result of charge payments/waivers. You are allowed to undo a transaction, however you cannot explicitly create one. ")
@RequiredArgsConstructor
public class ClientTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final ClientTransactionReadPlatformService clientTransactionReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientTransactionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ClientReadPlatformService clientReadPlatformService;

    @GET
    @Path("{clientId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Client Transactions", description = "The list capability of client transaction can support pagination."
            + "\n\n" + "Example Requests:\n\n" + "clients/189/transactions\n\n" + "clients/189/transactions?offset=10&limit=50")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.GetClientsClientIdTransactionsResponse.class))) })
    public String retrieveAllClientTransactions(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo, @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        return getAllClientTransactions(clientId, uriInfo, offset, limit);
    }

    @GET
    @Path("{clientId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Transaction", description = "Example Requests:\n" + "clients/1/transactions/1\n" + "\n" + "\n"
            + "clients/1/transactions/1?fields=id,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.GetClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String retrieveClientTransaction(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        return getClientTransaction(clientId, transactionId, uriInfo);
    }

    @POST
    @Path("{clientId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo a Client Transaction", description = "Undoes a Client Transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.PostClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String undoClientTransaction(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return undoTransaction(clientId, transactionId, commandParam, apiRequestBodyAsJson);
    }

    @GET
    @Path("external-id/{clientExternalId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Client Transactions", description = "The list capability of client transaction can support pagination."
            + "\n\n" + "Example Requests:\n\n" + "clients/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/transactions\n\n"
            + "clients/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/transactions?offset=10&limit=50")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.GetClientsClientIdTransactionsResponse.class))) })
    public String retrieveAllClientTransactions(
            @PathParam("clientExternalId") @Parameter(description = "clientExternalId") final String clientExternalId,
            @Context final UriInfo uriInfo, @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit) {
        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        ExternalId clientExtId = ExternalIdFactory.produce(clientExternalId);

        Long clientId = resolveClientId(clientExtId);
        if (Objects.isNull(clientId)) {
            throw new ClientNotFoundException(clientExtId);
        }

        return getAllClientTransactions(clientId, uriInfo, offset, limit);
    }

    @GET
    @Path("external-id/{clientExternalId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Transaction", description = "Example Requests:\n"
            + "clients/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/transactions/1\n" + "\n" + "\n"
            + "clients/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/transactions/1?fields=id,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.GetClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String retrieveClientTransaction(
            @PathParam("clientExternalId") @Parameter(description = "clientExternalId") final String clientExternalId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        ExternalId clientExtId = ExternalIdFactory.produce(clientExternalId);

        Long clientId = resolveClientId(clientExtId);
        if (Objects.isNull(clientId)) {
            throw new ClientNotFoundException(clientExtId);
        }

        return getClientTransaction(clientId, transactionId, uriInfo);
    }

    @POST
    @Path("external-id/{clientExternalId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo a Client Transaction", description = "Undoes a Client Transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.PostClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String undoClientTransaction(
            @PathParam("clientExternalId") @Parameter(description = "clientExternalId") final String clientExternalId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        ExternalId clientExtId = ExternalIdFactory.produce(clientExternalId);

        Long clientId = resolveClientId(clientExtId);
        if (Objects.isNull(clientId)) {
            throw new ClientNotFoundException(clientExtId);
        }

        return undoTransaction(clientId, transactionId, commandParam, apiRequestBodyAsJson);
    }

    @GET
    @Path("{clientId}/transactions/external-id/{transactionExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Transaction", description = "Example Requests:\n"
            + "clients/1/transactions/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854\n" + "\n" + "\n"
            + "clients/1/transactions/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854?fields=id,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.GetClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String retrieveClientTransaction(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("transactionExternalId") @Parameter(description = "transactionExternalId") final String transactionExternalId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        ExternalId transactionExtId = ExternalIdFactory.produce(transactionExternalId);

        Long transactionId = resolveTransactionId(transactionExtId);

        if (Objects.isNull(transactionId)) {
            throw new ClientTransactionNotFoundException(clientId, transactionExtId);
        }

        return getClientTransaction(clientId, transactionId, uriInfo);
    }

    @GET
    @Path("external-id/{clientExternalId}/transactions/external-id/{transactionExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Transaction", description = "Example Requests:\n"
            + "clients/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/transactions/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854\n"
            + "\n" + "\n"
            + "clients/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/transactions/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854?fields=id,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.GetClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String retrieveClientTransaction(
            @PathParam("clientExternalId") @Parameter(description = "clientExternalId") final String clientExternalId,
            @PathParam("transactionExternalId") @Parameter(description = "transactionExternalId") final String transactionExternalId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        ExternalId clientExtId = ExternalIdFactory.produce(clientExternalId);
        Long clientId = resolveClientId(clientExtId);
        if (Objects.isNull(clientId)) {
            throw new ClientNotFoundException(clientExtId);
        }

        ExternalId transactionExtId = ExternalIdFactory.produce(transactionExternalId);
        Long transactionId = resolveTransactionId(transactionExtId);
        if (Objects.isNull(transactionId)) {
            throw new ClientTransactionNotFoundException(clientId, transactionExtId);
        }

        return getClientTransaction(clientId, transactionId, uriInfo);
    }

    @POST
    @Path("{clientId}/transactions/external-id/{transactionExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo a Client Transaction", description = "Undoes a Client Transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.PostClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String undoClientTransaction(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("transactionExternalId") @Parameter(description = "transactionExternalId") final String transactionExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        ExternalId transactionExtId = ExternalIdFactory.produce(transactionExternalId);
        Long transactionId = resolveTransactionId(transactionExtId);
        if (Objects.isNull(transactionId)) {
            throw new ClientTransactionNotFoundException(clientId, transactionExtId);
        }

        return undoTransaction(clientId, transactionId, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{clientExternalId}/transactions/external-id/{transactionExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Undo a Client Transaction", description = "Undoes a Client Transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientTransactionsApiResourceSwagger.PostClientsClientIdTransactionsTransactionIdResponse.class))) })
    public String undoClientTransaction(
            @PathParam("clientExternalId") @Parameter(description = "clientExternalId") final String clientExternalId,
            @PathParam("transactionExternalId") @Parameter(description = "transactionExternalId") final String transactionExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        ExternalId clientExtId = ExternalIdFactory.produce(clientExternalId);

        Long clientId = resolveClientId(clientExtId);
        if (Objects.isNull(clientId)) {
            throw new ClientNotFoundException(clientExtId);
        }

        ExternalId transactionExtId = ExternalIdFactory.produce(transactionExternalId);
        Long transactionId = resolveTransactionId(transactionExtId);
        if (Objects.isNull(transactionId)) {
            throw new ClientTransactionNotFoundException(clientId, transactionExtId);
        }

        return undoTransaction(clientId, transactionId, commandParam, apiRequestBodyAsJson);
    }

    private String getClientTransaction(Long clientId, Long transactionId, UriInfo uriInfo) {
        final ClientTransactionData clientTransaction = clientTransactionReadPlatformService.retrieveTransaction(clientId, transactionId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, clientTransaction, ClientApiConstants.CLIENT_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    private String getAllClientTransactions(Long clientId, UriInfo uriInfo, Integer offset, Integer limit) {

        SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).build();
        final Page<ClientTransactionData> clientTransactions = clientTransactionReadPlatformService.retrieveAllTransactions(clientId,
                searchParameters);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, clientTransactions, ClientApiConstants.CLIENT_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    private String undoTransaction(Long clientId, Long transactionId, String commandParam, String apiRequestBodyAsJson) {
        if (is(commandParam, ClientApiConstants.CLIENT_TRANSACTION_COMMAND_UNDO)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().undoClientTransaction(clientId, transactionId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

            return toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, ClientApiConstants.CLIENT_TRANSACTION_COMMAND_UNDO);
        }
    }

    private Long resolveClientId(ExternalId clientExternalId) {
        return clientReadPlatformService.retrieveClientIdByExternalId(clientExternalId);
    }

    private Long resolveTransactionId(ExternalId transactionExternalId) {
        ClientTransaction clientTransaction = clientTransactionReadPlatformService.retrieveTransactionByExternalId(transactionExternalId);
        return !Objects.isNull(clientTransaction) ? clientTransaction.getId() : null;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
