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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.CommandParameterUtil;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.LoanChargeNotFoundException;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Tag(name = "Loan Charges", description = "Its typical for MFIs to add extra costs for their loan products. They can be either Fees or Penalties.\n"
        + "\n"
        + "Loan Charges are instances of Charges and represent either fees and penalties for loan products. Refer Charges for documentation of the various properties of a charge, Only additional properties ( specific to the context of a Charge being associated with a Loan) are described here")
@RequiredArgsConstructor
public class LoanChargesApiResource {

    public static final String COMMAND_PAY = "pay";
    public static final String COMMAND_WAIVE = "waive";
    public static final String COMMAND_ADJUSTMENT = "adjustment";
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "chargeId", "name", "penalty", "chargeTimeType", "dueAsOfDate", "chargeCalculationType", "percentage",
                    "amountPercentageAppliedTo", "currency", "amountWaived", "amountWrittenOff", "amountOutstanding", "amountOrPercentage",
                    "amount", "amountPaid", "chargeOptions", "installmentChargeData", "externalId"));
    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";
    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanChargeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final LoanReadPlatformService loanReadPlatformService;

    @GET
    @Path("{loanId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Charges", description = "It lists all the Loan Charges specific to a Loan \n\n" + "Example Requests:\n"
            + "\n" + "loans/1/charges\n" + "\n" + "\n" + "loans/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class)))) })
    public String retrieveAllLoanCharges(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Context final UriInfo uriInfo) {

        return retrieveAllLoanCharges(loanId, null, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Charges", description = "It lists all the Loan Charges specific to a Loan \n\n" + "Example Requests:\n"
            + "\n" + "loans/1/charges\n" + "\n" + "\n" + "loans/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class)))) })
    public String retrieveAllLoanCharges(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Context final UriInfo uriInfo) {

        return retrieveAllLoanCharges(null, loanExternalId, uriInfo);
    }

    @GET
    @Path("{loanId}/charges/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Charges Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loans/1/charges/template\n" + "\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesTemplateResponse.class))) })
    public String retrieveTemplate(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Context final UriInfo uriInfo) {

        return retrieveTemplate(loanId, null, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Charges Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loans/1/charges/template\n" + "\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesTemplateResponse.class))) })
    public String retrieveTemplate(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @Context final UriInfo uriInfo) {

        return retrieveTemplate(null, loanExternalId, uriInfo);
    }

    @GET
    @Path("{loanId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves Loan Charge according to the Loan ID and Loan Charge ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class))) })
    public String retrieveLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId, @Context final UriInfo uriInfo) {

        return retrieveLoanCharge(loanId, null, loanChargeId, null, uriInfo);
    }

    @GET
    @Path("{loanId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves Loan Charge according to the Loan ID and Loan Charge External ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n"
            + "/loans/1/charges/external-id/1?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class))) })
    public String retrieveLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @Context final UriInfo uriInfo) {

        return retrieveLoanCharge(loanId, null, null, loanChargeExternalId, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves Loan Charge according to the Loan external ID and Loan Charge ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class))) })
    public String retrieveLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId, @Context final UriInfo uriInfo) {

        return retrieveLoanCharge(null, loanExternalId, loanChargeId, null, uriInfo);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves Loan Charge according to the Loan External ID and Loan Charge External ID"
            + "Example Requests:\n" + "\n" + "/loans/1/charges/1\n" + "\n" + "\n" + "/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.GetLoansLoanIdChargesChargeIdResponse.class))) })
    public String retrieveLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @Context final UriInfo uriInfo) {

        return retrieveLoanCharge(null, loanExternalId, null, loanChargeExternalId, uriInfo);
    }

    @POST
    @Path("{loanId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Charge (no command provided) or Pay a charge (command=pay)", description = "Creates a Loan Charge | Pay a Loan Charge")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesResponse.class))) })
    public String executeLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(loanId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Charge (no command provided) or Pay a charge (command=pay)", description = "Creates a Loan Charge | Pay a Loan Charge")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesResponse.class))) })
    public String executeLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(null, loanExternalId, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("{loanId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay / Waive / Adjustment for Loan Charge", description = "Loan Charge will be paid if the loan is linked with a savings account | Waive Loan Charge | Add Charge Adjustment")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdResponse.class))) })
    public String executeLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(loanId, null, loanChargeId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("{loanId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay / Waive / Adjustment for Loan Charge", description = "Loan Charge will be paid if the loan is linked with a savings account | Waive Loan Charge | Add Charge Adjustment")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdResponse.class))) })
    public String executeLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(loanId, null, null, loanChargeExternalId, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay / Waive / Adjustment for Loan Charge", description = "Loan Charge will be paid if the loan is linked with a savings account | Waive Loan Charge | Add Charge Adjustment")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdResponse.class))) })
    public String executeLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(null, loanExternalId, loanChargeId, null, commandParam, apiRequestBodyAsJson);
    }

    @POST
    @Path("external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Pay / Waive / Adjustment for Loan Charge", description = "Loan Charge will be paid if the loan is linked with a savings account | Waive Loan Charge | Add Charge Adjustment")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PostLoansLoanIdChargesChargeIdResponse.class))) })
    public String executeLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return handleExecuteLoanCharge(null, loanExternalId, null, loanChargeExternalId, commandParam, apiRequestBodyAsJson);
    }

    @PUT
    @Path("{loanId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Charge", description = "Currently Loan Charges may be updated only if the Loan is not yet approved")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdResponse.class))) })
    public String updateLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return updateLoanCharge(loanId, null, loanChargeId, null, apiRequestBodyAsJson);
    }

    @PUT
    @Path("{loanId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Charge", description = "Currently Loan Charges may be updated only if the Loan is not yet approved")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdResponse.class))) })
    public String updateLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return updateLoanCharge(loanId, null, null, loanChargeExternalId, apiRequestBodyAsJson);
    }

    @PUT
    @Path("external-id/{loanExternalId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Charge", description = "Currently Loan Charges may be updated only if the Loan is not yet approved")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdResponse.class))) })
    public String updateLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return updateLoanCharge(null, loanExternalId, loanChargeId, null, apiRequestBodyAsJson);
    }

    @PUT
    @Path("external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Charge", description = "Currently Loan Charges may be updated only if the Loan is not yet approved")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.PutLoansLoanIdChargesChargeIdResponse.class))) })
    public String updateLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return updateLoanCharge(null, loanExternalId, null, loanChargeExternalId, apiRequestBodyAsJson);
    }

    @DELETE
    @Path("{loanId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Charge", description = "Note: Currently, A Loan Charge may only be removed from Loans that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.DeleteLoansLoanIdChargesChargeIdResponse.class))) })
    public String deleteLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId) {

        return deleteLoanCharge(loanId, null, loanChargeId, null);
    }

    @DELETE
    @Path("{loanId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Charge", description = "Note: Currently, A Loan Charge may only be removed from Loans that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.DeleteLoansLoanIdChargesChargeIdResponse.class))) })
    public String deleteLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId) {

        return deleteLoanCharge(loanId, null, null, loanChargeExternalId);
    }

    @DELETE
    @Path("external-id/{loanExternalId}/charges/{loanChargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Charge", description = "Note: Currently, A Loan Charge may only be removed from Loans that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.DeleteLoansLoanIdChargesChargeIdResponse.class))) })
    public String deleteLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeId") @Parameter(description = "loanChargeId") final Long loanChargeId) {

        return deleteLoanCharge(null, loanExternalId, loanChargeId, null);
    }

    @DELETE
    @Path("external-id/{loanExternalId}/charges/external-id/{loanChargeExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Loan Charge", description = "Note: Currently, A Loan Charge may only be removed from Loans that are not yet approved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanChargesApiResourceSwagger.DeleteLoansLoanIdChargesChargeIdResponse.class))) })
    public String deleteLoanCharge(@PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanChargeExternalId") @Parameter(description = "loanChargeExternalId") final String loanChargeExternalId) {

        return deleteLoanCharge(null, loanExternalId, null, loanChargeExternalId);
    }

    private String deleteLoanCharge(final Long loanId, final String loanExternalIdStr, final Long loanChargeId,
            final String loanChargeExternalIdStr) {

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        ExternalId loanChargeExternalId = ExternalIdFactory.produce(loanChargeExternalIdStr);

        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        Long resolvedLoanChargeId = getResolvedLoanChargeId(loanChargeId, loanChargeExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanCharge(resolvedLoanId, resolvedLoanChargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private String retrieveLoanCharge(final Long loanId, final String loanExternalIdStr, final Long loanChargeId,
            final String loanChargeExternalIdStr, final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        ExternalId loanChargeExternalId = ExternalIdFactory.produce(loanChargeExternalIdStr);

        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        Long resolvedLoanChargeId = getResolvedLoanChargeId(loanChargeId, loanChargeExternalId);

        final LoanChargeData loanCharge = this.loanChargeReadPlatformService.retrieveLoanChargeDetails(resolvedLoanChargeId,
                resolvedLoanId);

        final Collection<LoanInstallmentChargeData> installmentChargeData = this.loanChargeReadPlatformService
                .retrieveInstallmentLoanCharges(resolvedLoanChargeId, true);

        final LoanChargeData loanChargeData = new LoanChargeData(loanCharge, installmentChargeData);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanChargeData, RESPONSE_DATA_PARAMETERS);
    }

    private String handleExecuteLoanCharge(final Long loanId, final String loanExternalIdStr, final String commandParam,
            final String apiRequestBodyAsJson) {

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);

        CommandProcessingResult result;
        if (CommandParameterUtil.is(commandParam, COMMAND_PAY)) {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().payLoanCharge(resolvedLoanId, null)
                    .withJson(apiRequestBodyAsJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanCharge(resolvedLoanId)
                    .withJson(apiRequestBodyAsJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private String handleExecuteLoanCharge(final Long loanId, final String loanExternalIdStr, final Long loanChargeId,
            final String loanChargeExternalIdStr, final String commandParam, final String apiRequestBodyAsJson) {

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        ExternalId loanChargeExternalId = ExternalIdFactory.produce(loanChargeExternalIdStr);

        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        Long resolvedLoanChargeId = getResolvedLoanChargeId(loanChargeId, loanChargeExternalId);

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        CommandProcessingResult result;
        if (CommandParameterUtil.is(commandParam, COMMAND_WAIVE)) {
            final CommandWrapper commandRequest = builder.waiveLoanCharge(resolvedLoanId, resolvedLoanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (CommandParameterUtil.is(commandParam, COMMAND_PAY)) {
            final CommandWrapper commandRequest = builder.payLoanCharge(resolvedLoanId, resolvedLoanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (CommandParameterUtil.is(commandParam, COMMAND_ADJUSTMENT)) {
            final CommandWrapper commandRequest = builder.adjustmentForLoanCharge(resolvedLoanId, resolvedLoanChargeId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    private String updateLoanCharge(final Long loanId, final String loanExternalIdStr, final Long loanChargeId,
            final String loanChargeExternalIdStr, final String apiRequestBodyAsJson) {

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        ExternalId loanChargeExternalId = ExternalIdFactory.produce(loanChargeExternalIdStr);

        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);
        Long resolvedLoanChargeId = getResolvedLoanChargeId(loanChargeId, loanChargeExternalId);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanCharge(resolvedLoanId, resolvedLoanChargeId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private String retrieveAllLoanCharges(final Long loanId, final String loanExternalIdStr, final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);

        final Collection<LoanChargeData> loanCharges = this.loanChargeReadPlatformService.retrieveLoanCharges(resolvedLoanId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanCharges, RESPONSE_DATA_PARAMETERS);
    }

    private String retrieveTemplate(final Long loanId, final String loanExternalIdStr, final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        Long resolvedLoanId = getResolvedLoanId(loanId, loanExternalId);

        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(resolvedLoanId,
                new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });
        final LoanChargeData loanChargeTemplate = LoanChargeData.template(chargeOptions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanChargeTemplate, RESPONSE_DATA_PARAMETERS);
    }

    private Long getResolvedLoanChargeId(final Long loanChargeId, final ExternalId loanChargeExternalId) {
        Long resolvedLoanChargeId = loanChargeId;
        if (resolvedLoanChargeId == null) {
            loanChargeExternalId.throwExceptionIfEmpty();
            resolvedLoanChargeId = this.loanChargeReadPlatformService.retrieveLoanChargeIdByExternalId(loanChargeExternalId);
            if (resolvedLoanChargeId == null) {
                throw new LoanChargeNotFoundException(loanChargeExternalId);
            }
        }
        return resolvedLoanChargeId;
    }

    private Long getResolvedLoanId(final Long loanId, final ExternalId loanExternalId) {
        Long resolvedLoanId = loanId;
        if (resolvedLoanId == null) {
            loanExternalId.throwExceptionIfEmpty();
            resolvedLoanId = this.loanReadPlatformService.retrieveLoanIdByExternalId(loanExternalId);
            if (resolvedLoanId == null) {
                throw new LoanNotFoundException(loanExternalId);
            }
        }
        return resolvedLoanId;
    }
}
