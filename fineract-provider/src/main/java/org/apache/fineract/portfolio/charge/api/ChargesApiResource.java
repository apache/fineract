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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Component;

@Path("/v1/charges")
@Component
@Tag(name = "Charges", description = "Its typical for MFIs to add extra costs for their financial products. These are typically Fees or Penalties.\n"
        + "\n" + "A Charge on fineract platform is what we use to model both Fees and Penalties.\n" + "\n"
        + "At present we support defining charges for use with Client accounts and both loan and saving products.")
@RequiredArgsConstructor
public class ChargesApiResource {

    private static final Set<String> CHARGES_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "amount", "currency", "penalty",
            "active", "chargeAppliesTo", "chargeTimeType", "chargeCalculationType", "chargeCalculationTypeOptions",
            "chargeAppliesToOptions", "chargeTimeTypeOptions", "currencyOptions", "loanChargeCalculationTypeOptions",
            "loanChargeTimeTypeOptions", "savingsChargeCalculationTypeOptions", "savingsChargeTimeTypeOptions", "incomeAccount",
            "clientChargeCalculationTypeOptions", "clientChargeTimeTypeOptions"));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "CHARGE";

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<ChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Charges", description = "Returns the list of defined charges.\n" + "\n" + "Example Requests:\n" + "\n"
            + "charges")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChargesApiResourceSwagger.GetChargesResponse.class)))) })
    public String retrieveAllCharges(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<ChargeData> charges = this.readPlatformService.retrieveAllCharges();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, charges, CHARGES_DATA_PARAMETERS);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Charge", description = "Returns the details of a defined Charge.\n" + "\n" + "Example Requests:\n"
            + "\n" + "charges/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.GetChargesResponse.class))) })
    public String retrieveCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ChargeData charge = this.readPlatformService.retrieveCharge(chargeId);
        if (settings.isTemplate()) {
            final ChargeData templateData = this.readPlatformService.retrieveNewChargeDetails();
            charge = ChargeData.withTemplate(charge, templateData);
        }

        return this.toApiJsonSerializer.serialize(settings, charge, CHARGES_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Charge Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "charges/template\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.GetChargesTemplateResponse.class))) })
    public String retrieveNewChargeDetails(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ChargeData charge = this.readPlatformService.retrieveNewChargeDetails();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, charge, CHARGES_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create/Define a Charge", description = "Define a new charge that can later be associated with loans and savings through their respective product definitions or directly on each account instance.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.PostChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.PostChargesResponse.class))) })
    public String createCharge(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCharge().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Charge", description = "Updates the details of a Charge.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.PutChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.PutChargesChargeIdResponse.class))) })
    public String updateCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCharge(chargeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chargeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Charge", description = "Deletes a Charge.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesApiResourceSwagger.DeleteChargesChargeIdResponse.class))) })
    public String deleteCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCharge(chargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
