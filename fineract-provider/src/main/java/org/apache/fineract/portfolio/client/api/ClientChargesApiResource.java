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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.apache.commons.lang3.StringUtils;
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
@Tag(name = "Client Charges", description = "It is typical for MFI's to directly associate charges with an implicit Client account. These can be either fees or penalties\n"
        + "\n"
        + "Client Charges are client specific instances of Charges. Refer Charges for documentation of the various properties of a charge, Only additional properties ( specific to the context of a Charge being associated with a Client account) are described here")
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
    @Operation(summary = "List Client Charges", description = "The list capability of client charges supports pagination."
            + "Example Requests:\n" + "clients/1/charges\n" + "\nclients/1/charges?offset=0&limit=5")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.GetClientsClientIdChargesResponse.class))) })
    public String retrieveAllClientCharges(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @DefaultValue(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL) @QueryParam(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS) @Parameter(description = "chargeStatus") final String chargeStatus,
            @QueryParam("pendingPayment") @Parameter(description = "pendingPayment") final Boolean pendingPayment,
            @Context final UriInfo uriInfo, @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (!(is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL)
                || is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE)
                || is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE))) {
            throw new UnrecognizedQueryParamException(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS, chargeStatus,
                    new Object[] { ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL,
                            ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE,
                            ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE });
        }
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
    public String retrieveTemplate(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

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
    @Operation(summary = "Retrieve a Client Charge", description = "Example Requests:\n" + "clients/1/charges/1\n" + "\n" + "\n"
            + "clients/1/charges/1?fields=name,id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.GetClientsClientIdChargesResponse.GetClientsChargesPageItems.class))) })
    public String retrieveClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId, @Context final UriInfo uriInfo) {

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
    @Operation(summary = "Add Client Charge", description = " This API associates a Client charge with an implicit Client account\n"
            + "Mandatory Fields : \n" + "chargeId and dueDate  \n" + "Optional Fields : \n" + "amount")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesResponse.class))) })
    public String applyClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientCharge(clientId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay a Client Charge | Waive a Client Charge", description = "Pay a Client Charge:\n\n" + "Mandatory Fields:"
            + "transactionDate and amount " + "" + "\"Pay either a part of or the entire due amount for a charge.(command=paycharge)\n"
            + "\n" + "Waive a Client Charge:\n" + "\n" + "\n"
            + "This API provides the facility of waiving off the remaining amount on a client charge (command=waive)\n\n"
            + "Showing request/response for 'Pay a Client Charge'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesChargeIdResponse.class))) })
    public String payOrWaiveClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

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
    @Operation(summary = "Delete a Client Charge", description = "Deletes a Client Charge on which no transactions have taken place (either payments or waivers). ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.DeleteClientsClientIdChargesChargeIdResponse.class))) })
    public String deleteClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientCharge(clientId, chargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
