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
package org.apache.fineract.portfolio.self.loanaccount.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import javax.ws.rs.Consumes;
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
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanChargesApiResource;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionsApiResource;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTemplateTypeRequiredException;
import org.apache.fineract.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.apache.fineract.portfolio.loanaccount.guarantor.api.GuarantorsApiResource;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.loanaccount.data.SelfLoansDataValidator;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/loans")
@Component
@Scope("singleton")

@Tag(name = "Self Loans", description = "")
public class SelfLoansApiResource {

    private final PlatformSecurityContext context;
    private final LoansApiResource loansApiResource;
    private final LoanTransactionsApiResource loanTransactionsApiResource;
    private final LoanChargesApiResource loanChargesApiResource;
    private final AppuserLoansMapperReadService appuserLoansMapperReadService;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final SelfLoansDataValidator dataValidator;
    private final GuarantorsApiResource guarantorsApiResource;

    @Autowired
    public SelfLoansApiResource(final PlatformSecurityContext context, final LoansApiResource loansApiResource,
            final LoanTransactionsApiResource loanTransactionsApiResource, final LoanChargesApiResource loanChargesApiResource,
            final AppuserLoansMapperReadService appuserLoansMapperReadService,
            final AppuserClientMapperReadService appUserClientMapperReadService, final SelfLoansDataValidator dataValidator,
            final GuarantorsApiResource guarantorsApiResource) {
        this.context = context;
        this.loansApiResource = loansApiResource;
        this.loanTransactionsApiResource = loanTransactionsApiResource;
        this.loanChargesApiResource = loanChargesApiResource;
        this.appuserLoansMapperReadService = appuserLoansMapperReadService;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.dataValidator = dataValidator;
        this.guarantorsApiResource = guarantorsApiResource;
    }

    @GET
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan", description = "Retrieves a Loan\n\n" + "Example Requests:\n" + "\n" + "self/loans/1\n" + "\n"
            + "\n" + "self/loans/1?fields=id,principal,annualInterestRate\n" + "\n" + "\n"
            + "self/loans/1?fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.GetSelfLoansLoanIdResponse.class))) })
    public String retrieveLoan(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId, @Context final UriInfo uriInfo) {

        this.dataValidator.validateRetrieveLoan(uriInfo);

        validateAppuserLoanMapping(loanId);

        final boolean staffInSelectedOfficeOnly = false;
        return this.loansApiResource.retrieveLoan(loanId, staffInSelectedOfficeOnly, uriInfo);
    }

    @GET
    @Path("{loanId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Transaction Details", description = "Retrieves a Loan Transaction Details" + "Example Request:\n"
            + "\n" + "self/loans/5/transactions/3")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.GetSelfLoansLoanIdTransactionsTransactionIdResponse.class))) })
    public String retrieveTransaction(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.dataValidator.validateRetrieveTransaction(uriInfo);

        validateAppuserLoanMapping(loanId);

        return this.loanTransactionsApiResource.retrieveTransaction(loanId, transactionId, uriInfo);
    }

    @GET
    @Path("{loanId}/charges")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Charges", description = "Lists loan Charges\n\n" + "Example Requests:\n" + "\n"
            + "self/loans/1/charges\n" + "\n" + "\n" + "self/loans/1/charges?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SelfLoansApiResourceSwagger.GetSelfLoansLoanIdChargesResponse.class)))) })
    public String retrieveAllLoanCharges(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Context final UriInfo uriInfo) {

        validateAppuserLoanMapping(loanId);

        return this.loanChargesApiResource.retrieveAllLoanCharges(loanId, uriInfo);
    }

    @GET
    @Path("{loanId}/charges/{chargeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Charge", description = "Retrieves a Loan Charge\n\n" + "Example Requests:\n" + "\n"
            + "self/loans/1/charges/1\n" + "\n" + "\n" + "self/loans/1/charges/1?fields=name,amountOrPercentage")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.GetSelfLoansLoanIdChargesResponse.class))) })
    public String retrieveLoanCharge(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("chargeId") @Parameter(description = "chargeId") final Long loanChargeId, @Context final UriInfo uriInfo) {

        validateAppuserLoanMapping(loanId);

        return this.loanChargesApiResource.retrieveLoanCharge(loanId, loanChargeId, uriInfo);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Details Template", description = "Retrieves Loan Details Template\n\n"
            + "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n\n" + "Example Requests:\n" + "\n"
            + "self/loans/template?templateType=individual&clientId=1\n" + "\n" + "\n"
            + "self/loans/template?templateType=individual&clientId=1&productId=1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.GetSelfLoansTemplateResponse.class))) })
    public String template(@QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("templateType") @Parameter(description = "templateType") final String templateType,
            @Context final UriInfo uriInfo) {

        if (clientId != null) {
            validateAppuserClientsMapping(clientId);
        }

        if (templateType == null) {
            final String errorMsg = "Loan template type must be provided";
            throw new LoanTemplateTypeRequiredException(errorMsg);
        } else if (!(templateType.equalsIgnoreCase("individual") || templateType.equalsIgnoreCase("collateral"))) {
            final String errorMsg = "Loan template type '" + templateType + "' is not supported";
            throw new NotSupportedLoanTemplateTypeException(errorMsg, templateType);
        }
        final Long groupId = null;
        final boolean staffInSelectedOfficeOnly = false;
        final boolean onlyActive = true;
        return this.loansApiResource.template(clientId, groupId, productId, templateType, staffInSelectedOfficeOnly, onlyActive, uriInfo);

    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Calculate Loan Repayment Schedule | Submit a new Loan Application", description = "Calculate Loan Repayment Schedule:\n\n"
            + "Calculates Loan Repayment Schedule\n\n"
            + "Mandatory Fields: productId, principal, loanTermFrequency, loanTermFrequencyType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, amortizationType, interestType, interestCalculationPeriodType, expectedDisbursementDate, transactionProcessingStrategyId\n\n"
            + "Submit a new Loan Application:\n\n"
            + "Mandatory Fields: clientId, productId, principal, loanTermFrequency, loanTermFrequencyType, loanType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, amortizationType, interestType, interestCalculationPeriodType, transactionProcessingStrategyId, expectedDisbursementDate, submittedOnDate, loanType\n\n"
            + "Additional Mandatory Fields if interest recalculation is enabled for product and Rest frequency not same as repayment period: recalculationRestFrequencyDate\n\n"
            + "Additional Mandatory Fields if interest recalculation with interest/fee compounding is enabled for product and compounding frequency not same as repayment period: recalculationCompoundingFrequencyDate\n\n"
            + "Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type loan: datatables\n\n"
            + "Optional Fields: graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, linkAccountId, allowPartialPeriodInterestCalcualtion, fixedEmiAmount, maxOutstandingLoanBalance, disbursementData, graceOnArrearsAgeing, createStandingInstructionAtDisbursement (requires linkedAccountId if set to true)\n\n"
            + "Showing request/response for 'Submit a new Loan Application'")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.PostSelfLoansRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.PostSelfLoansResponse.class))) })
    public String calculateLoanScheduleOrSubmitLoanApplication(
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        HashMap<String, Object> attr = this.dataValidator.validateLoanApplication(apiRequestBodyAsJson);
        final Long clientId = (Long) attr.get("clientId");
        validateAppuserClientsMapping(clientId);

        return this.loansApiResource.calculateLoanScheduleOrSubmitLoanApplication(commandParam, uriInfo, apiRequestBodyAsJson);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Application", description = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.PutSelfLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.PutSelfLoansLoanIdResponse.class))) })
    public String modifyLoanApplication(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        HashMap<String, Object> attr = this.dataValidator.validateModifyLoanApplication(apiRequestBodyAsJson);
        validateAppuserLoanMapping(loanId);
        final Long clientId = (Long) attr.get("clientId");
        if (clientId != null) {
            validateAppuserClientsMapping(clientId);
        }

        return this.loansApiResource.modifyLoanApplication(loanId, apiRequestBodyAsJson);
    }

    @POST
    @Path("{loanId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Applicant Withdraws from Loan Application", description = "Applicant Withdraws from Loan Application\n\n"
            + "Mandatory Fields: withdrawnOnDate")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.PostSelfLoansLoanIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfLoansApiResourceSwagger.PostSelfLoansLoanIdResponse.class))) })
    public String stateTransitions(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        if (!is(commandParam, "withdrawnByApplicant")) {
            throw new UnrecognizedQueryParamException("command", commandParam);
        }
        validateAppuserLoanMapping(loanId);
        return this.loansApiResource.stateTransitions(loanId, commandParam, apiRequestBodyAsJson);
    }

    private void validateAppuserLoanMapping(final Long loanId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isLoanMappedToUser = this.appuserLoansMapperReadService.isLoanMappedToUser(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{loanId}/guarantors")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveGuarantorDetails(@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo) {

        validateAppuserLoanMapping(loanId);
        return this.guarantorsApiResource.retrieveGuarantorDetails(uriInfo, loanId);
    }

}
