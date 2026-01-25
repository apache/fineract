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
package org.apache.fineract.portfolio.loanorigination.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorsResponse;
import org.apache.fineract.portfolio.loanorigination.service.LoanOriginatorReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Path("/v1/loans")
@Component
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
@Tag(name = "Loan Originators", description = "Fetch loan originator details for a specific loan")
@RequiredArgsConstructor
public class LoanOriginatorsApiResource {

    private static final String LOAN_RESOURCE_NAME = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanOriginatorReadPlatformService loanOriginatorReadPlatformService;

    @GET
    @Path("{loanId}/originators")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve originators for a loan by loan ID", description = "Retrieves all originators attached to a specific loan. Requires READ_LOAN permission.")
    @ApiResponse(responseCode = "200", description = "OK - Returns wrapped list of originators (may be empty)")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public LoanOriginatorsResponse retrieveOriginatorsByLoanId(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        this.context.authenticatedUser().validateHasReadPermission(LOAN_RESOURCE_NAME);

        if (!this.loanReadPlatformService.existsByLoanId(loanId)) {
            throw new LoanNotFoundException(loanId);
        }

        return LoanOriginatorsResponse.of(this.loanOriginatorReadPlatformService.retrieveByLoanId(loanId));
    }

    @GET
    @Path("external-id/{loanExternalId}/originators")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve originators for a loan by loan external ID", description = "Retrieves all originators attached to a specific loan using loan external ID. Requires READ_LOAN permission.")
    @ApiResponse(responseCode = "200", description = "OK - Returns wrapped list of originators (may be empty)")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @ApiResponse(responseCode = "404", description = "Loan not found")
    public LoanOriginatorsResponse retrieveOriginatorsByLoanExternalId(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId") final String loanExternalId) {
        this.context.authenticatedUser().validateHasReadPermission(LOAN_RESOURCE_NAME);

        final ExternalId externalId = ExternalIdFactory.produce(loanExternalId);
        final Long loanId = this.loanReadPlatformService.retrieveLoanIdByExternalId(externalId);
        if (loanId == null) {
            throw new LoanNotFoundException(externalId);
        }

        return LoanOriginatorsResponse.of(this.loanOriginatorReadPlatformService.retrieveByLoanId(loanId));
    }
}
