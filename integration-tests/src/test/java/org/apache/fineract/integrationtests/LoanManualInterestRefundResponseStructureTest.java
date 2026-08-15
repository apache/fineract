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

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RecalculationRestFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.SupportedInterestRefundTypesItem;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests validate that manual Interest Refund transactions return the correct response structure: - entityId should
 * contain the Interest Refund transaction ID - entityExternalId should contain the Interest Refund external ID -
 * subEntityId should be null/not set - subEntityExternalId should be null/not set
 */
@Slf4j
public class LoanManualInterestRefundResponseStructureTest extends FeignLoanTestBase {

    private static PostClientsResponse client;

    @BeforeAll
    public static void beforeAll() {
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    @Test
    public void testManualInterestRefundResponseStructureWithoutExternalIds() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        AtomicReference<Long> targetTransactionIdRef = new AtomicReference<>();

        runAt("01 January 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL).daysInYearType(DaysInYearType.ACTUAL)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY));

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductId, "01 January 2024", 1000.0, 9.9, 12, null);
            assertNotNull(loanId);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "01 January 2024");
        });

        runAt("15 January 2024", () -> {
            Long loanId = loanIdRef.get();

            PostLoansLoanIdTransactionsResponse refundResponse = makeLoanMerchantIssuedRefund(loanId, "15 January 2024", 100.0);
            assertNotNull(refundResponse);
            assertNotNull(refundResponse.getResourceId());
            targetTransactionIdRef.set(refundResponse.getResourceId());

            PostLoansLoanIdTransactionsResponse interestRefundResponse = createManualInterestRefund(loanId, refundResponse.getResourceId(),
                    "15 January 2024", 5.0, null);

            assertNotNull(interestRefundResponse, "Interest refund response should not be null");
            assertNotNull(interestRefundResponse.getResourceId(), "Interest refund resource ID should not be null");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            GetLoansLoanIdTransactions interestRefundTransaction = findTransactionByType(loanDetails, "Interest Refund");
            assertNotNull(interestRefundTransaction, "Interest Refund transaction should exist");

            assertEquals(interestRefundTransaction.getId(), interestRefundResponse.getResourceId(),
                    "Response entityId should be the Interest Refund transaction ID");

            assertNull(interestRefundResponse.getResourceExternalId(), "entityExternalId should be null when no external ID provided");

            assertNull(interestRefundResponse.getSubResourceId(), "subEntityId should be null");

            assertNull(interestRefundResponse.getSubResourceExternalId(), "subEntityExternalId should be null");
        });
    }

    @Test
    public void testManualInterestRefundResponseStructureWithExternalIds() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();

        String loanExternalId = UUID.randomUUID().toString();

        runAt("01 February 2024", () -> {
            Long loanProductId = createLoanProduct(
                    create4IProgressive().daysInMonthType(DaysInMonthType.ACTUAL).daysInYearType(DaysInYearType.ACTUAL)
                            .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY));

            Long loanId = applyAndApproveProgressiveLoanWithExternalId(client.getClientId(), loanProductId, loanExternalId,
                    "01 February 2024", 1000.0, 9.9, 12, null);
            assertNotNull(loanId);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "01 February 2024");
        });

        runAt("15 February 2024", () -> {
            Long loanId = loanIdRef.get();
            String repaymentExternalId = UUID.randomUUID().toString();

            PostLoansLoanIdTransactionsResponse refundResponse = makeLoanMerchantIssuedRefundWithExternalId(loanId, repaymentExternalId,
                    "15 February 2024", 100.0);
            assertNotNull(refundResponse);
            assertNotNull(refundResponse.getResourceId());

            String interestRefundExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse interestRefundResponse = createManualInterestRefund(loanId, refundResponse.getResourceId(),
                    "15 February 2024", 5.0, interestRefundExternalId);

            assertNotNull(interestRefundResponse, "Interest refund response should not be null");
            assertNotNull(interestRefundResponse.getResourceId(), "Interest refund resource ID should not be null");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
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

    private PostLoansLoanIdTransactionsResponse makeLoanMerchantIssuedRefund(Long loanId, String transactionDate, Double amount) {
        org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest request = new org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest()
                .transactionDate(transactionDate).transactionAmount(amount).interestRefundCalculation(false).dateFormat("dd MMMM yyyy")
                .locale("en");
        return makeMerchantIssuedRefund(loanId, request);
    }

    private PostLoansLoanIdTransactionsResponse makeLoanMerchantIssuedRefundWithExternalId(Long loanId, String externalId,
            String transactionDate, Double amount) {
        org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest request = new org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest()
                .transactionDate(transactionDate).transactionAmount(amount).externalId(externalId).interestRefundCalculation(false)
                .dateFormat("dd MMMM yyyy").locale("en");
        return makeMerchantIssuedRefund(loanId, request);
    }

    private GetLoansLoanIdTransactions findTransactionByType(GetLoansLoanIdResponse loanDetails, String transactionType) {
        return loanDetails.getTransactions().stream().filter(t -> transactionType.equals(t.getType().getValue())).findFirst().orElse(null);
    }

    private Long applyAndApproveProgressiveLoanWithExternalId(Long clientId, Long productId, String loanExternalId, String submittedDate,
            Double amount, Double interestRate, Integer termFrequency, java.util.function.Consumer<PostLoansRequest> customizer) {

        PostLoansRequest request = applyLP2ProgressiveLoanRequest(clientId, productId, submittedDate, amount, interestRate, termFrequency,
                customizer);
        request.externalId(loanExternalId);

        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(amount, submittedDate));
        return loanId;
    }
}
