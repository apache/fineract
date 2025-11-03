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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.BuyDownFeeAmortizationDetails;
import org.apache.fineract.portfolio.loanaccount.data.LoanAmortizationAllocationData;
import org.apache.fineract.portfolio.loanaccount.service.BuyDownFeeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAmortizationAllocationService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Tag(name = "Loan Buy Down Fees", description = "Loan Buy Down Fees")
@RequiredArgsConstructor
public class LoanBuyDownFeeApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("loanId", "loanExternalId", "baseLoanTransactionId", "baseLoanTransactionDate", "baseLoanTransactionAmount",
                    "unrecognizedAmount", "chargedOffAmount", "adjustmentAmount", "amortizationMappings"));
    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";
    private final PlatformSecurityContext context;
    private final BuyDownFeeReadPlatformService buyDownFeeReadPlatformService;
    private final LoanAmortizationAllocationService loanAmortizationAllocationService;
    private final DefaultToApiJsonSerializer<LoanAmortizationAllocationData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final LoanReadPlatformService loanReadPlatformService;

    @Path("/{loanId}/buydown-fees")
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get the amortization details of Buy Down fees for a loan", description = "Returns a list of all Buy Down fee entries with amortization details for the specified loan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BuyDownFeeAmortizationDetails.class)))) })
    public List<BuyDownFeeAmortizationDetails> retrieveLoanBuyDownFeeAmortizationDetails(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return this.buyDownFeeReadPlatformService.retrieveLoanBuyDownFeeAmortizationDetails(loanId);
    }

    @GET
    @Path("/external-id/{loanExternalId}/buydown-fees")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get the amortization details of Buy Down fees for a loan by external ID", description = "Returns a list of all Buy Down fee entries with amortization details for the loan specified by external ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BuyDownFeeAmortizationDetails.class)))) })
    public List<BuyDownFeeAmortizationDetails> retrieveLoanBuyDownFeeAmortizationDetailsByExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ExternalId externalId = ExternalIdFactory.produce(loanExternalId);
        final Long resolvedLoanId = loanReadPlatformService.getResolvedLoanId(externalId);

        return this.buyDownFeeReadPlatformService.retrieveLoanBuyDownFeeAmortizationDetails(resolvedLoanId);
    }

    /**
     * Get BuyDown Fees allocation data by loan ID and transaction ID
     */
    @GET
    @Path("{loanId}/buydown-fees/{loanTransactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a BuyDown Fees allocation data", description = "Retrieves BuyDown Fees allocation data according to the Loan ID and Loan Transaction ID"
            + "Example Requests:\n" + "\n" + "/loans/1/buydown-fees/1\n" + "\n" + "\n"
            + "/loans/1/buydown-fees/1?fields=baseLoanTransaction,unrecognizedAmount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanAmortizationAllocationApiResourceSwagger.LoanAmortizationAllocationResponse.class))) })
    public String retrieveBuyDownFeesAllocationData(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanTransactionId") @Parameter(description = "loanTransactionId") final Long loanTransactionId,
            @Context final UriInfo uriInfo) {
        return retrieveBuyDownFeesAllocationData(loanId, null, loanTransactionId, null, uriInfo);
    }

    /**
     * Get BuyDown Fees allocation data by loan external ID and transaction ID
     */
    @GET
    @Path("external-id/{loanExternalId}/buydown-fees/{loanTransactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a BuyDown Fees allocation data", description = "Retrieves BuyDown Fees allocation data according to the Loan external ID and Loan Transaction ID"
            + "Example Requests:\n" + "\n" + "/loans/external-id/1/buydown-fees/1\n" + "\n" + "\n"
            + "/loans/external-id/1/buydown-fees/1?fields=baseLoanTransaction,unrecognizedAmount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanAmortizationAllocationApiResourceSwagger.LoanAmortizationAllocationResponse.class))) })
    public String getBuyDownFeesAllocationDataByLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanTransactionId") @Parameter(description = "loanTransactionId") final Long loanTransactionId,
            @Context final UriInfo uriInfo) {
        return retrieveBuyDownFeesAllocationData(null, loanExternalId, loanTransactionId, null, uriInfo);
    }

    /**
     * Get BuyDown Fees allocation data by loan ID and transaction external ID
     */
    @GET
    @Path("{loanId}/buydown-fees/external-id/{loanTransactionExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a BuyDown Fees allocation data", description = "Retrieves BuyDown Fees allocation data according to the Loan ID and Loan Transaction external ID"
            + "Example Requests:\n" + "\n" + "/loans/1/buydown-fees/external-id/1\n" + "\n" + "\n"
            + "/loans/1/buydown-fees/external-id/1?fields=baseLoanTransaction,unrecognizedAmount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanAmortizationAllocationApiResourceSwagger.LoanAmortizationAllocationResponse.class))) })
    public String getBuyDownFeesAllocationDataByTransactionExternalId(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("loanTransactionExternalId") @Parameter(description = "loanTransactionExternalId") final String loanTransactionExternalId,
            @Context final UriInfo uriInfo) {
        return retrieveBuyDownFeesAllocationData(loanId, null, null, loanTransactionExternalId, uriInfo);
    }

    /**
     * Get BuyDown Fees allocation data by loan external ID and transaction external ID
     */
    @GET
    @Path("external-id/{loanExternalId}/buydown-fees/external-id/{loanTransactionExternalId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a BuyDown Fees allocation data", description = "Retrieves BuyDown Fees allocation data according to the Loan external ID and Loan Transaction external ID"
            + "Example Requests:\n" + "\n" + "/loans/external-id/1/buydown-fees/1\n" + "\n" + "\n"
            + "/loans/external-id/1/buydown-fees/1?fields=baseLoanTransaction,unrecognizedAmount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanAmortizationAllocationApiResourceSwagger.LoanAmortizationAllocationResponse.class))) })
    public String getBuyDownFeesAllocationDataByExternalIds(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId,
            @PathParam("loanTransactionExternalId") @Parameter(description = "loanTransactionExternalId") final String loanTransactionExternalId,
            @Context final UriInfo uriInfo) {
        return retrieveBuyDownFeesAllocationData(null, loanExternalId, null, loanTransactionExternalId, uriInfo);
    }

    private String retrieveBuyDownFeesAllocationData(final Long loanId, final String loanExternalIdStr, final Long loanTransactionId,
            final String loanTransactionExternalIdStr, final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final ExternalId loanExternalId = ExternalIdFactory.produce(loanExternalIdStr);
        final ExternalId loanTransactionExternalId = ExternalIdFactory.produce(loanTransactionExternalIdStr);

        final Long resolvedLoanId = loanId == null ? loanReadPlatformService.getResolvedLoanId(loanExternalId) : loanId;
        final Long resolvedLoanTransactionId = loanReadPlatformService.getResolvedLoanTransactionId(loanTransactionId,
                loanTransactionExternalId);

        final LoanAmortizationAllocationData loanAmortizationAllocationData = loanAmortizationAllocationService
                .retrieveLoanAmortizationAllocationsForBuyDownFeeTransaction(resolvedLoanTransactionId, resolvedLoanId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, loanAmortizationAllocationData, RESPONSE_DATA_PARAMETERS);
    }
}
