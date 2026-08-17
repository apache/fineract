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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BatchLoanIntegrationTest extends FeignLoanTestBase {

    @Test
    public void test_InlineLoanCOB_ShouldExecute_WhenLoanIsBehind_And_RescheduleIsRequestedViaBatchApi() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            createdLoanId.set(createBehindLoan());
        });

        runAt("02 January 2023", () -> {
            executeInlineCOB(createdLoanId.get());
        });

        runAt("05 January 2023", () -> {
            runAsNonByPass(() -> {
                long loanId = createdLoanId.get();

                List<BatchResponse> responses = batchRequest() //
                        .rescheduleLoan(1L, loanId, "01 January 2023", "01 February 2023", "01 March 2023") //
                        .approveRescheduleLoan(2L, 1L, "01 January 2023") //
                        .executeEnclosingTransaction(); //

                Assertions.assertEquals(HttpStatus.SC_OK, responses.get(0).getStatusCode(),
                        "Verify Status Code 200 for Create Reschedule Loan request");
                Assertions.assertEquals(HttpStatus.SC_OK, responses.get(1).getStatusCode(),
                        "Verify Status Code 200 for Approve Reschedule Loan request");

                verifyLastClosedBusinessDate(loanId, "04 January 2023");
            });
        });
    }

    @Test
    public void test_InlineLoanCOB_ShouldExecute_WhenLoanIsHardLocked_And_RescheduleIsRequestedViaBatchApi() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            createdLoanId.set(createBehindLoan());
        });

        runAt("02 January 2023", () -> {
            executeInlineCOB(createdLoanId.get());
        });

        runAt("05 January 2023", () -> {
            long loanId = createdLoanId.get();
            placeHardLockOnLoan(loanId);
            runAsNonByPass(() -> {

                ErrorResponse response = batchRequest() //
                        .rescheduleLoan(1L, loanId, "01 January 2023", "01 February 2023", "01 March 2023") //
                        .approveRescheduleLoan(2L, 1L, "01 January 2023") //
                        .executeEnclosingTransactionError(); //

                Assertions.assertEquals(HttpStatus.SC_CONFLICT, response.getHttpStatusCode());
            });
        });
    }

    private Long createBehindLoan() {
        // Create Client
        Long clientId = createClient();

        int numberOfRepayments = 24;
        int repaymentEvery = 1;

        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                .numberOfRepayments(numberOfRepayments) //
                .repaymentEvery(repaymentEvery) //
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L); //

        // Create Loan Product
        Long loanProductId = createLoanProduct(product);

        double amount = 1250.0;

        PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                .repaymentEvery(repaymentEvery)//
                .loanTermFrequency(numberOfRepayments)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                .loanTermFrequencyType(RepaymentFrequencyType.MONTHS);

        Long loanId = applyForLoan(applicationRequest);

        approveLoan(loanId, approveLoanRequest(amount, "01 January 2023"));

        disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

        return loanId;
    }
}
