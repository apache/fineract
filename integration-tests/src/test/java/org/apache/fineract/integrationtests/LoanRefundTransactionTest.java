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
import java.util.ArrayList;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanRefundTransactionTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRefundTransactionTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static BusinessDateHelper businessDateHelper;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);
    }

    @Test
    public void testMerchantIssuedRefundCreatesAndReversesInterestRefund() {
        runAt("01 July 2024", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createAndDisburseLoanForMerchantIssuedRefundWithInterestRefund(clientId);
            final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResponse = loanTransactionHelper.makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 July 2024").locale("en")
                            .transactionAmount(100.0));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(
                    loanDetails.getTransactions().stream().anyMatch(transaction -> transaction.getType().getMerchantIssuedRefund()
                            && Boolean.FALSE.equals(transaction.getManuallyReversed())));

            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .anyMatch(transaction -> transaction.getType().getCode().equals("loanTransactionType.interestRefund")
                            && Boolean.FALSE.equals(transaction.getManuallyReversed())));

            loanTransactionHelper.reverseLoanTransaction(loanId.intValue(), merchantIssuedRefundResponse.getResourceId(), "01 July 2024",
                    responseSpec);

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(
                    loanDetails.getTransactions().stream().anyMatch(transaction -> transaction.getType().getMerchantIssuedRefund()
                            && Boolean.TRUE.equals(transaction.getManuallyReversed())));

            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .anyMatch(transaction -> transaction.getType().getCode().equals("loanTransactionType.interestRefund")
                            && Boolean.TRUE.equals(transaction.getManuallyReversed())));
        });
    }

    @Test
    public void testPayoutRefundCreatesAndReversesInterestRefund() {
        runAt("01 July 2024", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createAndDisburseLoanForPayoutRefundWithInterestRefund(clientId);
            final PostLoansLoanIdTransactionsResponse payoutRefundResponse = loanTransactionHelper.makePayoutRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 July 2024").locale("en")
                            .transactionAmount(100.0));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().stream().anyMatch(
                    transaction -> transaction.getType().getPayoutRefund() && Boolean.FALSE.equals(transaction.getManuallyReversed())));

            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .anyMatch(transaction -> transaction.getType().getCode().equals("loanTransactionType.interestRefund")
                            && Boolean.FALSE.equals(transaction.getManuallyReversed())));

            loanTransactionHelper.reverseLoanTransaction(loanId.intValue(), payoutRefundResponse.getResourceId(), "01 July 2024",
                    responseSpec);

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().stream().anyMatch(
                    transaction -> transaction.getType().getPayoutRefund() && Boolean.TRUE.equals(transaction.getManuallyReversed())));

            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .anyMatch(transaction -> transaction.getType().getCode().equals("loanTransactionType.interestRefund")
                            && Boolean.TRUE.equals(transaction.getManuallyReversed())));
        });
    }

    @Test
    public void testMerchantIssuedRefundDoesNotCreateInterestRefundWithLessThanOrEqualToZeroInterest() {
        runAt("20 April 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanId = createLoanForRefundWithInterestRefund(clientId, "MERCHANT_ISSUED_REFUND", "05 April 2025", 500.0, 20.99, 6);
            disburseLoan(loanId, BigDecimal.valueOf(265.91), "05 April 2025");
            disburseLoan(loanId, BigDecimal.valueOf(1.99), "05 April 2025");
            disburseLoan(loanId, BigDecimal.valueOf(20.00), "05 April 2025");

            final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResponse1 = loanTransactionHelper.makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 April 2025").locale("en")
                            .transactionAmount(6.29));

            final PostLoansLoanIdTransactionsResponse merchantIssuedRefundResponse2 = loanTransactionHelper.makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("07 April 2025").locale("en")
                            .transactionAmount(1.99));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertTrue(loanDetails.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getCode().equals("loanTransactionType.interestRefund"))
                    .allMatch(transaction -> transaction.getAmount().doubleValue() > 0.0));
        });
    }

    private Long createAndDisburseLoanForMerchantIssuedRefundWithInterestRefund(Long clientId) {
        return createAndDisburseLoanForRefundWithInterestRefund(clientId, "MERCHANT_ISSUED_REFUND");
    }

    private Long createAndDisburseLoanForPayoutRefundWithInterestRefund(Long clientId) {
        return createAndDisburseLoanForRefundWithInterestRefund(clientId, "PAYOUT_REFUND");
    }

    private Long createAndDisburseLoanForRefundWithInterestRefund(Long clientId, String refundType) {
        Long loanId = createLoanForRefundWithInterestRefund(clientId, refundType, "01 June 2024", 1000.0, 10.0, 4);
        disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 June 2024");
        return loanId;
    }

    private Long createLoanForRefundWithInterestRefund(Long clientId, String refundType, String date, double amount, double interestRate,
            int numRepayments) {
        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(
                create4IProgressive().supportedInterestRefundTypes(new ArrayList<>()).addSupportedInterestRefundTypesItem(refundType));
        PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(clientId,
                loanProductsResponse.getResourceId(), date, amount, interestRate, numRepayments, null));
        Long loanId = postLoansResponse.getLoanId();
        loanTransactionHelper.approveLoan(loanId, approveLoanRequest(amount, date));
        return loanId;
    }
}
