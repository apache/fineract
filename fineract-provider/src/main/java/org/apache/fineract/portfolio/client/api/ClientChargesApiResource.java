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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

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

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientChargeData;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.apache.fineract.portfolio.client.service.ClientChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientTransactionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/clients/{clientId}/charges")
@Component
public class ClientChargesApiResource {

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final ClientChargeReadPlatformService clientChargeReadPlatformService;
    private final ClientTransactionReadPlatformService clientTransactionReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientChargesApiResource(final PlatformSecurityContext context, final ChargeReadPlatformService chargeReadPlatformService,
            final ClientChargeReadPlatformService clientChargeReadPlatformService,
            final DefaultToApiJsonSerializer<ClientChargeData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ClientTransactionReadPlatformService clientTransactionReadPlatformService) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.clientChargeReadPlatformService = clientChargeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.clientTransactionReadPlatformService = clientTransactionReadPlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllClientCharges(@PathParam("clientId") final Long clientId,
            @DefaultValue(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL) @QueryParam(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS) final String chargeStatus,
            @QueryParam("pendingPayment") final Boolean pendingPayment, @Context final UriInfo uriInfo,
            @QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (!(is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL)
                || is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE)
                || is(chargeStatus,
                        ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE))) { throw new UnrecognizedQueryParamException(
                                ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS, chargeStatus,
                                new Object[] { ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL,
                                        ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE,
                                        ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE }); }
        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit);

        final Page<ClientChargeData> clientCharges = this.clientChargeReadPlatformService.retrieveClientCharges(clientId, chargeStatus,
                pendingPayment, searchParameters);
        return this.toApiJsonSerializer.serialize(settings, clientCharges, ClientApiConstants.CLIENT_CHARGES_RESPONSE_DATA_PARAMETERS);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveAllChargesApplicableToClients();
        final ClientChargeData clientChargeData = ClientChargeData.template(chargeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientChargeData, ClientApiConstants.CLIENT_CHARGES_RESPONSE_DATA_PARAMETERS);

    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientCharge(@PathParam("clientId") final Long clientId, @PathParam("chargeId") final Long chargeId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);
        ClientChargeData clientCharge = this.clientChargeReadPlatformService.retrieveClientCharge(clientId, chargeId);
        // extract associations
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList(ClientApiConstants.CLIENT_CHARGE_ASSOCIATIONS_TRANSACTIONS));
            }
            ApiParameterHelper.excludeAssociationsForResponseIfProvided(uriInfo.getQueryParameters(), associationParameters);
            if (associationParameters.contains(ClientApiConstants.CLIENT_CHARGE_ASSOCIATIONS_TRANSACTIONS)) {
                Collection<ClientTransactionData> clientTransactionDatas = this.clientTransactionReadPlatformService
                        .retrieveAllTransactions(clientId, chargeId);
                if (!CollectionUtils.isEmpty(clientTransactionDatas)) {
                    clientCharge = ClientChargeData.addAssociations(clientCharge, clientTransactionDatas);
                }
            }
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientCharge, ClientApiConstants.CLIENT_CHARGES_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String applyClientCharge(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientCharge(clientId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String payOrWaiveClientCharge(@PathParam("clientId") final Long clientId, @PathParam("chargeId") final Long chargeId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        String json = "";
        if (is(commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_WAIVE_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().waiveClientCharge(clientId, chargeId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_PAY_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().payClientCharge(clientId, chargeId)
                    .withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_INACTIVATE_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().inactivateClientCharge(clientId, chargeId).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_WAIVE_CHARGE,
                    ClientApiConstants.CLIENT_CHARGE_COMMAND_PAY_CHARGE, ClientApiConstants.CLIENT_CHARGE_COMMAND_INACTIVATE_CHARGE);
        }

        return json;
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClientCharge(@PathParam("clientId") final Long clientId, @PathParam("chargeId") final Long chargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientCharge(clientId, chargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
