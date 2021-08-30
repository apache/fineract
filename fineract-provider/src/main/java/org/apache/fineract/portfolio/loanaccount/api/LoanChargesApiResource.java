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
package org.apache.fineract.portfolio.loanaccount.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/charges")
@Component
@Scope("singleton")
@Tag(name = "Loan Charges", description = "Its typical for MFIs to add extra costs for their loan products. They can be either Fees or Penalties.\n"
        + "\n"
        + "Loan Charges are instances of Charges and represent either fees and penalties for loan products. Refer Charges for documentation of the various properties of a charge, Only additional properties ( specific to the context of a Charge being associated with a Loan) are described here")
public class LoanChargesApiResource {

    private final Set<String> responseDataParameters = new HashSet<>(
            Arrays.asList("id", "chargeId", "name", "penalty", "chargeTimeType", "dueAsOfDate", "chargeCalculationType", "percentage",
                    "amountPercentageAppliedTo", "currency", "amountWaived", "amountWrittenOff", "amountOutstanding", "amountOrPercentage",
                    "amount", "amountPaid", "chargeOptions", "installmentChargeData"));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public LoanChargesApiResource(final PlatformSecurityContext context, final ChargeReadPlatformService chargeReadPlatformService,
            final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final DefaultToApiJsonSerializer<LoanChargeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
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
    @Operation(summary = "List Loan Charges", description = "It lists all the Loan Charges specific to a Loan \n\n" + "Example Requests:\n"
            + "\n" + "loans/1/charges\n" + "\n" + "\n" + "loans/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class)))) })
    public String retrieveAllLoanCharges(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<LoanChargeData> loanCharges = this.loanChargeReadPlatformService.retrieveLoanCharges(loanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanCharges, this.responseDataParameters);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Charges Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loans/1/charges/template\n" + "\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesTemplateResponse.class))) })
    public String retrieveTemplate(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
        final LoanChargeData loanChargeTemplate = LoanChargeData.template(chargeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanChargeTemplate, this.responseDataParameters);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves Loan Charge according to the Loan ID and Charge ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class))) })
    public String retrieveLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long loanChargeId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final LoanChargeData loanCharge = this.loanChargeReadPlatformService.retrieveLoanChargeDetails(loanChargeId, loanId);

        final Collection<LoanInstallmentChargeData> installmentChargeData = this.loanChargeReadPlatformService
                .retrieveInstallmentLoanCharges(loanChargeId, true);

        final LoanChargeData loanChargeData = new LoanChargeData(loanCharge, installmentChargeData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanChargeData, this.responseDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Charge", description = "It Creates a Loan Charge")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesResponse.class))) })
    public String executeLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        CommandProcessingResult result = null;
        if (is(commandParam, "pay")) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().payLoanCharge(loanId, null).withJson(apiRequestBodyAsJson)
                    .build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanCharge(loanId).withJson(apiRequestBodyAsJson)
                    .build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Charge", description = "Currently Loan Charges may be updated only if the Loan is not yet approved")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdResponse.class))) })
    public String updateLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long loanChargeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanCharge(loanId, loanChargeId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay Loan Charge", description = "Loan Charge will be paid if the loan is linked with a savings account")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdResponse.class))) })
    public String executeLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long loanChargeId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandProcessingResult result = null;
        if (is(commandParam, "waive")) {
            final CommandWrapper commandRequest = builder.waiveLoanCharge(loanId, loanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "pay")) {
            final CommandWrapper commandRequest = builder.payLoanCharge(loanId, loanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Charge", description = "Note: Currently, A Loan Charge may only be removed from Loans that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.DeleteLoansLoanIdChargesChargeIdResponse.class))) })
    public String deleteLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long loanChargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanCharge(loanId, loanChargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
