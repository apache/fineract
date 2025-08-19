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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests validate that manual Interest Refund transactions return the correct response structure: - entityId should
 * contain the Interest Refund transaction ID - entityExternalId should contain the Interest Refund external ID -
 * subEntityId should be null/not set - subEntityExternalId should be null/not set
 */
@Slf4j
public class LoanManualInterestRefundResponseStructureTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private PostClientsResponse client;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        this.client = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    @Test
    public void testManualInterestRefundResponseStructureWithoutExternalIds() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> targetTransactionIdRef = new AtomicReference<>();

        runAt("01 January 2024", () -> {
            // Create loan product that supports manual interest refund
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL).daysInYearType(DaysInYearType.ACTUAL)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY));

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "01 January 2024", 1000.0, 9.9,
                    12, null);
            assertNotNull(loanId);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "01 January 2024");
        });

        runAt("15 January 2024", () -> {
            Long loanId = loanIdRef.get();

            // Make a merchant issued refund to have a target transaction that supports manual interest refund
            PostLoansLoanIdTransactionsResponse refundResponse = makeLoanMerchantIssuedRefund(loanId, "15 January 2024", 100.0);
            assertNotNull(refundResponse);
            assertNotNull(refundResponse.getResourceId());
            targetTransactionIdRef.set(refundResponse.getResourceId());

            // Create manual interest refund via API
            PostLoansLoanIdTransactionsResponse interestRefundResponse = createManualInterestRefund(loanId, refundResponse.getResourceId(),
                    "15 January 2024", 5.0, null);

            assertNotNull(interestRefundResponse, "Interest refund response should not be null");
            assertNotNull(interestRefundResponse.getResourceId(), "Interest refund resource ID should not be null");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            GetLoansLoanIdTransactions interestRefundTransaction = findTransactionByType(loanDetails, "Interest Refund");
            assertNotNull(interestRefundTransaction, "Interest Refund transaction should exist");

            assertEquals(interestRefundTransaction.getId(), interestRefundResponse.getResourceId(),
                    "Response entityId should be the Interest Refund transaction ID");

            // entityExternalId should be null (since no external ID was provided)
            assertNull(interestRefundResponse.getResourceExternalId(), "entityExternalId should be null when no external ID provided");

            // subEntityId should be null (not the target transaction ID)
            assertNull(interestRefundResponse.getSubResourceId(), "subEntityId should be null");

            // subEntityExternalId should be null
            assertNull(interestRefundResponse.getSubResourceExternalId(), "subEntityExternalId should be null");
        });
    }

    @Test
    public void testManualInterestRefundResponseStructureWithExternalIds() {
        AtomicReference<String> loanExternalIdRef = new AtomicReference<>();
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<String> targetTransactionExternalIdRef = new AtomicReference<>();

        String loanExternalId = UUID.randomUUID().toString();
        loanExternalIdRef.set(loanExternalId);

        runAt("01 February 2024", () -> {
            // Create loan product that supports manual interest refund
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL).daysInYearType(DaysInYearType.ACTUAL)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY));

            Long loanId = applyAndApproveProgressiveLoanWithExternalId(client.getClientId(), loanProduct.getResourceId(), loanExternalId,
                    "01 February 2024", 1000.0, 9.9, 12, null);
            assertNotNull(loanId);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "01 February 2024");
        });

        runAt("15 February 2024", () -> {
            Long loanId = loanIdRef.get();
            String repaymentExternalId = UUID.randomUUID().toString();
            targetTransactionExternalIdRef.set(repaymentExternalId);

            // Make a merchant issued refund with external ID (without automatic interest refund)
            PostLoansLoanIdTransactionsResponse refundResponse = makeLoanMerchantIssuedRefundWithExternalId(loanId, repaymentExternalId,
                    "15 February 2024", 100.0);
            assertNotNull(refundResponse);
            assertNotNull(refundResponse.getResourceId());

            // Create manual interest refund with external ID
            String interestRefundExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse interestRefundResponse = createManualInterestRefund(loanId, refundResponse.getResourceId(),
                    "15 February 2024", 5.0, interestRefundExternalId);

            assertNotNull(interestRefundResponse, "Interest refund response should not be null");
            assertNotNull(interestRefundResponse.getResourceId(), "Interest refund resource ID should not be null");

            // Get the actual interest refund transaction to verify
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            GetLoansLoanIdTransactions interestRefundTransaction = findTransactionByType(loanDetails, "Interest Refund");
            assertNotNull(interestRefundTransaction, "Interest Refund transaction should exist");

            assertEquals(interestRefundTransaction.getId(), interestRefundResponse.getResourceId(),
                    "Response entityId should be the Interest Refund transaction ID");

            assertEquals(interestRefundExternalId, interestRefundResponse.getResourceExternalId(),
                    "entityExternalId should be the Interest Refund external ID");

            assertNull(interestRefundResponse.getSubResourceId(), "subEntityId should be null");

            assertNull(interestRefundResponse.getSubResourceExternalId(), "subEntityExternalId should be null");
        });
    }

    /**
     * Helper method to create manual interest refund transaction
     */
    private PostLoansLoanIdTransactionsResponse createManualInterestRefund(Long loanId, Long targetTransactionId, String transactionDate,
            Double amount, String externalId) {

        PostLoansLoanIdTransactionsTransactionIdRequest request = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionAmount(amount).dateFormat("dd MMMM yyyy").locale("en");

        if (externalId != null) {
            request.externalId(externalId);
        }

        return loanTransactionHelper.manualInterestRefund(loanId, targetTransactionId, request);
    }

    /**
     * Helper method to make loan merchant issued refund (without automatic interest refund)
     */
    private PostLoansLoanIdTransactionsResponse makeLoanMerchantIssuedRefund(Long loanId, String transactionDate, Double amount) {
        // Create merchant issued refund transaction without automatic interest refund
        org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest request = new org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest()
                .transactionDate(transactionDate).transactionAmount(amount).interestRefundCalculation(false).dateFormat("dd MMMM yyyy")
                .locale("en");
        return loanTransactionHelper.makeMerchantIssuedRefund(loanId, request);
    }

    /**
     * Helper method to make loan merchant issued refund with external ID (without automatic interest refund)
     */
    private PostLoansLoanIdTransactionsResponse makeLoanMerchantIssuedRefundWithExternalId(Long loanId, String externalId,
            String transactionDate, Double amount) {
        // Create merchant issued refund transaction with external ID but without automatic interest refund
        org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest request = new org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest()
                .transactionDate(transactionDate).transactionAmount(amount).externalId(externalId).interestRefundCalculation(false)
                .dateFormat("dd MMMM yyyy").locale("en");
        return loanTransactionHelper.makeMerchantIssuedRefund(loanId, request);
    }

    /**
     * Helper method to find transaction by type
     */
    private GetLoansLoanIdTransactions findTransactionByType(GetLoansLoanIdResponse loanDetails, String transactionType) {
        return loanDetails.getTransactions().stream().filter(t -> transactionType.equals(t.getType().getValue())).findFirst().orElse(null);
    }

    /**
     * Helper method to apply and approve progressive loan with external ID
     */
    private Long applyAndApproveProgressiveLoanWithExternalId(Long clientId, Long productId, String loanExternalId, String submittedDate,
            Double amount, Double interestRate, Integer termFrequency,
            java.util.function.Consumer<org.apache.fineract.client.models.PostLoansRequest> customizer) {

        org.apache.fineract.client.models.PostLoansRequest request = applyLP2ProgressiveLoanRequest(clientId, productId, submittedDate,
                amount, interestRate, termFrequency, customizer);
        request.externalId(loanExternalId);

        org.apache.fineract.client.models.PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(request);
        Long loanId = loanResponse.getLoanId();

        loanTransactionHelper.approveLoan(loanId, approveLoanRequest(amount, submittedDate));
        return loanId;
    }
}
