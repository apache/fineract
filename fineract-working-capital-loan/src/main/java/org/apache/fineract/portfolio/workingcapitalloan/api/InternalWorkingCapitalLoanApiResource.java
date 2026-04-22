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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.data.InternalWorkingCapitalLoanPaymentRequest;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.InternalWorkingCapitalLoanPaymentService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanAmortizationScheduleWriteService;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanDelinquencyRangeScheduleService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Profile(FineractProfiles.TEST)
@Component
@Path("v1/internal/working-capital-loans")
@Tag(name = "Working Capital Loans", description = "Internal WCL testing API. This API should be disabled in production!")
public class InternalWorkingCapitalLoanApiResource implements InitializingBean {

    private final WorkingCapitalLoanAmortizationScheduleWriteService writeService;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService rangeScheduleService;
    private final InternalWorkingCapitalLoanPaymentService paymentService;

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void afterPropertiesSet() throws Exception {
        log.warn("------------------------------------------------------------");
        log.warn("                                                            ");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("Internal client services mode is enabled");
        log.warn("DO NOT USE THIS IN PRODUCTION!");
        log.warn("                                                            ");
        log.warn("------------------------------------------------------------");
    }

    @POST
    @Path("{loanId}/amortization-schedule")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate and save Projected Amortization Schedule (testing)", description = """
            Generates a projected amortization schedule from the provided parameters \
            and saves it for the given Working Capital Loan.

            DO NOT USE THIS IN PRODUCTION! In the real flow, the schedule will be \
            generated during loan approval/disbursement from the loan and product data.""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public void generateAmortizationSchedule(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            final ProjectedAmortizationScheduleGenerateRequest request) {
        writeService.generateAndSaveAmortizationSchedule(loanId, request);
    }

    @POST
    @Transactional
    @Path("{loanId}/activate")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Activate a Working Capital Loan (testing only)", description = """
            Sets the WC loan status to ACTIVE and records a disbursement detail with the given date.
            Also generates the initial delinquency range schedule period if a delinquency bucket is configured.

            DO NOT USE THIS IN PRODUCTION! Disbursement must go through the proper disbursement flow.""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public Response activateLoan(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("disbursementDate") @Parameter(description = "Disbursement date (yyyy-MM-dd)") final String disbursementDateStr) {
        WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        LocalDate disbursementDate = disbursementDateStr != null ? LocalDate.parse(disbursementDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                : DateUtils.getBusinessLocalDate();

        WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
        detail.setWcLoan(loan);
        detail.setActualDisbursementDate(disbursementDate);
        detail.setActualAmount(loan.getApprovedPrincipal());
        loan.getDisbursementDetails().add(detail);

        loan.setLoanStatus(LoanStatus.ACTIVE);
        loanRepository.saveAndFlush(loan);

        rangeScheduleService.generateInitialPeriod(loan);

        log.info("Activated WC loan {} with disbursement date {} (TEST ONLY)", loanId, disbursementDate);
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Path("{loanId}/generate-next-delinquency-period")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Generate next delinquency range schedule period (testing only)", description = """
            Generates the next delinquency range schedule period if the business date \
            has passed the current period's toDate.

            DO NOT USE THIS IN PRODUCTION! Period generation must go through COB.""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public Response generateNextDelinquencyPeriod(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("businessDate") @Parameter(description = "Business date (yyyy-MM-dd)") final String businessDateStr) {
        WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        LocalDate businessDate = businessDateStr != null ? LocalDate.parse(businessDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                : DateUtils.getBusinessLocalDate();

        rangeScheduleService.generateNextPeriodIfNeeded(loan, businessDate);

        log.info("Generated next delinquency period for WC loan {} with business date {} (TEST ONLY)", loanId, businessDate);
        return Response.ok().build();
    }

    @POST
    @Path("{loanId}/internalMakePayment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Makes Payment (testing)", description = """
            Makes payment for testing purposes.

            DO NOT USE THIS IN PRODUCTION! In the real flow, the schedule will be \
            generated during loan approval/disbursement from the loan and product data.""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Working Capital Loan not found") })
    public void payment(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            final InternalWorkingCapitalLoanPaymentRequest request) {
        paymentService.makePayment(loanId, request.getAmount(), request.getTransactionDate());
    }

}
