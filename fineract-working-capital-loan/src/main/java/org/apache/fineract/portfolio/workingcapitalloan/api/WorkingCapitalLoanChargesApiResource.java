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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanChargeTemplateData;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanApplicationReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@Path("/v1/working-capital-loans")
@Tag(name = "Working Capital Loan Charges", description = "Charge templates applicable to Working Capital Loans.")
@RequiredArgsConstructor
public class WorkingCapitalLoanChargesApiResource extends BaseWorkingCapitalLoanApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = WorkingCapitalLoanConstants.WCL_RESOURCE_NAME;

    private final PlatformSecurityContext context;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final WorkingCapitalLoanApplicationReadPlatformService workingCapitalLoanReadPlatformService;

    @GET
    @Path("{loanId}/charges/template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanChargeTemplate", summary = "Retrieve Working Capital Loan Charges Template", description = "Returns the list of charges applicable to a Working Capital Loan, excluding overdue-installment charges. Useful for building the charge-creation form.\n\n"
            + "Example Request:\n\nworking-capital-loans/1/charges/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.GetWorkingCapitalLoansLoanIdChargesTemplateResponse.class))) })
    public WorkingCapitalLoanChargeTemplateData retrieveTemplate(
            @PathParam("loanId") @Parameter(description = "loanId", required = true) final Long loanId) {
        return buildTemplate(loanId, null);
    }

    @GET
    @Path("external-id/{loanExternalId}/charges/template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveWorkingCapitalLoanChargeTemplateByExternalId", summary = "Retrieve Working Capital Loan Charges Template by External ID", description = "Returns the list of charges applicable to a Working Capital Loan identified by external ID, excluding overdue-installment charges.\n\n"
            + "Example Request:\n\nworking-capital-loans/external-id/7dd80a7c-ycba-a446-t378-91eb6f53e854/charges/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkingCapitalLoanChargesApiResourceSwagger.GetWorkingCapitalLoansLoanIdChargesTemplateResponse.class))) })
    public WorkingCapitalLoanChargeTemplateData retrieveTemplate(
            @PathParam("loanExternalId") @Parameter(description = "loanExternalId", required = true) final String loanExternalId) {
        return buildTemplate(null, loanExternalId);
    }

    private WorkingCapitalLoanChargeTemplateData buildTemplate(final Long loanId, final String loanExternalIdStr) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Long resolvedLoanId = resolveWorkingCapitalLoanId(loanId, loanExternalIdStr, workingCapitalLoanReadPlatformService);
        final List<ChargeData> chargeOptions = chargeReadPlatformService.retrieveLoanAccountApplicableCharges(
                ChargeAppliesTo.WORKING_CAPITAL_LOAN, resolvedLoanId, new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT });

        return WorkingCapitalLoanChargeTemplateData.builder().chargeOptions(chargeOptions).build();
    }
}
