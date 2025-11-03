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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanProductOverAppliedAmountTest extends BaseLoanIntegrationTest {

    @Test
    public void testCreateMultiDisburseLoanProductWithOverAppliedAmountAndExpectedTranches() {
        runAt("01 January 2024", () -> {
            // Create Loan Product with multi-disburse, expected tranches, and over-applied amount
            final PostLoanProductsRequest loanProductRequest = create4IProgressive().multiDisburseLoan(true).maxTrancheCount(5)
                    .outstandingLoanBalance(10000.0).disallowExpectedDisbursements(false) // Expected tranches enabled
                    .allowApprovedDisbursedAmountsOverApplied(true).overAppliedCalculationType("percentage").overAppliedNumber(50);

            // This should not throw an exception
            final PostLoanProductsResponse loanProductResponse = assertDoesNotThrow(
                    () -> loanProductHelper.createLoanProduct(loanProductRequest));

            assertNotNull(loanProductResponse);
            assertNotNull(loanProductResponse.getResourceId());

            // Retrieve the created loan product to verify settings
            final GetLoanProductsProductIdResponse retrievedProduct = loanProductHelper
                    .retrieveLoanProductById(loanProductResponse.getResourceId());

            // Verify the loan product was created with correct settings
            assertEquals(true, retrievedProduct.getMultiDisburseLoan());
            assertEquals(false, retrievedProduct.getDisallowExpectedDisbursements());
            assertEquals(true, retrievedProduct.getAllowApprovedDisbursedAmountsOverApplied());
            assertEquals("percentage", retrievedProduct.getOverAppliedCalculationType());
        });
    }

    @Test
    public void testModifyMultiDisburseLoanProductWithOverAppliedAmountAndExpectedTranches() {
        runAt("01 January 2024", () -> {
            // Create initial loan product without over-applied amount
            final PostLoanProductsRequest initialLoanProductRequest = create4IProgressive().multiDisburseLoan(true).maxTrancheCount(5)
                    .outstandingLoanBalance(10000.0).disallowExpectedDisbursements(false).allowApprovedDisbursedAmountsOverApplied(false)
                    .overAppliedCalculationType(null).overAppliedNumber(null);

            final PostLoanProductsResponse initialLoanProductResponse = loanProductHelper.createLoanProduct(initialLoanProductRequest);
            final Long loanProductId = initialLoanProductResponse.getResourceId();

            // Modify loan product to enable over-applied amount
            final PutLoanProductsProductIdRequest modifyRequest = new PutLoanProductsProductIdRequest()
                    .allowApprovedDisbursedAmountsOverApplied(true).overAppliedCalculationType("flat").overAppliedNumber(200).locale("en");

            final PutLoanProductsProductIdResponse modifyResponse = assertDoesNotThrow(
                    () -> loanProductHelper.updateLoanProductById(loanProductId, modifyRequest));

            assertNotNull(modifyResponse);

            // Retrieve the updated loan product to verify settings
            final GetLoanProductsProductIdResponse retrievedProduct = loanProductHelper.retrieveLoanProductById(loanProductId);
            assertEquals(true, retrievedProduct.getMultiDisburseLoan());
            assertEquals(false, retrievedProduct.getDisallowExpectedDisbursements());
            assertEquals(true, retrievedProduct.getAllowApprovedDisbursedAmountsOverApplied());
            assertEquals("flat", retrievedProduct.getOverAppliedCalculationType());
        });
    }

    @Test
    public void testAvailableDisbursementAmountNotNegativeWhenDisbursedAmountExceedsApprovedAmount() {
        runAt("01 January 2024", () -> {
            // Create Loan Product with over-applied amount enabled
            final PostLoanProductsRequest loanProductRequest = create4IProgressive().multiDisburseLoan(true).maxTrancheCount(5)
                    .outstandingLoanBalance(10000.0).disallowExpectedDisbursements(false) // Expected tranches enabled
                    .allowApprovedDisbursedAmountsOverApplied(true).overAppliedCalculationType("percentage").overAppliedNumber(50);

            final PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductRequest);
            final Long loanProductId = loanProductResponse.getResourceId();

            // Create client
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            assertNotNull(clientId);

            // Create and approve loan with amount 1000
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 1000.0, 7.0, 6,
                    request -> request.disbursementData(List.of(new PostLoansDisbursementData().expectedDisbursementDate("1 January 2024")
                            .principal(BigDecimal.valueOf(1000.0)))));

            // Disburse loan with amount 1500 (exceeds approved amount, but allowed due to over-applied setting)
            disburseLoan(loanId, BigDecimal.valueOf(1500.0), "01 January 2024");

            // Verify loan is active
            verifyLoanStatus(loanId, LoanStatus.ACTIVE);

            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails);

            // Verify that the loan was created and disbursed successfully
            assert loanDetails.getStatus() != null;
            assertEquals(Boolean.TRUE, loanDetails.getStatus().getActive());

            // Verify the amounts
            assert loanDetails.getApprovedPrincipal() != null;
            assertEquals(BigDecimal.valueOf(1000.0), loanDetails.getApprovedPrincipal().setScale(1, RoundingMode.HALF_UP));
            assert loanDetails.getDisbursementDetails() != null;
            double disbursementPrincipalSum = loanDetails.getDisbursementDetails().stream().mapToDouble(detail -> {
                assert detail.getPrincipal() != null;
                return detail.getPrincipal();
            }).sum();
            assertEquals(1500.0, disbursementPrincipalSum);

            // The key test: availableDisbursementAmount should be 0 (not negative)
            // since disbursed amount (1500) > approved amount (1000)
            assert loanDetails.getDelinquent() != null;
            final BigDecimal availableDisbursementAmount = loanDetails.getDelinquent().getAvailableDisbursementAmount();
            assertNotNull(availableDisbursementAmount);
            assertTrue(availableDisbursementAmount.compareTo(BigDecimal.ZERO) >= 0,
                    "availableDisbursementAmount should not be negative. Expected >= 0, but was: " + availableDisbursementAmount);
            assertEquals(BigDecimal.ZERO, availableDisbursementAmount,
                    "availableDisbursementAmount should be 0 when disbursed amount exceeds approved amount");
        });
    }
}
