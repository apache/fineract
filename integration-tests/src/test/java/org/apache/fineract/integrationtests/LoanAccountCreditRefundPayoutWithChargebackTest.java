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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanAccountCreditRefundPayoutWithChargebackTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecErr400;
    private ResponseSpecification responseSpecErr503;
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
        this.responseSpecErr400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.responseSpecErr503 = new ResponseSpecBuilder().expectStatusCode(503).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanCreditRefundPayoutGetCreatedWithChargebackTest() {
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

        // make Repayment with full amount, Loan balance becomes zero, Loan closes
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(1000.0));

        // Chargeback full repayment amount to add to principal balance
        PostLoansLoanIdTransactionsResponse chargebackResponse = loanTransactionHelper.chargebackLoanTransaction(loanExternalIdStr,
                repaymentTransaction_1.getResourceId(),
                new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(1000.0).paymentTypeId(1L));

        GetLoansLoanIdTransactionsTransactionIdResponse chargebackTransactionResponse = loanTransactionHelper
                .getLoanTransactionDetails(chargebackResponse.getLoanId(), chargebackResponse.getResourceId());
        assertEquals(1L, chargebackTransactionResponse.getPaymentDetailData().getPaymentType().getId());

        // Verify Goodwill Credit can be created with full amount
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(1000.0));
        assertNotNull(goodwillCredit_1);
        assertEquals(goodwillCredit_1.getLoanId(), (long) loanId);

        // Verify Goodwill Credit can be created after balance is zero again
        final PostLoansLoanIdTransactionsResponse goodwillCredit_2 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(10.0));
        assertNotNull(goodwillCredit_2);
        assertEquals(goodwillCredit_2.getLoanId(), (long) loanId);

        // Verify Payout refund can be created after balance is zero again
        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = loanTransactionHelper.makePayoutRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(10.0));
        assertNotNull(payoutRefund_1);
        assertEquals(payoutRefund_1.getLoanId(), (long) loanId);

        // Verify Merchant refund can be created after balance is zero again
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(10.0));
        assertNotNull(merchantIssuedRefund_1);
        assertEquals(merchantIssuedRefund_1.getLoanId(), (long) loanId);

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
