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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterestRecognitionFromDistbusementDateTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(InterestRecognitionFromDistbusementDateTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static Integer commonLoanProductId;
    private static PostClientsResponse client;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
    }

    // UC1: Create Loan Product using Progressive Loan Schedule Type and interestChargedFromDisbursementDate flag
    // 1. Create a Loan product with Adv. Pment. Alloc. (PROGRESSIVE) without interestChargedFromDisbursementDate
    // 2. Create a Loan product with Adv. Pment. Alloc. (PROGRESSIVE) and interestChargedFromDisbursementDate
    // 3. Create a Loan product with Cumulative Loan Schedule and interestChargedFromDisbursementDate
    @Test
    public void uc1() {
        final String operationDate = "1 January 2025";
        runAt(operationDate, () -> {
            // Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) withou interestChargedFromDisbursementDate
            LOG.info("Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) not using interestChargedFromDisbursementDate flag");
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .numberOfRepayments(6);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            GetLoanProductsProductIdResponse loanProductData = loanProductHelper
                    .retrieveLoanProductById(loanProductResponse.getResourceId());
            assertEquals(Boolean.FALSE, loanProductData.getInterestRecognitionOnDisbursementDate());

            // Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) using interestChargedFromDisbursementDate in true
            LOG.info("Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) using interestChargedFromDisbursementDate flag");
            product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation().numberOfRepayments(6)
                    .interestRecognitionOnDisbursementDate(true);
            loanProductResponse = loanProductHelper.createLoanProduct(product);
            loanProductData = loanProductHelper.retrieveLoanProductById(loanProductResponse.getResourceId());
            assertEquals(Boolean.TRUE, loanProductData.getInterestRecognitionOnDisbursementDate());

            // Try to create a Loan Product (CUMULATIVE) using interestChargedFromDisbursementDate in true
            LOG.info("Try to create a Loan Product (CUMULATIVE) using interestChargedFromDisbursementDate flag");
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> loanProductHelper.createLoanProduct(
                    createOnePeriod30DaysPeriodicAccrualProduct(8.0).numberOfRepayments(6).interestRecognitionOnDisbursementDate(true)));
            assertEquals(403, exception.getResponse().code());
            assertTrue(exception.getMessage()
                    .contains("interestRecognitionOnDisbursementDate.is.only.supported.for.progressive.loan.schedule.type"));
        });
    }

    // UC2: Create Loan Product using Progressive Loan Schedule Type and interestChargedFromDisbursementDate flag
    // 1. Create a Loan product with Adv. Pment. Alloc. (PROGRESSIVE) and interestChargedFromDisbursementDate
    // 2. Create a Loan account and inherit the interestChargedFromDisbursementDate flag
    // 3. Create a Loan account and override the interestChargedFromDisbursementDate flag
    @Test
    public void uc2() {
        final String operationDate = "1 January 2025";
        runAt(operationDate, () -> {
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .numberOfRepayments(6).interestRecognitionOnDisbursementDate(true);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            GetLoanProductsProductIdResponse loanProductData = loanProductHelper
                    .retrieveLoanProductById(loanProductResponse.getResourceId());

            client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

            PostLoansRequest applicationRequest = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(), operationDate,
                    100.0, 4).transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);

            // Create a Loan account and inherit the interestChargedFromDisbursementDate flag
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            assertEquals(loanProductData.getInterestRecognitionOnDisbursementDate(),
                    loanDetails.getInterestRecognitionOnDisbursementDate());

            // Create a Loan account and override the interestChargedFromDisbursementDate flag
            applicationRequest = applyLoanRequest(client.getClientId(), loanProductResponse.getResourceId(), operationDate, 100.0, 4)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                    .interestRecognitionOnDisbursementDate(false);
            loanResponse = loanTransactionHelper.applyLoan(applicationRequest);

            // Create a Loan account and inherit the interestChargedFromDisbursementDate flag
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            assertNotEquals(loanProductData.getInterestRecognitionOnDisbursementDate(),
                    loanDetails.getInterestRecognitionOnDisbursementDate());
        });
    }

}
