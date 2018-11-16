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
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.*;

@Path("/savingsaccounts/{savingsAccountId}/charges")
@Component
@Scope("singleton")
@Api(value = "Savings Charges", description = "Its typical for MFIs to add maintenance and operating charges. They can be either Fees or Penalties.\n" + "\n" + "Savings Charges are instances of Charges and represent either fees and penalties for savings products. Refer Charges for documentation of the various properties of a charge, Only additional properties ( specific to the context of a Charge being associated with a Savings account) are described here")
public class SavingsAccountChargesApiResource {

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    private final DefaultToApiJsonSerializer<SavingsAccountChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public SavingsAccountChargesApiResource(final PlatformSecurityContext context,
            final ChargeReadPlatformService chargeReadPlatformService,
            final SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService,
            final DefaultToApiJsonSerializer<SavingsAccountChargeData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.savingsAccountChargeReadPlatformService = savingsAccountChargeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Savings Charges", httpMethod = "GET", notes = "Lists Savings Charges\n\n" + "Example Requests:\n" + "\n" + "savingsaccounts/1/charges\n" + "\n" + "savingsaccounts/1/charges?chargeStatus=all\n" + "\n" + "savingsaccounts/1/charges?chargeStatus=inactive\n" + "\n" + "savingsaccounts/1/charges?chargeStatus=active\n" + "\n" + "savingsaccounts/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = SavingsAccountChargesApiResourceSwagger.GetSavingsAccountsSavingsAccountIdChargesResponse.class)})
    public String retrieveAllSavingsAccountCharges(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId") final Long savingsAccountId,
            @DefaultValue("all") @QueryParam("chargeStatus") @ApiParam(value = "chargeStatus") final String chargeStatus, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        if (!(is(chargeStatus, "all") || is(chargeStatus, "active") || is(chargeStatus, "inactive"))) { throw new UnrecognizedQueryParamException(
                "status", chargeStatus, new Object[] { "all", "active", "inactive" }); }

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
    @ApiOperation(value = "Retrieve Savings Charges Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "savingsaccounts/1/charges/template")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountChargesApiResourceSwagger.GetSavingsAccountsSavingsAccountIdChargesTemplateResponse.class)})
    public String retrieveTemplate(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId")final Long savingsAccountId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsAccountApplicableCharges(savingsAccountId);
        final SavingsAccountChargeData savingsAccountChargeTemplate = SavingsAccountChargeData.template(chargeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingsAccountChargeTemplate,
                SavingsApiSetConstants.SAVINGS_ACCOUNT_CHARGES_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Savings account Charge", httpMethod = "GET", notes = "Retrieves a Savings account Charge\n\n" + "Example Requests:\n" + "\n" + "/savingsaccounts/1/charges/5\n" + "\n" + "\n" + "/savingsaccounts/1/charges/5?fields=name,amountOrPercentage")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountChargesApiResourceSwagger.GetSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class)})
    public String retrieveSavingsAccountCharge(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId")final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @ApiParam(value = "savingsAccountChargeId") final Long savingsAccountChargeId, @Context final UriInfo uriInfo) {

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
    @ApiOperation(value = "Create a Savings account Charge", httpMethod = "POST", notes = "Creates a Savings account Charge\n\n" + "Mandatory Fields for Savings account Charges: chargeId, amount\n\n" + "chargeId, amount, dueDate, dateFormat, locale\n\n" + "chargeId, amount, feeOnMonthDay, monthDayFormat, locale")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesResponse.class)})
    public String addSavingsAccountCharge(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId")final Long savingsAccountId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccountCharge(savingsAccountId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Savings account Charge", httpMethod = "PUT", notes = "Currently Savings account Charges may be updated only if the Savings account is not yet approved.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SavingsAccountChargesApiResourceSwagger.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountChargesApiResourceSwagger.PutSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class)})
    public String updateSavingsAccountCharge(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId")final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @ApiParam(value = "savingsAccountChargeId") final Long savingsAccountChargeId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .updateSavingsAccountCharge(savingsAccountId, savingsAccountChargeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Pay a Savings account Charge | Waive off a Savings account Charge | Inactivate a Savings account Charge", httpMethod = "POST", notes = "Pay a Savings account Charge:\n\n" + "An active charge will be paid when savings account is active and having sufficient balance.\n\n" + "Waive off a Savings account Charge:\n\n" + "Outstanding charge amount will be waived off.\n\n" + "Inactivate a Savings account Charge:\n\n" + "A charge will be allowed to inactivate when savings account is active and not having any dues as of today. If charge is overpaid, corresponding charge payment transactions will be reversed.\n\n" + "Showing request/response for 'Pay a Savings account Charge'")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200,message = "OK", response = SavingsAccountChargesApiResourceSwagger.PostSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class)})
    public String payOrWaiveSavingsAccountCharge(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @ApiParam(value = "savingsAccountChargeId")final Long savingsAccountChargeId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

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
    @ApiOperation(value = "Delete a Savings account Charge", httpMethod = "DELETE", notes = "Note: Currently, A Savings account Charge may only be removed from Savings that are not yet approved.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = SavingsAccountChargesApiResourceSwagger.DeleteSavingsAccountsSavingsAccountIdChargesSavingsAccountChargeIdResponse.class)})
    public String deleteSavingsAccountCharge(@PathParam("savingsAccountId") @ApiParam(value = "savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") @ApiParam(value = "savingsAccountChargeId") final Long savingsAccountChargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingsAccountCharge(savingsAccountId,
                savingsAccountChargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}