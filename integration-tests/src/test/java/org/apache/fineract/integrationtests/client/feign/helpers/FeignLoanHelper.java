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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostAddAndDeleteDisbursementDetailRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansOriginatorData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountRequest;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountResponse;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignLoanHelper {

    private final FineractFeignClient fineractClient;

    public FeignLoanHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createSimpleLoanProduct() {
        PostLoanProductsRequest request = new PostLoanProductsRequest()//
                .name("Simple Loan Product " + System.currentTimeMillis())//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .principal(10000.0)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2L)//
                .interestRatePerPeriod(1.0)//
                .interestRateFrequencyType(2)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .daysInYearType(365)//
                .daysInMonthType(30)//
                .isInterestRecalculationEnabled(false)//
                .accountingRule(1)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
        return createLoanProduct(request);
    }

    public Long createLoanProduct(PostLoanProductsRequest request) {
        PostLoanProductsResponse response = ok(() -> fineractClient.loanProducts().createLoanProduct(request));
        return response.getResourceId();
    }

    public GetLoanProductsProductIdResponse retrieveLoanProduct(Long productId) {
        return ok(() -> fineractClient.loanProducts().retrieveOneLoanProduct(productId));
    }

    public PutLoanProductsProductIdResponse updateLoanProduct(Long productId, PutLoanProductsProductIdRequest request) {
        return ok(() -> fineractClient.loanProducts().updateLoanProduct(productId, request));
    }

    public Long applyForLoan(PostLoansRequest request) {
        PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
        return response.getLoanId();
    }

    public PostLoansLoanIdResponse approveLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "approve")));
    }

    public PostLoansLoanIdResponse disburseLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "disburse")));
    }

    public PostLoansLoanIdResponse disburseToSavings(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "disburseToSavings")));
    }

    public PostLoansLoanIdResponse rejectLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "reject")));
    }

    public PostLoansLoanIdResponse withdrawLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "withdrawnByApplicant")));
    }

    public PostLoansLoanIdResponse moveLoanState(Long loanId, PostLoansLoanIdRequest request, String command) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", command)));
    }

    public PutLoansLoanIdResponse markAsFraud(Long loanId, boolean fraud) {
        return ok(() -> fineractClient.loans().updateLoanApplication(loanId, new PutLoansLoanIdRequest().fraud(fraud),
                Map.of("command", "markAsFraud")));
    }

    public PostLoansLoanIdTransactionsResponse closeLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "close")));
    }

    public PostLoansLoanIdResponse closeAsRescheduled(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "closeAsRescheduled")));
    }

    public PostLoansLoanIdTransactionsResponse forecloseLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "foreclosure")));
    }

    public PostLoansLoanIdResponse assignLoanOfficer(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "assignLoanOfficer")));
    }

    public PostLoansLoanIdResponse unassignLoanOfficer(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "unassignLoanOfficer")));
    }

    public PostLoansLoanIdResponse recoverGuarantee(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "recoverFromGuarantor")));
    }

    public GetLoansLoanIdResponse getLoanDetails(Long loanId) {
        return ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("associations", "all", "exclude", "guarantors,futureSchedule")));
    }

    public GetLoansLoanIdResponse getLoanDetailsByExternalId(String loanExternalId) {
        return ok(() -> fineractClient.loans().retrieveOneLoanByExternalId(loanExternalId, false, "all", null, null));
    }

    public PostLoansLoanIdResponse disburseLoanWithAmount(Long loanId, String date, double amount) {
        return disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(date).transactionAmount(BigDecimal.valueOf(amount))
                .dateFormat("dd MMMM yyyy").locale("en"));
    }

    public GetLoansLoanIdResponse getLoanDetailsWithAssociations(Long loanId, String associations) {
        return ok(() -> fineractClient.loans().retrieveOneLoan(loanId, Map.of("associations", associations)));
    }

    public GetLoansLoanIdResponse getLoanDetailsWithAssociationsAndExclude(Long loanId, String associations, String exclude) {
        return ok(() -> fineractClient.loans().retrieveOneLoan(loanId, Map.of("associations", associations, "exclude", exclude)));
    }

    public void undoApproval(Long loanId) {
        PostLoansLoanIdRequest request = new PostLoansLoanIdRequest();
        ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "undoApproval")));
    }

    public void undoDisbursement(Long loanId) {
        PostLoansLoanIdRequest request = new PostLoansLoanIdRequest();
        ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "undoDisbursal")));
    }

    public Long applyAndApproveLoan(Long clientId, Long productId, String submittedOnDate, Double principal, Integer numberOfRepayments) {
        PostLoansRequest applyRequest = new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .loanType("individual")//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(submittedOnDate)//
                .principal(BigDecimal.valueOf(principal))//
                .loanTermFrequency(numberOfRepayments)//
                .loanTermFrequencyType(2)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");

        Long loanId = applyForLoan(applyRequest);

        PostLoansLoanIdRequest approveRequest = new PostLoansLoanIdRequest()//
                .approvedLoanAmount(BigDecimal.valueOf(principal))//
                .approvedOnDate(submittedOnDate)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");

        approveLoan(loanId, approveRequest);
        return loanId;
    }

    public Long createSubmittedLoan(Long clientId, Long productId, String submittedOnDate, Double principal, Integer numberOfRepayments) {
        PostLoansRequest applyRequest = new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .loanType("individual")//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(submittedOnDate)//
                .principal(BigDecimal.valueOf(principal))//
                .loanTermFrequency(numberOfRepayments)//
                .loanTermFrequencyType(2)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
        return applyForLoan(applyRequest);
    }

    public Long createSubmittedLoan(Long clientId) {
        Long productId = createSimpleLoanProduct();
        String todayDate = org.apache.fineract.integrationtests.common.Utils.dateFormatter
                .format(org.apache.fineract.integrationtests.common.Utils.getLocalDateOfTenant());
        return createSubmittedLoan(clientId, productId, todayDate, 10000.0, 12);
    }

    public Long createSubmittedLoanWithOriginators(Long clientId, List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId);
        request.setOriginators(originators);
        PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
        return response.getLoanId();
    }

    public Long createSubmittedLoanWithOriginators(Long clientId, Long productId, List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId, productId);
        request.setOriginators(originators);
        PostLoansResponse response = ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
        return response.getLoanId();
    }

    public CallFailedRuntimeException createSubmittedLoanWithOriginatorsExpectingError(Long clientId,
            List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId);
        request.setOriginators(originators);
        return fail(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
    }

    private PostLoansRequest buildSubmittedLoanRequest(Long clientId) {
        return buildSubmittedLoanRequest(clientId, createSimpleLoanProduct());
    }

    public PostLoansLoanIdChargesResponse addLoanCharge(Long loanId, PostLoansLoanIdChargesRequest request) {
        return ok(() -> fineractClient.loanCharges().createOrPayLoanCharge(loanId, request, (String) null));
    }

    public List<GetLoansLoanIdChargesChargeIdResponse> getLoanCharges(Long loanId) {
        return ok(() -> fineractClient.loanCharges().retrieveAllLoanCharges(loanId));
    }

    public GetLoansLoanIdChargesChargeIdResponse getLoanCharge(Long loanId, Long loanChargeId) {
        return ok(() -> fineractClient.loanCharges().retrieveOneLoanCharge(loanId, loanChargeId));
    }

    public PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(Long loanId, Long loanChargeId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().updateLoanCharge(loanId, loanChargeId, request));
    }

    public DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(Long loanId, Long loanChargeId) {
        return ok(() -> fineractClient.loanCharges().deleteLoanCharge(loanId, loanChargeId));
    }

    public GetLoansLoanIdChargesTemplateResponse getLoanChargeTemplate(Long loanId) {
        return ok(() -> fineractClient.loanCharges().retrieveTemplateLoanCharge(loanId));
    }

    public PostLoansLoanIdChargesChargeIdResponse waiveLoanCharge(Long loanId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeOnExistingCharge(loanId, loanChargeId, request, "waive"));
    }

    public PostLoansLoanIdChargesChargeIdResponse payLoanCharge(Long loanId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeOnExistingCharge(loanId, loanChargeId, request, "pay"));
    }

    public PostLoansLoanIdChargesChargeIdResponse adjustLoanCharge(Long loanId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeOnExistingCharge(loanId, loanChargeId, request, "adjustment"));
    }

    public Long addSpecifiedDueDateCharge(Long loanId, Long chargeId, double amount, String dueDate) {
        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .dueDate(dueDate)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
        return addLoanCharge(loanId, request).getResourceId();
    }

    public Long addDisbursementCharge(Long loanId, Long chargeId, double amount) {
        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
        return addLoanCharge(loanId, request).getResourceId();
    }

    public CommandProcessingResult addAndDeleteDisbursementDetail(Long loanId, PostAddAndDeleteDisbursementDetailRequest request) {
        return ok(() -> fineractClient.loanDisbursementDetails().addAndDeleteDisbursementDetail(loanId, request));
    }

    public CommandProcessingResult addAndDeleteDisbursementDetail(Long loanId, List<DisbursementDetail> disbursementDetails) {
        return addAndDeleteDisbursementDetail(loanId, new PostAddAndDeleteDisbursementDetailRequest().locale("en")//
                .dateFormat("dd MMMM yyyy")//
                .disbursementData(disbursementDetails));
    }

    public String getDisbursementDetail(Long loanId, Long disbursementId) {
        return ok(() -> fineractClient.loanDisbursementDetails().retriveDetail(loanId, disbursementId));
    }

    public CommandProcessingResult updateDisbursementDate(Long loanId, Long disbursementId, String body) {
        return ok(() -> fineractClient.loanDisbursementDetails().updateDisbursementDate(loanId, disbursementId, body));
    }

    public PutLoansAvailableDisbursementAmountResponse modifyAvailableDisbursementAmount(Long loanId,
            PutLoansAvailableDisbursementAmountRequest request) {
        return ok(() -> fineractClient.loans().updateAvailableDisbursementAmountLoan(loanId, request));
    }

    public Long createRescheduleRequest(PostCreateRescheduleLoansRequest request) {
        PostCreateRescheduleLoansResponse response = ok(() -> fineractClient.rescheduleLoans().createRescheduleLoan(request));
        return response.getResourceId();
    }

    public Long approveRescheduleRequest(Long scheduleId, PostUpdateRescheduleLoansRequest request) {
        PostUpdateRescheduleLoansResponse response = ok(
                () -> fineractClient.rescheduleLoans().updateRescheduleLoan(scheduleId, request, "approve"));
        return response.getResourceId();
    }

    public void createAndApproveRescheduleRequest(PostCreateRescheduleLoansRequest createRequest,
            PostUpdateRescheduleLoansRequest approveRequest) {
        Long scheduleId = createRescheduleRequest(createRequest);
        approveRescheduleRequest(scheduleId, approveRequest);
    }

    private PostLoansRequest buildSubmittedLoanRequest(Long clientId, Long productId) {
        String todayDate = org.apache.fineract.integrationtests.common.Utils.dateFormatter
                .format(org.apache.fineract.integrationtests.common.Utils.getLocalDateOfTenant());
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .loanType("individual")//
                .submittedOnDate(todayDate)//
                .expectedDisbursementDate(todayDate)//
                .principal(BigDecimal.valueOf(10000.0))//
                .loanTermFrequency(12)//
                .loanTermFrequencyType(2)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
    }
}
