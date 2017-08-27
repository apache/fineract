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

import io.swagger.annotations.*;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.apache.fineract.portfolio.client.service.ClientTransactionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/transactions")
@Component
@Api(value = "Client Transaction", description = "Client Transactions refer to transactions made directly againt a Client's internal account. Currently, these transactions are only created as a result of charge payments/waivers. You are allowed to undo a transaction, however you cannot explicitly create one. ")
public class ClientTransactionsApiResource {

    private final PlatformSecurityContext context;
    private final ClientTransactionReadPlatformService clientTransactionReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientTransactionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientTransactionsApiResource(final PlatformSecurityContext context,
            final ClientTransactionReadPlatformService clientTransactionReadPlatformService,
            final DefaultToApiJsonSerializer<ClientTransactionData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.clientTransactionReadPlatformService = clientTransactionReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Client Transactions", notes = "The list capability of client transaction can support pagination." + "\n\nMandatory Arguments : " + "offset\n" + "    Integer Mandatory, defaults to 0 \n" + "    Indicates the result from which pagination starts\n" + "limit\n" + "    Integer Mandatory, defaults to 200 \n" + "    Restricts the size of results returned. To override the default and return all entries you must explicitly pass a non-positive integer value for limit e.g. limit=0, or limit=-1\n" + "\n" + "\n\n" + "Example Requests:\n" + "clients/189/transactions\n" + "clients/189/transactions?offset=10&limit=50" )
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ClientTransactionData.class)})
    public String retrieveAllClientTransactions(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo,
            @QueryParam("offset") final Integer offset, @QueryParam("limit") final Integer limit) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        SearchParameters searchParameters = SearchParameters.forPagination(offset, limit);
        final Page<ClientTransactionData> clientTransactions = this.clientTransactionReadPlatformService.retrieveAllTransactions(clientId,
                searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientTransactions,
                ClientApiConstants.CLIENT_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Client Transaction", notes = "Example Requests:\n" + "clients/1/transactions/1\n" + "\n" + "\n" + "clients/1/transactions/1?fields=id,officeName" )
    @ApiResponses({@ApiResponse(code = 200, message = "", response = ClientTransactionData.class)})
    public String retrieveClientTransaction(@PathParam("clientId") final Long clientId,
            @PathParam("transactionId") final Long transactionId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        final ClientTransactionData clientTransaction = this.clientTransactionReadPlatformService.retrieveTransaction(clientId,
                transactionId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientTransaction,
                ClientApiConstants.CLIENT_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Undo a Client Transaction")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", dataType = "body", dataTypeClass = ClientData.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = CommandProcessingResult.class)})
    public String undoClientTransaction(@PathParam("clientId") final Long clientId, @PathParam("transactionId") final Long transactionId,
            @QueryParam("command") final String commandParam, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        String json = "";
        if (is(commandParam, ClientApiConstants.CLIENT_TRANSACTION_COMMAND_UNDO)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().undoClientTransaction(clientId, transactionId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, ClientApiConstants.CLIENT_TRANSACTION_COMMAND_UNDO);
        }

        return json;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
