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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanTransactionSummaryTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanTransactionSummaryTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        log.info("Client Id {}", clientId);
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);

        final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);
        log.info("Loan Id {}", loanId);

        // make Repayments
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(100.0));

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("6 September 2022").locale("en")
                        .transactionAmount(100.0));

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_3 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(50.0));

        // reverse Repayment
        loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction_3.getResourceId().intValue(), "7 September 2022");

        // Merchant Refund
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(100.0));

        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_2 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(50.0));

        // reverse Merchant Refund
        loanTransactionHelper.reverseRepayment(loanId, merchantIssuedRefund_2.getResourceId().intValue(), "8 September 2022");

        // Payout Refund
        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = loanTransactionHelper.makePayoutRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(100.0));

        final PostLoansLoanIdTransactionsResponse payoutRefund_2 = loanTransactionHelper.makePayoutRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(50.0));

        // reverse Payout Refund
        loanTransactionHelper.reverseRepayment(loanId, payoutRefund_2.getResourceId().intValue(), "9 September 2022");

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(100.0));

        final PostLoansLoanIdTransactionsResponse goodwillCredit_2 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(50.0));

        // reverse Goodwill Credit
        loanTransactionHelper.reverseRepayment(loanId, goodwillCredit_2.getResourceId().intValue(), "10 September 2022");

        // Chargeback

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_4 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("11 September 2022").locale("en")
                        .transactionAmount(150.0));

        loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr, repaymentTransaction_4.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(50.0).paymentTypeId(1L));

        // Charge Adjustment

        // Add Charge
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        LocalDate targetDate = LocalDate.of(2022, 9, 10);
        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = loanTransactionHelper.chargeAdjustment((long) loanId,
                (long) penalty1LoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).locale("en"));

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

        // verify transaction summary fields

        GetLoansLoanIdSummary loanSummary = loanDetails.getSummary();

        assertNotNull(loanSummary);

        // repayment
        assertEquals(loanSummary.getTotalRepaymentTransaction(), 350.00);
        // repayment reversed
        assertEquals(loanSummary.getTotalRepaymentTransactionReversed(), 50.00);
        // merchant refund
        assertEquals(loanSummary.getTotalMerchantRefund(), 100.00);
        // merchant refund reversed
        assertEquals(loanSummary.getTotalMerchantRefundReversed(), 50.00);
        // payout refund
        assertEquals(loanSummary.getTotalPayoutRefund(), 100.00);
        // payout refund reversed
        assertEquals(loanSummary.getTotalPayoutRefundReversed(), 50.00);
        // goodwill credit
        assertEquals(loanSummary.getTotalGoodwillCredit(), 100.00);
        // goodwill credit reversed
        assertEquals(loanSummary.getTotalGoodwillCreditReversed(), 50.00);
        // charge adjustment
        assertEquals(loanSummary.getTotalChargeAdjustment(), 10.00);
        // charge
        assertEquals(loanSummary.getTotalChargeback(), 50.00);
    }

    @Test
    public void lastRepaymentAmountTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);

        final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

        // Merchant Refund
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(20.0));

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        final PostLoansLoanIdTransactionsResponse repaymentTransaction_2 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("6 September 2022").locale("en")
                        .transactionAmount(50.0));

        // Retrieve Loan with loanId
        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);

        assertEquals(20.0, loanDetails.getDelinquent().getLastPaymentAmount());
        assertEquals(LocalDate.of(2022, 9, 8), loanDetails.getDelinquent().getLastPaymentDate());

        assertEquals(100.0, loanDetails.getDelinquent().getLastRepaymentAmount());
        assertEquals(LocalDate.of(2022, 9, 7), loanDetails.getDelinquent().getLastRepaymentDate());
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

}
