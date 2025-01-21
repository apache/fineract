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
package org.apache.fineract.integrationtests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanInterestPauseApiTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanInterestPauseApiTest.class);

    private static RequestSpecification REQUEST_SPEC;
    private static ResponseSpecification RESPONSE_SPEC;
    private static ResponseSpecification RESPONSE_SPEC_403;
    private static ResponseSpecification RESPONSE_SPEC_404;
    private static ResponseSpecification RESPONSE_SPEC_204;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static LoanTransactionHelper LOAN_TRANSACTIONAL_HELPER_204;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER_403;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER_404;
    private static AccountHelper ACCOUNT_HELPER;
    private static final Integer nonExistLoanId = 99999;
    private static String externalId;
    private static final String nonExistExternalId = "7c4fb86f-a778-4d02-b7a8-ec3ec98941fa";
    private Integer clientId;
    private Integer loanProductId;
    private Integer loanId;
    private final String loanPrincipalAmount = "10000.00";
    private final String numberOfRepayments = "12";
    private final String interestRatePerPeriod = "18";
    private final String dateString = "01 January 2023";

    @BeforeEach
    public void initialize() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        RESPONSE_SPEC_403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        RESPONSE_SPEC_404 = new ResponseSpecBuilder().expectStatusCode(404).build();
        RESPONSE_SPEC_204 = new ResponseSpecBuilder().expectStatusCode(204).build();
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
        LOAN_TRANSACTION_HELPER_403 = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC_403);
        LOAN_TRANSACTION_HELPER_404 = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC_404);
        LOAN_TRANSACTIONAL_HELPER_204 = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC_204);
        ACCOUNT_HELPER = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);

        externalId = UUID.randomUUID().toString();

        createRequiredEntities();

        Assertions.assertNotNull(loanProductId, "Loan Product ID should not be null after creation");
        Assertions.assertNotNull(loanId, "Loan ID should not be null after creation");
        Assertions.assertNotNull(externalId, "External Loan ID should not be null after creation");
    }

    /**
     * Creates the client, loan product, and loan entities
     **/
    private void createRequiredEntities() {
        this.createClientEntity();
        this.createLoanProductEntity();
        this.createLoanEntity();
    }

    @Test
    public void testCreateInterestPauseByLoanId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-12",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testCreateInterestPauseByLoanId_endDateBeforeStartDate_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByLoanId("2024-12-05", "2023-01-12", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.before.start.date"),
                    "Response should contain the validation error message for end date before start date");
        }
    }

    @Test
    public void testCreateInterestPauseByLoanId_startDateBeforeLoanStart_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByLoanId("2022-12-01", "2023-01-12", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.start.date.before.loan.start.date"),
                    "Response should contain the validation error message for start date before loan start date");
        }
    }

    @Test
    public void testCreateInterestPauseByLoanId_endDateAfterLoanMaturity_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByLoanId("2024-12-01", "2025-12-05", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.after.loan.maturity.date"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testRetrieveInterestPausesByLoanId_noPauses_shouldReturnEmpty() {
        String response = LOAN_TRANSACTION_HELPER.retrieveInterestPauseByLoanId(nonExistLoanId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.contains("id"));
        Assertions.assertFalse(response.contains("startDate"));
        Assertions.assertFalse(response.contains("endDate"));
    }

    @Test
    public void testRetrieveInterestPausesByLoanId_shouldReturnData() {
        LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-12", "yyyy-MM-dd", "en", loanId);

        String response = LOAN_TRANSACTION_HELPER.retrieveInterestPauseByLoanId(loanId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.contains("2023-01-01"));
        Assertions.assertTrue(response.contains("2023-01-12"));
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01", "2023-01-12",
                "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_endDateBeforeStartDate_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByExternalId("2023-01-01", "2022-01-12", "yyyy-MM-dd", "en", externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.before.start.date"),
                    "Response should contain the validation error message for end date before start date");
        }
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_startDateBeforeLoanStart_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByExternalId("2022-12-01", "2024-12-05", "yyyy-MM-dd", "en", externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.start.date.before.loan.start.date"),
                    "Response should contain the validation error message for start date before loan start date");
        }
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_endDateAfterLoanMaturity_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByExternalId("2024-12-01", "2025-12-05", "yyyy-MM-dd", "en", externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.after.loan.maturity.date"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testRetrieveInterestPausesByExternalLoanId_noPauses_shouldReturnEmpty() {
        String response = LOAN_TRANSACTION_HELPER.retrieveInterestPauseByExternalId(nonExistExternalId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.contains("id"));
        Assertions.assertFalse(response.contains("startDate"));
        Assertions.assertFalse(response.contains("endDate"));
    }

    @Test
    public void testRetrieveInterestPausesByExternalLoanId_shouldReturnData() {
        LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01", "2023-01-12", "yyyy-MM-dd", "en", externalId);

        String response = LOAN_TRANSACTION_HELPER.retrieveInterestPauseByExternalId(externalId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.contains("2023-01-01"));
        Assertions.assertTrue(response.contains("2023-01-12"));
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-03",
                "yyyy-MM-dd", "en", loanId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-08",
                "2023-01-10", "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByLoanId(variationId, "2023-01-01", "2023-01-12", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldNotFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-03",
                "yyyy-MM-dd", "en", loanId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-08",
                "2023-01-10", "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER.updateInterestPauseByLoanId(variationId, "2023-01-01", "2023-01-07", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldFail2() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-06",
                "yyyy-MM-dd", "en", loanId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-07",
                "2023-01-12", "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByLoanId(variationId, "2023-01-02", "2023-01-13", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByLoanId_overlapping_shouldFail3() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-02", "2023-01-06",
                "yyyy-MM-dd", "en", loanId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-07",
                "2023-01-12", "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByLoanId(variationId, "2023-01-01", "2023-01-11", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByLoanId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-02",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        PostLoansLoanIdTransactionsResponse updateResponse = LOAN_TRANSACTION_HELPER.updateInterestPauseByLoanId(variationId, "2023-01-03",
                "2023-01-04", "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(updateResponse);
        Assertions.assertNotNull(updateResponse.getResourceId());
        Assertions.assertEquals(variationId, updateResponse.getResourceId());
    }

    @Test
    public void testUpdateInterestPauseByLoanId_endDateBeforeStartDate_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-12",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByLoanId(variationId, "2023-03-01", "2023-01-12", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.before.start.date"),
                    "Response should contain the validation error message for end date before start date");
        }
    }

    @Test
    public void testUpdateInterestPauseByLoanId_startDateBeforeLoanStart_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-12",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByLoanId(variationId, "2022-12-01", "2023-01-12", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.start.date.before.loan.start.date"),
                    "Response should contain the validation error message for start date before loan start date");
        }
    }

    @Test
    public void testDeleteInterestPauseByLoanId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-12",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse, "Create response should not be null");
        Assertions.assertNotNull(createResponse.getResourceId(), "Resource ID should not be null");

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTIONAL_HELPER_204.deleteInterestPauseByLoanId(variationId, loanId);
        } catch (Exception e) {
            Assertions.fail("Delete operation failed: " + e.getMessage());
        }

        String response = LOAN_TRANSACTION_HELPER.retrieveInterestPauseByLoanId(loanId);
        Assertions.assertFalse(response.contains(String.valueOf(variationId)), "Response should not contain the deleted variation ID");
    }

    @Test
    public void testDeleteInterestPauseByLoanId_nonExistentVariation_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.deleteInterestPauseByLoanId(99999L, loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("error.msg.variation.not.found"),
                    "Response should contain the validation error message for variation not found");
        }
    }

    @Test
    public void testDeleteInterestPauseByLoanId_invalidLoanId_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-01-12",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_404.deleteInterestPauseByLoanId(variationId, nonExistLoanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("error.msg.loan.id.invalid"),
                    "Response should contain the validation error message for variation not found");
        }
    }

    @Test
    public void testUpdateInterestPauseByExternalId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-02", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        PostLoansLoanIdTransactionsResponse updateResponse = LOAN_TRANSACTION_HELPER.updateInterestPauseByExternalId(variationId,
                "2023-01-03", "2023-01-04", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(updateResponse);
        Assertions.assertNotNull(updateResponse.getResourceId());
        Assertions.assertEquals(variationId, updateResponse.getResourceId());
    }

    @Test
    public void testUpdateInterestPauseByExternalId_overlapping_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-06", "yyyy-MM-dd", "en", externalId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-07",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();
        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByExternalId(variationId, "2023-01-01", "2023-01-12", "yyyy-MM-dd", "en",
                    externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByExternalId_overlapping_shouldFail2() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-06", "yyyy-MM-dd", "en", externalId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-07",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();
        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByExternalId(variationId, "2023-01-02", "2023-01-13", "yyyy-MM-dd", "en",
                    externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByExternalId_overlapping_shouldFail3() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-02",
                "2023-01-06", "yyyy-MM-dd", "en", externalId);
        PostLoansLoanIdTransactionsResponse createResponse2 = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-07",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();
        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByExternalId(variationId, "2023-01-01", "2023-01-11", "yyyy-MM-dd", "en",
                    externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.overlapping"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testUpdateInterestPauseByExternalId_endDateBeforeStartDate_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByExternalId(variationId, "2023-03-01", "2023-01-12", "yyyy-MM-dd", "en",
                    externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.before.start.date"),
                    "Response should contain the validation error message for end date before start date");
        }
    }

    @Test
    public void testUpdateInterestPauseByExternalId_startDateBeforeLoanStart_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_403.updateInterestPauseByExternalId(variationId, "2022-12-01", "2023-01-12", "yyyy-MM-dd", "en",
                    externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.start.date.before.loan.start.date"),
                    "Response should contain the validation error message for start date before loan start date");
        }
    }

    @Test
    public void testDeleteInterestPauseByExternalId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse, "Create response should not be null");
        Assertions.assertNotNull(createResponse.getResourceId(), "Resource ID should not be null");

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTIONAL_HELPER_204.deleteInterestPauseByExternalId(variationId, externalId);
        } catch (Exception e) {
            Assertions.fail("Delete operation failed: " + e.getMessage());
        }

        String response = LOAN_TRANSACTION_HELPER.retrieveInterestPauseByExternalId(externalId);
        Assertions.assertFalse(response.contains(String.valueOf(variationId)), "Response should not contain the deleted variation ID");
    }

    @Test
    public void testDeleteInterestPauseByExternalId_nonExistentVariation_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.deleteInterestPauseByExternalId(99999L, externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("error.msg.variation.not.found"),
                    "Response should contain the validation error message for variation not found");
        }
    }

    @Test
    public void testDeleteInterestPauseByExternalId_invalidExternalId_shouldFail() {
        PostLoansLoanIdTransactionsResponse createResponse = LOAN_TRANSACTION_HELPER.createInterestPauseByExternalId("2023-01-01",
                "2023-01-12", "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(createResponse);
        Assertions.assertNotNull(createResponse.getResourceId());

        Long variationId = createResponse.getResourceId();

        try {
            LOAN_TRANSACTION_HELPER_404.deleteInterestPauseByExternalId(variationId, nonExistExternalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("error.msg.loan.external.id.invalid"),
                    "Response should contain the validation error message for variation not found");
        }
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);

        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientId);
    }

    /**
     * create a new loan product
     **/
    private void createLoanProductEntity() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");

        final String interestRecalculationCompoundingMethod = LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE;
        final String rescheduleStrategyMethod = LoanProductTestBuilder.RECALCULATION_STRATEGY_ADJUST_LAST_UNPAID_PERIOD;
        final String preCloseInterestCalculationStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount).withNumberOfRepayments(numberOfRepayments)
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).addAdvancedPaymentAllocation(defaultAllocation)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .withMultiDisburse().withDisallowExpectedDisbursements(true).withInterestRecalculationDetails(
                        interestRecalculationCompoundingMethod, rescheduleStrategyMethod, preCloseInterestCalculationStrategy)
                .build();

        loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        LOG.info("Successfully created loan product  (ID:{}) ", loanProductId);
    }

    /**
     * submit a new loan application, approve and disburse the loan
     **/
    private void createLoanEntity() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanPrincipalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsDays().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsDays().withInterestRatePerPeriod(interestRatePerPeriod)
                .withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(dateString)
                .withSubmittedOnDate(dateString).withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy("advanced-payment-allocation-strategy").build(clientId.toString(), loanProductId.toString(), null);

        loanId = LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", loanId);

        approveLoanApplication();
        disburseLoan();
    }

    /**
     * approve the loan application
     **/
    private void approveLoanApplication() {

        if (loanId != null) {
            LOAN_TRANSACTION_HELPER.approveLoan(dateString, loanId);
            LOG.info("Successfully approved loan (ID: {} )", loanId);
        }
    }

    /**
     * disburse the newly created loan
     **/
    private void disburseLoan() {

        if (loanId != null) {
            LOAN_TRANSACTION_HELPER.disburseLoan(externalId, new PostLoansLoanIdRequest().actualDisbursementDate(dateString)
                    .transactionAmount(new BigDecimal(loanPrincipalAmount)).locale("en").dateFormat("dd MMMM yyyy"));
            LOG.info("Successfully disbursed loan (ID: {} )", loanId);
        }
    }
}
