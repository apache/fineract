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
package org.apache.fineract.portfolio.savings.api;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.COMMAND_INACTIVATE_CHARGE;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.COMMAND_PAY_CHARGE;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.COMMAND_WAIVE_CHARGE;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/savingsaccounts/{savingsAccountId}/charges")
@Component
@Tag(name = "Savings Charges", description = "Its typical for MFIs to add maintenance and operating charges. They can be either Fees or Penalties.\n"
        + "\n"
        + "Savings Charges are instances of Charges and represent either fees and penalties for savings products. Refer Charges for documentation of the various properties of a charge, Only additional properties ( specific to the context of a Charge being associated with a Savings account) are described here")
@RequiredArgsConstructor
public class SavingsAccountChargesApiResource {

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final DefaultToApiJsonSerializer<SavingsAccountChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Savings Charges", description = "Lists Savings Charges\n\n" + "Example Requests:\n" + "\n"
            + "savingsaccounts/1/charges\n" + "\n" + "savingsaccounts/1/charges?chargeStatus=all\n" + "\n"
            + "savingsaccounts/1/charges?chargeStatus=inactive\n" + "\n" + "savingsaccounts/1/charges?chargeStatus=active\n" + "\n"
            + "savingsaccounts/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.GetSavingsAccountsSavingsAccountIdChargesResponse.class)))) })
    public String retrieveAllSavingsAccountCharges(
            @PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @DefaultValue("all") @QueryParam("chargeStatus") @Parameter(description = "chargeStatus") final String chargeStatus,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) {
            throw new UnrecognizedQueryParamException("status", chargeStatus, new Object[] { "all", "active", "inactive" });
        }

        final Collection<SavingsAccountChargeData> savingsAccountCharges = this.savingsAccountChargeReadPlatformService
                .retrieveSavingsAccountCharges(savingsAccountId, chargeStatus);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccountCharges,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_CHARGES_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Savings Charges Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n"
            + "savingsaccounts/1/charges/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.GetSavingsAccountsSavingsAccountIdChargesTemplateResponse.class))) })
    public String retrieveTemplate(@PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService
                .retrieveSavingsAccountApplicableCharges(savingsAccountId);
        final SavingsAccountChargeData savingsAccountChargeTemplate = SavingsAccountChargeData.template(chargeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccountChargeTemplate,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_CHARGES_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Savings account Charge", description = "Retrieves a Savings account Charge\n\n" + "Example Requests:\n"
            + "\n" + "/savingsaccounts/1/charges/5\n" + "\n" + "\n" + "/savingsaccounts/1/charges/5?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class))) })
    public String retrieveSavingsAccountCharge(
            @PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @Parameter(description = "savingsAccountChargeId") final Long savingsAccountChargeId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        final SavingsAccountChargeData savingsAccountCharge = this.savingsAccountChargeReadPlatformService
                .retrieveSavingsAccountChargeDetails(savingsAccountChargeId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccountCharge,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_CHARGES_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Savings account Charge", description = "Creates a Savings account Charge\n\n"
            + "Mandatory Fields for Savings account Charges: chargeId, amount\n\n" + "chargeId, amount, dueDate, dateFormat, locale\n\n"
            + "chargeId, amount, feeOnMonthDay, monthDayFormat, locale")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesResponse.class))) })
    public String addSavingsAccountCharge(
            @PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccountCharge(savingsAccountId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Savings account Charge", description = "Currently Savings account Charges may be updated only if the Savings account is not yet approved.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class))) })
    public String updateSavingsAccountCharge(
            @PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @Parameter(description = "savingsAccountChargeId") final Long savingsAccountChargeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .updateSavingsAccountCharge(savingsAccountId, savingsAccountChargeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay a Savings account Charge | Waive off a Savings account Charge | Inactivate a Savings account Charge", description = "Pay a Savings account Charge:\n\n"
            + "An active charge will be paid when savings account is active and having sufficient balance.\n\n"
            + "Waive off a Savings account Charge:\n\n" + "Outstanding charge amount will be waived off.\n\n"
            + "Inactivate a Savings account Charge:\n\n"
            + "A charge will be allowed to inactivate when savings account is active and not having any dues as of today. If charge is overpaid, corresponding charge payment transactions will be reversed.\n\n"
            + "Showing request/response for 'Pay a Savings account Charge'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class))) })
    public String payOrWaiveSavingsAccountCharge(
            @PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @Parameter(description = "savingsAccountChargeId") final Long savingsAccountChargeId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        String json = "";
        if (is(commandParam, COMMAND_WAIVE_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder()
                    .waiveSavingsAccountCharge(savingsAccountId, savingsAccountChargeId).withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, COMMAND_PAY_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder()
                    .paySavingsAccountCharge(savingsAccountId, savingsAccountChargeId).withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, COMMAND_INACTIVATE_CHARGE)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder()
                    .inactivateSavingsAccountCharge(savingsAccountId, savingsAccountChargeId).withJson(apiRequestBodyAsJson).build();

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

            json = this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, COMMAND_PAY_CHARGE, COMMAND_WAIVE_CHARGE,
                    COMMAND_INACTIVATE_CHARGE);
        }

        return json;
    }

    @DELETE
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Savings account Charge", description = "Note: Currently, A Savings account Charge may only be removed from Savings that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SavingsAccountChargesApiResourceSwagger.DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class))) })
    public String deleteSavingsAccountCharge(
            @PathParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @Parameter(description = "savingsAccountChargeId") final Long savingsAccountChargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .deleteSavingsAccountCharge(savingsAccountId, savingsAccountChargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
