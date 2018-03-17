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
package org.apache.fineract.portfolio.charge.api;

import io.swagger.annotations.*;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("/charges")
@Component
@Scope("singleton")
@Api(value = "Charges", description = "Its typical for MFIs to add extra costs for their financial products. These are typically Fees or Penalties.\n" + "\n" + "A Charge on fineract platform is what we use to model both Fees and Penalties.\n" + "\n" + "At present we support defining charges for use with Client accounts and both loan and saving products.")
public class ChargesApiResource {

    private final Set<String> CHARGES_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "amount", "currency", "penalty", "active",
            "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions", "chargeAppliesToOptions",
            "chargeTimeTypeOptions", "currencyOptions", "loanChargeCalculationTypeOptions", "loanChargeTimeTypeOptions",
            "savingsChargeCalculationTypeOptions", "savingsChargeTimeTypeOptions", "incomeAccount", "clientChargeCalculationTypeOptions",
            "clientChargeTimeTypeOptions"));

    private final String resourceNameForPermissions = "CHARGE";

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<ChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ChargesApiResource(final PlatformSecurityContext context, final ChargeReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<ChargeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Charges", httpMethod = "GET", notes = "Returns the list of defined charges.\n" + "\n" + "Example Requests:\n" + "\n" + "charges")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ChargesApiResourceSwagger.GetChargesResponse.class, responseContainer = "List")})
    public String retrieveAllCharges(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ChargeData> charges = this.readPlatformService.retrieveAllCharges();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, charges, this.CHARGES_DATA_PARAMETERS);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Charge", httpMethod = "GET", notes = "Returns the details of a defined Charge.\n" + "\n" + "Example Requests:\n" + "\n" + "charges/1")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ChargesApiResourceSwagger.GetChargesResponse.class)})
    public String retrieveCharge(@PathParam("chargeId") @ApiParam(value = "chargeId") final Long chargeId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ChargeData charge = this.readPlatformService.retrieveCharge(chargeId);
        if (settings.isTemplate()) {
            final ChargeData templateData = this.readPlatformService.retrieveNewChargeDetails();
            charge = ChargeData.withTemplate(charge, templateData);
        }

        return this.toApiJsonSerializer.serialize(settings, charge, this.CHARGES_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Charge Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "charges/template\n")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ChargesApiResourceSwagger.GetChargesTemplateResponse.class)})
    public String retrieveNewChargeDetails(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ChargeData charge = this.readPlatformService.retrieveNewChargeDetails();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, charge, this.CHARGES_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create/Define a Charge", httpMethod = "POST", notes = "Define a new charge that can later be associated with loans and savings through their respective product definitions or directly on each account instance.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ChargesApiResourceSwagger.PostChargesRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ChargesApiResourceSwagger.PostChargesResponse.class)})
    public String createCharge(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCharge().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Charge", httpMethod = "PUT", notes = "Updates the details of a Charge.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = ChargesApiResourceSwagger.PutChargesChargeIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ChargesApiResourceSwagger.PutChargesChargeIdResponse.class)})
    public String updateCharge(@PathParam("chargeId") @ApiParam(value = "chargeId") final Long chargeId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCharge(chargeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Charge", httpMethod = "DELETE", notes = "Deletes a Charge.")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = ChargesApiResourceSwagger.DeleteChargesChargeIdResponse.class)})
    public String deleteCharge(@PathParam("chargeId") @ApiParam(value = "chargeId") final Long chargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCharge(chargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}