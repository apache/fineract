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

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/savingsaccounts/{savingsAccountId}/charges")
@Component
@Scope("singleton")
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
    public String retrieveAllSavingsAccountCharges(@PathParam("savingsAccountId") final Long savingsAccountId,
            @DefaultValue("all") @QueryParam("chargeStatus") final String chargeStatus, @Context final UriInfo uriInfo) {

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
    public String retrieveTemplate(@PathParam("savingsAccountId") final Long savingsAccountId, @Context final UriInfo uriInfo) {

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
    public String retrieveSavingsAccountCharge(@PathParam("savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") final Long savingsAccountChargeId, @Context final UriInfo uriInfo) {

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
    public String addSavingsAccountCharge(@PathParam("savingsAccountId") final Long savingsAccountId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccountCharge(savingsAccountId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateSavingsAccountCharge(@PathParam("savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") final Long savingsAccountChargeId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .updateSavingsAccountCharge(savingsAccountId, savingsAccountChargeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{savingsAccountChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String payOrWaiveSavingsAccountCharge(@PathParam("savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") final Long savingsAccountChargeId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {

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
    public String deleteSavingsAccountCharge(@PathParam("savingsAccountId") final Long savingsAccountId,
            @PathParam("savingsAccountChargeId") final Long savingsAccountChargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingsAccountCharge(savingsAccountId,
                savingsAccountChargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}