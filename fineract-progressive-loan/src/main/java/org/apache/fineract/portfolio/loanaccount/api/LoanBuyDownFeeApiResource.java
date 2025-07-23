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
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.BuyDownFeeAmortizationDetails;
import org.apache.fineract.portfolio.loanaccount.service.BuyDownFeeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@Tag(name = "Loan Buy Down Fees", description = "Loan Buy Down Fees")
@RequiredArgsConstructor
public class LoanBuyDownFeeApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOAN";
    private final PlatformSecurityContext context;
    private final BuyDownFeeReadPlatformService buyDownFeeReadPlatformService;
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

}
