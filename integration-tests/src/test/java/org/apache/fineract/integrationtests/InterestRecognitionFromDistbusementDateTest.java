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

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class InterestRecognitionFromDistbusementDateTest extends FeignLoanTestBase {

    // UC1: Create Loan Product using Progressive Loan Schedule Type and interestChargedFromDisbursementDate flag
    // 1. Create a Loan product with Adv. Pment. Alloc. (PROGRESSIVE) without interestChargedFromDisbursementDate
    // 2. Create a Loan product with Adv. Pment. Alloc. (PROGRESSIVE) and interestChargedFromDisbursementDate
    // 3. Create a Loan product with Cumulative Loan Schedule and interestChargedFromDisbursementDate
    @Test
    public void uc1() {
        final String operationDate = "1 January 2025";
        runAt(operationDate, () -> {
            // Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) withou interestChargedFromDisbursementDate
            // Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) without interestChargedFromDisbursementDate
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .numberOfRepayments(6);
            Long loanProductId = createLoanProduct(product);
            GetLoanProductsProductIdResponse loanProductData = retrieveLoanProduct(loanProductId);
            assertEquals(Boolean.FALSE, loanProductData.getInterestRecognitionOnDisbursementDate());

            // Create a Loan Product Adv. Pment. Alloc. (PROGRESSIVE) using interestChargedFromDisbursementDate in true
            product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation().numberOfRepayments(6)
                    .interestRecognitionOnDisbursementDate(true);
            loanProductId = createLoanProduct(product);
            loanProductData = retrieveLoanProduct(loanProductId);
            assertEquals(Boolean.TRUE, loanProductData.getInterestRecognitionOnDisbursementDate());

            // Try to create a Loan Product (CUMULATIVE) using interestChargedFromDisbursementDate in true
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> createLoanProduct(
                    createOnePeriod30DaysPeriodicAccrualProduct(8.0).numberOfRepayments(6).interestRecognitionOnDisbursementDate(true)));
            assertEquals(403, exception.getStatus());
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
            Long loanProductId = createLoanProduct(product);
            GetLoanProductsProductIdResponse loanProductData = retrieveLoanProduct(loanProductId);

            Long clientId = createClient();

            // Create a Loan account and inherit the interestChargedFromDisbursementDate flag
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 4)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
            Long loanId = applyForLoan(applicationRequest);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(loanProductData.getInterestRecognitionOnDisbursementDate(),
                    loanDetails.getInterestRecognitionOnDisbursementDate());

            // Create a Loan account and override the interestChargedFromDisbursementDate flag
            applicationRequest = applyLoanRequest(clientId, loanProductId, operationDate, 100.0, 4)
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                    .interestRecognitionOnDisbursementDate(false);
            loanId = applyForLoan(applicationRequest);
            loanDetails = getLoanDetails(loanId);
            assertNotEquals(loanProductData.getInterestRecognitionOnDisbursementDate(),
                    loanDetails.getInterestRecognitionOnDisbursementDate());
        });
    }
}
