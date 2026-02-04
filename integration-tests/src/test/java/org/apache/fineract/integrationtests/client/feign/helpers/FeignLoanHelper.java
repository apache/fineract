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
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansOriginatorData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;

public class FeignLoanHelper {

    private final FineractFeignClient fineractClient;

    public FeignLoanHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createSimpleLoanProduct() {
        PostLoanProductsRequest request = new PostLoanProductsRequest()//
                .name("Simple Loan Product " + System.currentTimeMillis())//
                .shortName(java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase())//
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

    public Long applyForLoan(PostLoansRequest request) {
        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(request, (String) null));
        return response.getLoanId();
    }

    public Long approveLoan(Long loanId, PostLoansLoanIdRequest request) {
        PostLoansLoanIdResponse response = ok(() -> fineractClient.loans().stateTransitions(loanId, request, Map.of("command", "approve")));
        return response.getLoanId();
    }

    public Long disburseLoan(Long loanId, PostLoansLoanIdRequest request) {
        PostLoansLoanIdResponse response = ok(
                () -> fineractClient.loans().stateTransitions(loanId, request, Map.of("command", "disburse")));
        return response.getLoanId();
    }

    public GetLoansLoanIdResponse getLoanDetails(Long loanId) {
        return ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", "all", "exclude", "guarantors,futureSchedule")));
    }

    public GetLoansLoanIdResponse getLoanDetailsWithAssociations(Long loanId, String associations) {
        return ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", associations)));
    }

    public GetLoansLoanIdResponse getLoanDetailsWithAssociationsAndExclude(Long loanId, String associations, String exclude) {
        return ok(() -> fineractClient.loans().retrieveLoan(loanId, Map.of("associations", associations, "exclude", exclude)));
    }

    public void undoApproval(Long loanId) {
        PostLoansLoanIdRequest request = new PostLoansLoanIdRequest();
        ok(() -> fineractClient.loans().stateTransitions(loanId, request, Map.of("command", "undoApproval")));
    }

    public void undoDisbursement(Long loanId) {
        PostLoansLoanIdRequest request = new PostLoansLoanIdRequest();
        ok(() -> fineractClient.loans().stateTransitions(loanId, request, Map.of("command", "undoDisbursal")));
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
        PostLoansResponse response = ok(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(request, (String) null));
        return response.getLoanId();
    }

    public CallFailedRuntimeException createSubmittedLoanWithOriginatorsExpectingError(Long clientId,
            List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId);
        request.setOriginators(originators);
        return fail(() -> fineractClient.loans().calculateLoanScheduleOrSubmitLoanApplication(request, (String) null));
    }

    private PostLoansRequest buildSubmittedLoanRequest(Long clientId) {
        Long productId = createSimpleLoanProduct();
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
