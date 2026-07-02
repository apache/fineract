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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanTransactionBackdatedProgressiveTest extends FeignLoanTestBase {

    private static final String DATETIME_PATTERN = LoanTestData.DATETIME_PATTERN;

    private Long clientId;
    private Long loanId;

    @BeforeEach
    public void beforeEach() {
        runAt("01 July 2024", () -> {
            clientId = createClient();
            final Long loanProductId = createLoanProduct(create4IProgressive());
            loanId = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "01 June 2024", 1000.0, 10.0, 4, null));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "01 June 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(250.0), "01 June 2024");
            addRepaymentForLoan(loanId, 100.0, "10 June 2024");
        });
    }

    @Test
    public void testProgressiveBackdatedDisbursement() {
        runAt("01 July 2024", () -> {
            disburseLoan(loanId, BigDecimal.valueOf(250.0), "5 June 2024");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            Assertions.assertEquals(loanDetails.getDisbursementDetails().size(), 2);
        });
    }

    @Test
    public void testProgressiveBackdatedRepayment() {
        runAt("01 July 2024", () -> {
            addRepaymentForLoan(loanId, 100.0, "5 June 2024");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().size() >= 2);
        });
    }

    @Test
    public void testProgressiveBackdatedMerchantIssuedRefund() {
        runAt("01 July 2024", () -> {
            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("5 June 2024").locale("en").transactionAmount(100.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().size() >= 2);
        });
    }

    @Test
    public void testProgressiveBackdatedPayoutRefund() {
        runAt("01 July 2024", () -> {
            makePayoutRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("5 June 2024")
                    .locale("en").transactionAmount(100.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().size() >= 2);
        });
    }

    @Test
    public void testProgressiveBackdatedGoodwillCredit() {
        runAt("01 July 2024", () -> {
            makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("5 June 2024")
                    .locale("en").transactionAmount(100.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().size() >= 2);
        });
    }

    @Test
    public void testProgressiveBackdatedInterestPaymentWaiver() {
        runAt("01 July 2024", () -> {
            makeInterestPaymentWaiver(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("5 June 2024").locale("en").transactionAmount(100.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().size() >= 2);
        });
    }
}
