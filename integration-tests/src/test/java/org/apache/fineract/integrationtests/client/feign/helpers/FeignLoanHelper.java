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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.DeleteLoansLoanIdResponse;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanRescheduleRequestResponse;
import org.apache.fineract.client.models.GetLoansApprovalTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.LoanApprovedAmountHistoryData;
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
import org.apache.fineract.client.models.PutLoansApprovedAmountRequest;
import org.apache.fineract.client.models.PutLoansApprovedAmountResponse;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountRequest;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountResponse;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignLoanHelper {

    private static final String CREATE_LOAN_PRODUCT_URL = "/fineract-provider/api/v1/loanproducts?" + Utils.TENANT_IDENTIFIER;
    private static final String APPLY_LOAN_URL = "/fineract-provider/api/v1/loans?" + Utils.TENANT_IDENTIFIER;
    private static final String LOAN_STATE_TRANSITION_URL = "/fineract-provider/api/v1/loans/%d?" + Utils.TENANT_IDENTIFIER
            + "&command=approve";
    private static final String LOAN_DISBURSE_URL = "/fineract-provider/api/v1/loans/%d?" + Utils.TENANT_IDENTIFIER + "&command=disburse";
    private static final String LOAN_DISBURSE_TO_SAVINGS_URL = "/fineract-provider/api/v1/loans/%d?" + Utils.TENANT_IDENTIFIER
            + "&command=disburseToSavings";

    private final FineractFeignClient fineractClient;

    public FeignLoanHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostLoanProductsResponse createSimpleLoanProduct() {
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

    public PostLoanProductsResponse createLoanProduct(PostLoanProductsRequest request) {
        return ok(() -> fineractClient.loanProducts().createLoanProduct(request));
    }

    /**
     * WARNING: This method uses ObjectMapperFactory which silences unknown property errors. Do not use this method in
     * tests expecting strict deserialization.
     */
    public Long createLoanProductFromJson(String loanProductJson) {
        try {
            String sanitizedJson = loanProductJson.replaceAll("(?<=\\d),(?=\\d{3}(?!\\d))", "");
            PostLoanProductsRequest request = ObjectMapperFactory.getShared().readValue(sanitizedJson, PostLoanProductsRequest.class);
            return createLoanProduct(request).getResourceId();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid loan product json", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T extractErrorAttribute(CallFailedRuntimeException exception, String jsonAttributeToGetBack) {
        if (!(exception.getCause() instanceof org.apache.fineract.client.feign.FeignException feignException)) {
            throw new IllegalStateException("Expected FeignException cause");
        }
        try {
            Map<String, Object> body = ObjectMapperFactory.getShared().readValue(feignException.responseBodyAsString(), Map.class);
            return (T) body.get(jsonAttributeToGetBack);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse error response for attribute " + jsonAttributeToGetBack, e);
        }
    }

    public <T> T getLoanProductError(String loanProductJson, String jsonAttributeToGetBack) {
        try {
            String sanitizedJson = loanProductJson.replaceAll("(?<=\\d),(?=\\d{3}(?!\\d))", "");
            PostLoanProductsRequest request = ObjectMapperFactory.getShared().readValue(sanitizedJson, PostLoanProductsRequest.class);
            CallFailedRuntimeException ex = fail(() -> fineractClient.loanProducts().createLoanProduct(request));
            return extractErrorAttribute(ex, jsonAttributeToGetBack);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid loan product json", e);
        }
    }

    public CallFailedRuntimeException addLoanChargeExpectingError(Long loanId, PostLoansLoanIdChargesRequest request) {
        return fail(() -> fineractClient.loanCharges().createOrPayLoanCharge(loanId, request, (String) null));
    }

    public List<AdvancedPaymentData> getAdvancedPaymentAllocationRules(Long loanId) {
        return ok(() -> fineractClient.defaultApi().getAdvancedPaymentAllocationRulesOfLoan(loanId));
    }

    // TODO: Rewrite to use fineract-client instead!
    public Long applyForLoanFromJson(String loanApplicationJson) {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        Integer loanId = Utils.performServerPost(jsonRequestSpec(), responseSpec, APPLY_LOAN_URL, loanApplicationJson, "loanId");
        return loanId.longValue();
    }

    public GetLoanProductsProductIdResponse retrieveLoanProduct(Long productId) {
        return ok(() -> fineractClient.loanProducts().retrieveOneLoanProduct(productId));
    }

    public PutLoanProductsProductIdResponse updateLoanProduct(Long productId, PutLoanProductsProductIdRequest request) {
        return ok(() -> fineractClient.loanProducts().updateLoanProduct(productId, request));
    }

    public PostLoansResponse applyForLoan(PostLoansRequest request) {
        return ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
    }

    public PostLoansLoanIdResponse approveLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "approve")));
    }

    // TODO: Rewrite to use fineract-client instead!
    public void approveLoanFromJson(Long loanId, String approveLoanJson) {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        Utils.performServerPost(jsonRequestSpec(), responseSpec, LOAN_STATE_TRANSITION_URL.formatted(loanId), approveLoanJson, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    public void disburseLoanFromJson(Long loanId, String disburseLoanJson) {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        Utils.performServerPost(jsonRequestSpec(), responseSpec, LOAN_DISBURSE_URL.formatted(loanId), disburseLoanJson, "");
    }

    public PostLoansLoanIdResponse disburseLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "disburse")));
    }

    public PostLoansLoanIdResponse disburseToSavings(Long loanId, PostLoansLoanIdRequest request) {
        return disburseToSavingsFromJson(loanId, toDisburseToSavingsJson(request));
    }

    public PostLoansLoanIdResponse rejectLoanByExternalId(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "reject"));
    }

    public PostLoansLoanIdResponse rejectLoan(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "reject")));
    }

    public PostLoansLoanIdResponse disburseToSavingsLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "disburseToSavings"));
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

    public PostLoansLoanIdResponse undoLastDisbursement(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "undolastdisbursal")));
    }

    public PostLoansResponse applyAndApproveLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments) {
        PostLoansResponse response = createSubmittedLoan(clientId, productId, submittedOnDate, principal, numberOfRepayments);
        PostLoansLoanIdRequest approveRequest = new PostLoansLoanIdRequest()//
                .approvedLoanAmount(BigDecimal.valueOf(principal))//
                .approvedOnDate(submittedOnDate)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");

        approveLoan(response.getLoanId(), approveRequest);
        return response;
    }

    public PostLoansResponse createSubmittedLoan(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments) {
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

    public PostLoansResponse createSubmittedLoan(Long clientId) {
        Long productId = createSimpleLoanProduct().getResourceId();
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        return createSubmittedLoan(clientId, productId, todayDate, 10000.0, 12);
    }

    public PostLoansResponse createSubmittedLoanWithOriginators(Long clientId, List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId);
        request.setOriginators(originators);
        return ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
    }

    public PostLoansResponse createSubmittedLoanWithOriginators(Long clientId, Long productId, List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId, productId);
        request.setOriginators(originators);
        return ok(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
    }

    public CallFailedRuntimeException createSubmittedLoanWithOriginatorsExpectingError(Long clientId,
            List<PostLoansOriginatorData> originators) {
        PostLoansRequest request = buildSubmittedLoanRequest(clientId);
        request.setOriginators(originators);
        return fail(() -> fineractClient.loans().calculateOrSubmitLoanApplication(request, (String) null));
    }

    private PostLoansRequest buildSubmittedLoanRequest(Long clientId) {
        return buildSubmittedLoanRequest(clientId, createSimpleLoanProduct().getResourceId());
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

    public PostLoansLoanIdChargesResponse addSpecifiedDueDateCharge(Long loanId, Long chargeId, double amount, String dueDate) {
        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .dueDate(dueDate)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
        return addLoanCharge(loanId, request);
    }

    public PostLoansLoanIdChargesResponse addDisbursementCharge(Long loanId, Long chargeId, double amount) {
        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
        return addLoanCharge(loanId, request);
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

    public PostCreateRescheduleLoansResponse createRescheduleRequest(PostCreateRescheduleLoansRequest request) {
        if (request instanceof LoanRequestBuilders.RescheduleRequestWithRecalculateInterest recalcRequest
                && Boolean.TRUE.equals(recalcRequest.getRecalculateInterest())) {
            return new PostCreateRescheduleLoansResponse().resourceId(createRescheduleRequestFromJson(toRescheduleJson(request, true)));
        }
        return ok(() -> fineractClient.rescheduleLoans().createRescheduleLoan(request));
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Object> createRescheduleRequestWithFullResponse(PostCreateRescheduleLoansRequest request,
            int expectedStatusCode) {
        String json = toRescheduleJson(request,
                request instanceof LoanRequestBuilders.RescheduleRequestWithRecalculateInterest recalcRequest
                        && Boolean.TRUE.equals(recalcRequest.getRecalculateInterest()));
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(expectedStatusCode).build();
        return Utils.performServerPost(jsonRequestSpec(), responseSpec,
                "/fineract-provider/api/v1/rescheduleloans?" + Utils.TENANT_IDENTIFIER, json, "");
    }

    private String toRescheduleJson(PostCreateRescheduleLoansRequest request, boolean recalculateInterest) {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        ObjectNode body = mapper.valueToTree(request);
        if (recalculateInterest) {
            body.put("recalculateInterest", true);
        }
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize reschedule request", e);
        }
    }

    // TODO: Rewrite to use fineract-client instead!
    private Long createRescheduleRequestFromJson(String json) {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        Integer resourceId = Utils.performServerPost(jsonRequestSpec(), responseSpec,
                "/fineract-provider/api/v1/rescheduleloans?" + Utils.TENANT_IDENTIFIER, json, "resourceId");
        return resourceId.longValue();
    }

    // TODO: Rewrite to use fineract-client instead!
    private static RequestSpecification jsonRequestSpec() {
        Utils.initializeRESTAssured();
        return new RequestSpecBuilder().setContentType(ContentType.JSON)
                .addHeader("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey())
                .addHeader("Fineract-Platform-TenantId", "default").build();
    }

    // TODO: Rewrite to use fineract-client instead!
    private PostLoansLoanIdResponse disburseToSavingsFromJson(Long loanId, String disburseJson) {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        String response = Utils.performServerPost(jsonRequestSpec(), responseSpec, LOAN_DISBURSE_TO_SAVINGS_URL.formatted(loanId),
                disburseJson, null);
        try {
            return ObjectMapperFactory.getShared().readValue(response, PostLoansLoanIdResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse disburseToSavings response", e);
        }
    }

    private static String toDisburseToSavingsJson(PostLoansLoanIdRequest request) {
        ObjectMapper mapper = ObjectMapperFactory.getShared();
        ObjectNode body = mapper.valueToTree(request);
        if (request.getTransactionAmount() != null && !body.has("netDisbursalAmount")) {
            body.put("netDisbursalAmount", request.getTransactionAmount().toPlainString());
        }
        if (!body.has("note")) {
            body.put("note", "DISBURSE NOTE");
        }
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize disburseToSavings request", e);
        }
    }

    public PostUpdateRescheduleLoansResponse approveRescheduleRequest(Long scheduleId, PostUpdateRescheduleLoansRequest request) {
        return ok(() -> fineractClient.rescheduleLoans().updateRescheduleLoan(scheduleId, request, "approve"));
    }

    public PostUpdateRescheduleLoansResponse rejectRescheduleRequest(Long scheduleId, PostUpdateRescheduleLoansRequest request) {
        return ok(() -> fineractClient.rescheduleLoans().updateRescheduleLoan(scheduleId, request, "reject"));
    }

    public PostCreateRescheduleLoansResponse createRescheduleRequestResponse(PostCreateRescheduleLoansRequest request) {
        return ok(() -> fineractClient.rescheduleLoans().createRescheduleLoan(request));
    }

    public GetLoanRescheduleRequestResponse readRescheduleRequest(Long scheduleId, String fields) {
        return ok(() -> fineractClient.rescheduleLoans().retrieveOneRescheduleLoan(scheduleId, fields));
    }

    public void createAndApproveRescheduleRequest(PostCreateRescheduleLoansRequest createRequest,
            PostUpdateRescheduleLoansRequest approveRequest) {
        Long scheduleId = createRescheduleRequest(createRequest).getResourceId();
        approveRescheduleRequest(scheduleId, approveRequest);
    }

    public PutLoansApprovedAmountResponse modifyApprovedAmount(Long loanId, BigDecimal approvedAmount) {
        return ok(() -> fineractClient.loans().updateApprovedAmountLoan(loanId,
                new PutLoansApprovedAmountRequest().amount(approvedAmount).locale("en")));
    }

    public List<LoanApprovedAmountHistoryData> getLoanApprovedAmountHistory(Long loanId) {
        return ok(() -> fineractClient.loans().retrieveApprovedAmountHistoryLoan(loanId));
    }

    public PostLoansLoanIdResponse undoDisbursement(Long loanId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoan(loanId, request, Map.of("command", "undoDisbursal")));
    }

    public PostLoansLoanIdResponse approveLoan(String date, Long loanId) {
        return approveLoan(loanId, new PostLoansLoanIdRequest().approvedOnDate(date).dateFormat("dd MMMM yyyy").locale("en")
                .approvedLoanAmount(BigDecimal.valueOf(1000)).expectedDisbursementDate(date));
    }

    public PostLoansLoanIdResponse disburseLoanWithExternalId(String date, Long loanId, String transactionAmount, String externalId) {
        return disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(date).dateFormat("dd MMMM yyyy").locale("en")
                .transactionAmount(new BigDecimal(transactionAmount)).externalId(externalId));
    }

    public PostLoansLoanIdResponse disburseLoan(String date, Long loanId, String transactionAmount) {
        return disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(date).dateFormat("dd MMMM yyyy").locale("en")
                .transactionAmount(new BigDecimal(transactionAmount)));
    }

    public PostLoansLoanIdResponse approveLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "approve"));
    }

    public PostLoansLoanIdResponse disburseLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "disburse"));
    }

    public PostLoansLoanIdResponse undoApprovalLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "undoapproval"));
    }

    public PostLoansLoanIdResponse undoDisbursalLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "undodisbursal"));
    }

    public PostLoansLoanIdResponse withdrawnByApplicantLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "withdrawnByApplicant"));
    }

    public PostLoansLoanIdResponse assignLoanOfficerLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return assignLoanOfficerByExternalId(loanExternalId, request);
    }

    public PostLoansLoanIdResponse unassignLoanOfficerLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return unassignLoanOfficerByExternalId(loanExternalId, request);
    }

    public PostLoansLoanIdResponse assignLoanOfficerByExternalId(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "assignLoanOfficer"));
    }

    public PostLoansLoanIdResponse unassignLoanOfficerByExternalId(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "unassignLoanOfficer"));
    }

    public PostLoansLoanIdResponse recoverGuaranteesLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return recoverGuaranteeByExternalId(loanExternalId, request);
    }

    public PostLoansLoanIdResponse recoverGuaranteeByExternalId(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "recoverGuarantees"));
    }

    public PostLoansLoanIdResponse assignDelinquencyLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "assignDelinquency"));
    }

    public PutLoansLoanIdResponse modifyLoanApplication(String loanExternalId, String command, PutLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().updateLoanApplicationByExternalId(loanExternalId, request, command));
    }

    public DeleteLoansLoanIdResponse deleteLoanApplication(String loanExternalId) {
        return ok(() -> fineractClient.loans().deleteLoanApplicationByExternalId(loanExternalId));
    }

    public GetLoansApprovalTemplateResponse getLoanApprovalTemplate(String loanExternalId) {
        return ok(() -> fineractClient.loans().retrieveApprovalTemplateByExternalId(loanExternalId, "approval"));
    }

    public List<GetDelinquencyTagHistoryResponse> getLoanDelinquencyTags(String loanExternalId) {
        return ok(() -> fineractClient.loans().retrieveDelinquencyTagHistoryLoanByExternalId(loanExternalId));
    }

    public PostLoansLoanIdChargesResponse addLoanCharge(String loanExternalId, PostLoansLoanIdChargesRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeByLoanExternalId(loanExternalId, request, (String) null));
    }

    public java.util.List<GetLoansLoanIdChargesChargeIdResponse> getLoanCharges(String loanExternalId) {
        return ok(() -> fineractClient.loanCharges().retrieveAllLoanChargesByLoanExternalId(loanExternalId));
    }

    public GetLoansLoanIdChargesChargeIdResponse getLoanCharge(String loanExternalId, Long loanChargeId) {
        return ok(() -> fineractClient.loanCharges().retrieveOneLoanChargeByLoanExternalId(loanExternalId, loanChargeId));
    }

    public GetLoansLoanIdChargesChargeIdResponse getLoanCharge(Long loanId, String loanChargeExternalId) {
        return ok(() -> fineractClient.loanCharges().retrieveOneLoanChargeByChargeExternalId(loanId, loanChargeExternalId));
    }

    public GetLoansLoanIdChargesChargeIdResponse getLoanCharge(String loanExternalId, String loanChargeExternalId) {
        return ok(() -> fineractClient.loanCharges().retrieveOneLoanChargeByLoanAndChargeExternalId(loanExternalId, loanChargeExternalId));
    }

    public GetLoansLoanIdChargesTemplateResponse getLoanChargeTemplate(String loanExternalId) {
        return ok(() -> fineractClient.loanCharges().retrieveTemplateLoanChargeByLoanExternalId(loanExternalId));
    }

    public PostLoansLoanIdChargesChargeIdResponse waiveLoanCharge(String loanExternalId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeByLoanExternalIdOnExistingCharge(loanExternalId, loanChargeId,
                request, "waive"));
    }

    public PostLoansLoanIdChargesChargeIdResponse waiveLoanCharge(String loanExternalId, String loanChargeExternalId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeByLoanAndChargeExternalId(loanExternalId, loanChargeExternalId,
                request, "waive"));
    }

    public PostLoansLoanIdChargesChargeIdResponse payLoanCharge(String loanExternalId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeByLoanExternalIdOnExistingCharge(loanExternalId, loanChargeId,
                request, "pay"));
    }

    public PostLoansLoanIdChargesChargeIdResponse payLoanCharge(String loanExternalId, String loanChargeExternalId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeByLoanAndChargeExternalId(loanExternalId, loanChargeExternalId,
                request, "pay"));
    }

    public PostLoansLoanIdChargesChargeIdResponse chargeAdjustment(String loanExternalId, String loanChargeExternalId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().executeLoanChargeByLoanAndChargeExternalId(loanExternalId, loanChargeExternalId,
                request, "adjustment"));
    }

    public PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(String loanExternalId, Long loanChargeId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().updateLoanChargeByLoanExternalId(loanExternalId, loanChargeId, request));
    }

    public PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(String loanExternalId, String loanChargeExternalId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().updateLoanChargeByLoanAndChargeExternalId(loanExternalId, loanChargeExternalId,
                request));
    }

    public PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(Long loanId, String loanChargeExternalId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return ok(() -> fineractClient.loanCharges().updateLoanChargeByChargeExternalId(loanId, loanChargeExternalId, request));
    }

    public DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(String loanExternalId, Long loanChargeId) {
        return ok(() -> fineractClient.loanCharges().deleteLoanChargeByLoanExternalId(loanExternalId, loanChargeId));
    }

    public DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(String loanExternalId, String loanChargeExternalId) {
        return ok(() -> fineractClient.loanCharges().deleteLoanChargeByLoanAndChargeExternalId(loanExternalId, loanChargeExternalId));
    }

    public PostLoansLoanIdChargesResponse addChargesForLoan(Long loanId, PostLoansLoanIdChargesRequest request) {
        return ok(() -> fineractClient.loanCharges().createOrPayLoanCharge(loanId, request, (String) null));
    }

    public DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(Long loanId, String loanChargeExternalId) {
        return ok(() -> fineractClient.loanCharges().deleteLoanChargeByChargeExternalId(loanId, loanChargeExternalId));
    }

    public PostLoansLoanIdResponse undoLastDisbursalLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return ok(() -> fineractClient.loans().handleCommandsLoanByExternalId(loanExternalId, request, "undolastdisbursal"));
    }

    private PostLoansRequest buildSubmittedLoanRequest(Long clientId, Long productId) {
        String todayDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
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
