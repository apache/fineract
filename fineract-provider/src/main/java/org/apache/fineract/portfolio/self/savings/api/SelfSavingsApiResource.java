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
package org.apache.fineract.portfolio.self.savings.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.savings.api.SavingsAccountChargesApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.savings.data.SelfSavingsAccountConstants;
import org.apache.fineract.portfolio.self.savings.data.SelfSavingsDataValidator;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/savingsaccounts")
@Component
@Scope("singleton")

@Tag(name = "Self Savings Account", description = "")
public class SelfSavingsApiResource {

    private final PlatformSecurityContext context;
    private final SavingsAccountsApiResource savingsAccountsApiResource;
    private final SavingsAccountChargesApiResource savingsAccountChargesApiResource;
    private final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource;
    private final AppuserSavingsMapperReadService appuserSavingsMapperReadService;
    private final SelfSavingsDataValidator dataValidator;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @Autowired
    public SelfSavingsApiResource(final PlatformSecurityContext context, final SavingsAccountsApiResource savingsAccountsApiResource,
            final SavingsAccountChargesApiResource savingsAccountChargesApiResource,
            final SavingsAccountTransactionsApiResource savingsAccountTransactionsApiResource,
            final AppuserSavingsMapperReadService appuserSavingsMapperReadService, final SelfSavingsDataValidator dataValidator,
            final AppuserClientMapperReadService appUserClientMapperReadService) {
        this.context = context;
        this.savingsAccountsApiResource = savingsAccountsApiResource;
        this.savingsAccountChargesApiResource = savingsAccountChargesApiResource;
        this.savingsAccountTransactionsApiResource = savingsAccountTransactionsApiResource;
        this.appuserSavingsMapperReadService = appuserSavingsMapperReadService;
        this.dataValidator = dataValidator;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a savings account", description = "Retrieves a savings account\n\n" + "Example Requests :\n" + "\n"
            + "self/savingsaccounts/1\n" + "\n" + "\n" + "self/savingsaccounts/1?associations=transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfSavingsApiResourceSwagger.GetSelfSavingsAccountsResponse.class))) })
    public String retrieveSavings(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        this.dataValidator.validateRetrieveSavings(uriInfo);

        validateAppuserSavingsAccountMapping(accountId);

        final boolean staffInSelectedOfficeOnly = false;
        return this.savingsAccountsApiResource.retrieveOne(accountId, staffInSelectedOfficeOnly, chargeStatus, uriInfo);
    }

    @GET
    @Path("{accountId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Savings Account Transaction", description = "Retrieves Savings Account Transaction\n\n"
            + "Example Requests:\n" + "\n" + "self/savingsaccounts/1/transactions/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfSavingsApiResourceSwagger.GetSelfSavingsAccountsAccountIdTransactionsTransactionIdResponse.class))) })
    public String retrieveSavingsTransaction(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.dataValidator.validateRetrieveSavingsTransaction(uriInfo);

        validateAppuserSavingsAccountMapping(accountId);

        return this.savingsAccountTransactionsApiResource.retrieveOne(accountId, transactionId, uriInfo);
    }

    @GET
    @Path("{accountId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Savings Charges", description = "Lists Savings Charges\n\n" + "Example Requests:\n" + "\n"
            + "self/savingsaccounts/1/charges\n" + "\n" + "self/savingsaccounts/1/charges?chargeStatus=inactive\n" + "\n"
            + "self/savingsaccounts/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SelfSavingsApiResourceSwagger.GetSelfSavingsAccountsAccountIdChargesResponse.class)))) })
    public String retrieveAllSavingsAccountCharges(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        validateAppuserSavingsAccountMapping(accountId);

        return this.savingsAccountChargesApiResource.retrieveAllSavingsAccountCharges(accountId, chargeStatus, uriInfo);
    }

    @GET
    @Path("{accountId}/charges/{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Savings account Charge", description = "Retrieves a Savings account Charge\n\n" + "Example Requests:\n"
            + "\n" + "self/savingsaccounts/1/charges/5\n" + "\n" + "\n" + "self/savingsaccounts/1/charges/5?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfSavingsApiResourceSwagger.GetSelfSavingsAccountsAccountIdChargesSavingsAccountChargeIdResponse.class))) })
    public String retrieveSavingsAccountCharge(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @PathParam("savingsAccountChargeId") @Parameter(description = "savingsAccountChargeId") final Long savingsAccountChargeId,
            @Context final UriInfo uriInfo) {

        validateAppuserSavingsAccountMapping(accountId);

        return this.savingsAccountChargesApiResource.retrieveSavingsAccountCharge(accountId, savingsAccountChargeId, uriInfo);
    }

    private void validateAppuserSavingsAccountMapping(final Long accountId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isMappedSavings = this.appuserSavingsMapperReadService.isSavingsMappedToUser(accountId, user.getId());
        if (!isMappedSavings) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }

    @GET
    @Path("template")
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@QueryParam("clientId") final Long clientId, @QueryParam("productId") final Long productId,
            @Context final UriInfo uriInfo) {

        validateAppuserClientsMapping(clientId);
        Long groupId = null;
        boolean staffInSelectedOfficeOnly = false;
        return this.savingsAccountsApiResource.template(clientId, groupId, productId, staffInSelectedOfficeOnly, uriInfo);

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String submitSavingsAccountApplication(@QueryParam("command") final String commandParam, @Context final UriInfo uriInfo,
            final String apiRequestBodyAsJson) {

        HashMap<String, Object> parameterMap = this.dataValidator.validateSavingsApplication(apiRequestBodyAsJson);
        final Long clientId = (Long) parameterMap.get(SelfSavingsAccountConstants.clientIdParameterName);
        validateAppuserClientsMapping(clientId);
        return this.savingsAccountsApiResource.submitApplication(apiRequestBodyAsJson);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifySavingsAccountApplication(@PathParam("accountId") final Long accountId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        validateAppuserSavingsAccountMapping(accountId);
        this.dataValidator.validateSavingsApplication(apiRequestBodyAsJson);
        return this.savingsAccountsApiResource.update(accountId, apiRequestBodyAsJson, commandParam);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

}
