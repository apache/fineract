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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientChargeData;
import org.apache.fineract.portfolio.client.data.ClientTransactionData;
import org.apache.fineract.portfolio.client.service.ClientChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientTransactionReadPlatformService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/v1/clients/{clientId}/charges")
@Component
@Tag(name = "Client Charges", description = "It is typical for MFI's to directly associate charges with an implicit Client account. These can be either fees or penalties\n"
        + "\n"
        + "Client Charges are client specific instances of Charges. Refer Charges for documentation of the various properties of a charge, Only additional properties ( specific to the context of a Charge being associated with a Client account) are described here")
@RequiredArgsConstructor
public class ClientChargesApiResource {

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final ClientChargeReadPlatformService clientChargeReadPlatformService;
    private final ClientTransactionReadPlatformService clientTransactionReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ClientReadPlatformService clientReadPlatformService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Client Charges", operationId = "retrieveAllClientCharges", description = "The list capability of client charges supports pagination."
            + "Example Requests:\n" + "clients/1/charges\n" + "\nclients/1/charges?offset=0&limit=5")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.GetClientsClientIdChargesResponse.class)))
    public Page<ClientChargeData> retrieveAllClientCharges(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @DefaultValue(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL) @QueryParam(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS) @Parameter(description = "chargeStatus") final String chargeStatus,
            @QueryParam("pendingPayment") @Parameter(description = "pendingPayment") final Boolean pendingPayment,
            @Context final UriInfo uriInfo, @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);
        if (!(is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL)
                || is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE)
                || is(chargeStatus, ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE))) {
            throw new UnrecognizedQueryParamException(ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS, chargeStatus,
                    new Object[] { ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ALL,
                            ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_ACTIVE,
                            ClientApiConstants.CLIENT_CHARGE_QUERY_PARAM_STATUS_VALUE_INACTIVE });
        }
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).offset(offset).build();

        return this.clientChargeReadPlatformService.retrieveClientCharges(clientId, chargeStatus, pendingPayment, searchParameters);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client charge template", operationId = "retrieveTemplateClientCharge")
    @AlternativeOperationId("retrieveTemplate_4")
    public ClientChargeData retrieveTemplate(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME);

        final Long clientOfficeId = this.clientReadPlatformService.retrieveOne(clientId).getOfficeId();
        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveAllChargesApplicableToClients(clientOfficeId);
        return ClientChargeData.template(chargeOptions);
    }

    @GET
    @Path("{chargeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Charge", operationId = "retrieveOneClientCharge", description = "Example Requests:\n"
            + "clients/1/charges/1\n" + "\n" + "\n" + "clients/1/charges/1?fields=name,id")
    @AlternativeOperationId("retrieveClientCharge")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.GetClientsClientIdChargesResponse.GetClientsChargesPageItems.class)))
    public ClientChargeData retrieveClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
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
        return clientCharge;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Add Client Charge", operationId = "createClientCharge", description = " This API associates a Client charge with an implicit Client account\n"
            + "Mandatory Fields : \n" + "chargeId and dueDate  \n" + "Optional Fields : \n" + "amount")
    @AlternativeOperationId("applyClientCharge")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesResponse.class)))
    public CommandProcessingResult applyClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createClientCharge(clientId).withJson(apiRequestBodyAsJson)
                .build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @POST
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay a Client Charge | Waive a Client Charge", operationId = "payOrWaiveClientCharge", description = "Pay a Client Charge:\n\n"
            + "Mandatory Fields:" + "transactionDate and amount " + ""
            + "\"Pay either a part of or the entire due amount for a charge.(command=paycharge)\n" + "\n" + "Waive a Client Charge:\n"
            + "\n" + "\n" + "This API provides the facility of waiving off the remaining amount on a client charge (command=waive)\n\n"
            + "Showing request/response for 'Pay a Client Charge'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesChargeIdRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.PostClientsClientIdChargesChargeIdResponse.class)))
    public CommandProcessingResult payOrWaiveClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        if (is(commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_WAIVE_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().waiveClientCharge(clientId, chargeId)
                    .withJson(apiRequestBodyAsJson).build();

            return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        } else if (is(commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_PAY_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().payClientCharge(clientId, chargeId)
                    .withJson(apiRequestBodyAsJson).build();

            return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, ClientApiConstants.CLIENT_CHARGE_COMMAND_WAIVE_CHARGE,
                    ClientApiConstants.CLIENT_CHARGE_COMMAND_PAY_CHARGE);
        }
    }

    @DELETE
    @Path("{chargeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Client Charge", operationId = "deleteClientCharge", description = "Deletes a Client Charge on which no transactions have taken place (either payments or waivers). ")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ClientChargesApiResourceSwagger.DeleteClientsClientIdChargesChargeIdResponse.class)))
    public CommandProcessingResult deleteClientCharge(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteClientCharge(clientId, chargeId).build();

        return this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

}
