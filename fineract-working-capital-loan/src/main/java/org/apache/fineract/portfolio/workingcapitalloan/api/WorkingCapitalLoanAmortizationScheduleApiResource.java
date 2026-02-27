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
package org.apache.fineract.portfolio.workingcapitalloan.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleReadService;
import org.springframework.stereotype.Component;

@Path("/v1/working-capital-loans")
@Component
@Tag(name = "Working Capital Loans", description = "Working Capital Loan operations including projected amortization schedule.")
@RequiredArgsConstructor
public class WorkingCapitalLoanAmortizationScheduleApiResource {

    private final WorkingCapitalLoanAmortizationScheduleReadService readService;

    @GET
    @Path("{loanId}/amortization-schedule")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Projected Amortization Schedule", description = """
            Returns the projected amortization schedule for a Working Capital Loan.

            The schedule contains per-payment details including expected and forecast payments, \
            discount factors, NPV values, balances, expected and actual amortization amounts, \
            income modifications, and deferred balance.

            Example Request:

            working-capital-loans/1/amortization-schedule""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ProjectedAmortizationScheduleData.class))),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan or schedule not found") })
    public ProjectedAmortizationScheduleData retrieveAmortizationSchedule(
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        return readService.retrieveAmortizationSchedule(loanId);
    }
}
